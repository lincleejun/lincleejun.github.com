---
layout: post
title: android 无限列表
description: 每个android程序猿都应该知道的无限列表，这里有一个简易且易用的实现，可以作为参照 
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/on-the-road.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/peak.jpeg
---

无限列表已经是一个烂大街的东西了，本身没啥值得说的，处于学习的目的就啰嗦几句。

无限列表各家实现方法各不一致，不过大同小异，本次使用的方法是当用户在ListView或者RecyclerView往上滚动时，当滚动到底部或者快到底部（预加载）时，通知外部调用进行数据加载。


所以问题来了：
怎么判断快要到底部？或者到了底部？

1.针对 AbsListView 这种在 `OnScrollListener`里面将滚动的位置信息全部暴露给listener了,所以做起来比较简单，每次在listener中获取到事件后，即可进行计算了。
但是这里有个小坑，就是AbsListView的Listener是设置的，也就是说同时只能有一个存在，所以设置的顺序及设置的地方还需要注意。

2.针对RecyclerView 在`OnScrollListener`里面就没有暴露出任何的位置信息了，只有dx及dy，所以需要我们自行计算一下当前的firstVisibleItem、visibleItemCount等。


我们要做什么？
从上面的信息看到，我们需要做的就是适配一下不同的view，然后提供一个简单的接口给外部使用，到适当的时机，让外部进行数据加载，也就是说我们只是作为一个现有view的修饰，没有我们view照样使用。

所以有了一个修饰基类

{% highlight cpp %}
public abstract class BaseViewDecorator<DecoratedView, ScrollListener> {

  private static final int DEFAULT_PRELOAD_OFFSET = 5;

  private DecoratedView decoratedView;
  private ScrollListener scrollListener;
  private LoadingDelegate loadingDelegate;

  private int preloadOffset = DEFAULT_PRELOAD_OFFSET;

  public BaseViewDecorator(DecoratedView decoratedView, LoadingDelegate delegate) {
    this.decoratedView = Preconditions.checkNotNull(decoratedView, "decorated view");
    loadingDelegate = Preconditions.checkNotNull(delegate, "delegate");
  }

  /**
   * let subclass to do some real things.
   */
  public abstract void start();

  public DecoratedView getDecoratedView() {
    return decoratedView;
  }

  public LoadingDelegate getLoadingDelegate() {
    return loadingDelegate;
  }

  public BaseViewDecorator setScrollListener(ScrollListener scrollListener) {
    this.scrollListener = scrollListener;
    return this;
  }

  public ScrollListener getScrollListener() {
    return scrollListener;
  }

  public int getPreloadOffset() {
    return preloadOffset;
  }

  public BaseViewDecorator setPreloadOffset(int preloadOffset) {
    if (preloadOffset < 0) {
      throw new IllegalArgumentException("preload offset cannot less than 0");
    }
    this.preloadOffset = preloadOffset;
    return this;
  }
}
{% endhighlight %}

{% highlight cpp %}
public interface LoadingDelegate {

  // call when need load data.
  void onLoadMore();

  // tell currently is loading data or not.
  boolean isLoading();

  // if all data loaded, we'll not load any more.
  boolean isAllDataLoaded();
}
{% endhighlight %}
这个基类的主要作用就是监控`DecoratedView`的滚动消息，当上面描述的「适当时机」调用`delegate`的方法，通知外部进行数据加载。


对于 `AbsListView` 的实现

{% highlight cpp %}
// scroll up
if (previewsVisibleItem != -1 && previewsVisibleItem < firstVisibleItem) {
  if (!getLoadingDelegate().isLoading() && !getLoadingDelegate().isAllDataLoaded()) {
    int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
    int lastAdapterItem = totalItemCount - 1;
    if (lastVisibleItem >= (lastAdapterItem - getPreloadOffset())) {
      getLoadingDelegate().onLoadMore();
    }
  }
}
{% endhighlight %}

很简单，就是判断是否快要显示完全了，然后调用 `onLoadMore`。


再看一下RecyclerView的实现

{% highlight java %}

int firstVisibleItem = positionHelper.findFirstVisibleItemPosition();

if (previewsVisibleItem != -1 && previewsVisibleItem < firstVisibleItem) {
  if (!getLoadingDelegate().isLoading() && !getLoadingDelegate().isAllDataLoaded()) {
    int totalItemCount = positionHelper.getItemCount();
    int visibleItemCount = Math.abs(
        positionHelper.findLastVisibleItemPosition() - firstVisibleItem);
    int lastAdapterItem = totalItemCount - 1;
    int lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
    if (lastVisibleItem >= (lastAdapterItem - getPreloadOffset())) {
      getLoadingDelegate().onLoadMore();
    }
  }
}
{% endhighlight %}

其实大体上基本一样，不一样的就是没法直接拿到`firstVisibleItem`  `visibleItemCount` 和 `totalItemCount` 了。
所以我们要想法搞定。

用RecyclerView的时候要设置一个LayoutManager，所以将目光转向了LayoutManager，然后一看就能够直接得到`totalItemCount`了。
剩下的两个就需要我们自行计算了。计算的时候还需要LayoutManager中View的orientation是vertical还是horizontal的。

直接上代码吧。

{% highlight java %}

  View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                           boolean acceptPartiallyVisible) {
    OrientationHelper helper;
    if (layoutManager.canScrollVertically()) {
      helper = OrientationHelper.createVerticalHelper(layoutManager);
    } else {
      helper = OrientationHelper.createHorizontalHelper(layoutManager);
    }

    final int start = helper.getStartAfterPadding();
    final int end = helper.getEndAfterPadding();
    final int next = toIndex > fromIndex ? 1 : -1;
    View partiallyVisible = null;
    for (int i = fromIndex; i != toIndex; i += next) {
      final View child = layoutManager.getChildAt(i);
      final int childStart = helper.getDecoratedStart(child);
      final int childEnd = helper.getDecoratedEnd(child);
      if (childStart < end && childEnd > start) {
        if (completelyVisible) {
          if (childStart >= start && childEnd <= end) {
            return child;
          } else if (acceptPartiallyVisible && partiallyVisible == null) {
            partiallyVisible = child;
          }
        } else {
          return child;
        }
      }
    }
    return partiallyVisible;
  }
{% endhighlight %}

[RecyclerViewPositionHelper](https://github.com/vinaysshenoy/mugen/blob/master/library/src/main/java/com/mugen/attachers/RecyclerViewPositionHelper.java)

总的来说这种实现方式还是蛮符合我自己的期望的，使用也比较灵活，可以随时换换，也比较轻量级

参考
[mugen](https://github.com/vinaysshenoy/mugen)
对于原库有一些地方不是很满意，自己进行了一次修改，所以上面的代码与参照的代码有所不一致。

