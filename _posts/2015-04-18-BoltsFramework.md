---
layout: post
title: BoltsFramework 简介
description: 介绍一些bolts的简单用法
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/on-the-road.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/peak.jpeg
---

前段时间 `facebook` 开源了图像库 `Fresco`， 于是抽空看了下，单从图像库的角度讲，可能是至今android平台最屌的了，平时项目中能用到的基本都有，而且，若要将该库接入已有的平台也非常简单，更多信息 [狂点Fresco 官网](http://frescolib.org) 或者 [Fresco github](http://github.com/facebook/fresco)。

在Fresco的网络缓存部分采用了一个叫 `bolts` 的库，还挺奇怪的，只是这么一个小地方用，引入一个新库值得吗？whatever这不是我要想的问题，我要想的，facebook既然用了这么一个库，应该还比较屌吧？于是乎就开始找找相关介绍，结果就是很少有这个的介绍，难道是因为这个太low？完全没人用？还是都在用 `RxJava` ？还是都在用原生的 `AsyncTask` ？

去 [官网](http://parse.com) 上找了下，该公司现属facebook旗下，而且还有不少知名的公司。

## Bolts主要提供两个比较大的Feature。
* Tasks，更好的组织复杂的异步代码，与`JavaScript`的Promise非常像，但是是针对iOS和Android的一个实现。
* [App Links protocl](http://applinks.org/)辅助其他apps处理deep-links的。


针对第一个Feature还是蛮心动的，虽然看过RxJava，但是这个小巧灵活的库，对现有的代码库改造还是不错的，即使自己实现也比较靠谱。若想需要更多的操作，如：集合的操作等，还请移步RxJava。

这个库基本没有啥学习成本，下面介绍一些方法

{% highlight java %}
saveAsync(obj).continueWith(new Continuation<ParseObject, Void>() {
  public Void then(Task<ParseObject> task) throws Exception {
    if (task.isCancelled()) {
      // the save was cancelled.
    } else if (task.isFaulted()) {
      // the save failed.
      Exception error = task.getError()
    } else {
      // the object was saved successfully.
      ParseObject object = task.getResult();
    }
    return null;
  }
});
{% endhighlight %}

成功
{% highlight java %}
saveAsync(obj).onSuccess(new Continuation<ParseObject, Void>() {
  public Void then(Task<ParseObject> task) throws Exception {
    // the object was saved successfully.
    return null;
  }
});
{% endhighlight %}

奇怪的是没有提供失败这个接口，那遇到失败怎么做？
{% highlight cpp %}
saveAsync(obj).continueWith(new Continuation<ParseObject, Void>() {
  public Void then(Task<ParseObject> task) throws Exception {
    if (task.isFaulted()) {
      // todo process error
      return null;
    }
    return null;
  }
});
{% endhighlight %}
只要continueWith一个task，判断状态，

如何取消一个Task？
看接口
{% highlight cpp %}
public <TContinuationResult> Task<TContinuationResult> continueWith(
      final Continuation<TResult, TContinuationResult> continuation, final Executor executor,
      final CancellationToken ct)
{% endhighlight %}
所以我们只要弄一个`CancellationToken` 然后Cancel就行了.
同样还可以用 `CancellationTokenSource` 进行取消
在某一个task内部抛出预定义的异常同样也可以进行错误和取消处理.


有了上述的针对于单个Task的处理也有对应的多个Task的接口

{% highlight cpp %}
public static Task<Task<?>> whenAny(Collection<? extends Task<?>> tasks)
{% endhighlight %}
vs
{% highlight cpp %}
public static Task<Void> whenAll(Collection<? extends Task<?>> tasks)
{% endhighlight %}
对一堆task有需求的，这操作起来与单个的Task毫无差异。


## 线程切换
上面已经可以看出，能够将现有的Task进行一次级联了，但是缺少线程切换部分，Bolts是如何做的呢？
Bolts是通过给`Continuation`制定`ExecutorService`的形式进行线程切换的，非常灵活。

示例请狂点[这里](https://github.com/BoltsFramework/Bolts-Android/blob/master/BoltsTest/src/bolts/TaskTest.java)

扯了一堆均是 API 文档上的，也没啥干货，只是想说，这个轻量级的库提供的几个接口给足了理由抛弃掉AsyncTask，提供更好的代码模块化，让整个业务相关的代码放在一起。只要定义一堆`ExecutorService`后,也减少了是个人都`new Thread`的问题.

ps:若觉得这个提供的东西太少,可以参考`RxJava`这个比较重量级,也需要团队成员进行一下学习.


[1](http://blog.parse.com/announcements/lets-bolt/)
[2](http://blog.parse.com/learn/introducing-bolts-for-parse-sdks)