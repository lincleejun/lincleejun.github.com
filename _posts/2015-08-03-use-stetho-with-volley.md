---
layout: post
title: stetho with volley 实践
description: 在已有基于volley的项目基础上，添加volley的支持。
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/freezing.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/peak.jpeg
---

对于stetho官方其实已经给了一个不错的解决方案，若现有项目是基于OkHttp的，可以直接参考官方文档，也有中译版，
若现有项目是基于volley的，你就会发现其实还是需要自己去做不少事，才能完整的接入。
为什么强调是volley呢？因为其他几个模块，如：`elements`,`Resource`等，简单接入就能看到了，而网络模块，
啥都搞不到手，而接入这个库的很大一个目的就是为了使用Network部分，简单，方便。

所以接下来就简单谈谈如何在volley的基础上添加stetho。


1.添加`stetho`的依赖

```groovy
compile 'com.facebook.stetho:stetho:1.1.1'
compile 'com.facebook.stetho:stetho-okhttp:1.1.1'
compile 'com.squareup.okhttp:okhttp-urlconnection:2.4.0'
```
最后一个依赖是使用Volley需要的

2.接着在 Application 的OnCreate中添加

```Java
Stetho.initialize(
      Stetho.newInitializerBuilder(this)
        .enableDumpapp(
            Stetho.defaultDumperPluginsProvider(this))
        .enableWebKitInspector(
            Stetho.defaultInspectorModulesProvider(this))
        .build());
```

3.给 `OkHttp` 添加拦截器

```Java
    OkHttpClient client = new OkHttpClient();
    client.networkInterceptors().add(new StethoInterceptor());
    HttpStack stack = new OkHttpStack(client);
```

从前面三步看与使用 OkHttp 是一样的，接下来就不同了。


不同在于 `OkHttpStack` 这个类并不是Stetho 给我们提供的，而是需要自己撸。。

在网上一搜， 看到 [Jake Wharton](https://gist.github.com/JakeWharton/5616899)大神给了一段代码，
然后呢兴高采烈的撸过来，发现并不work。。。。

失落的我呢，就继续在网上搜，
发现有这么一个帖子 [Network inspection not working with OkUrlFactory](https://github.com/facebook/stetho/issues/43)，
不得不感叹，社区力量就是打呀！就说若你走HttpUrlConnection呢，前面设置的拦截器就没用了，就需要自己去写一大堆逻辑。。。
看完这段的我，整个人都不好了！

不过那哥们后面附了一段他给的[gist](https://gist.github.com/bryanstern/4e8f1cb5a8e14c202750)代码，
将其代码撸过来，可能需要删除与对不需要的Request Method， 然后问题搞定！！！



Tips：

添加了Stetho库之后，可能会遇到

```
    LICENSE.txt
    NOTICE.txt
```

这两个错误，

在gradle 中添加如下代码即可

```
    packagingOptions {
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
    }
```


结论：

其实没啥结论啦，就感觉Volley是后娘养得，需要自己去处理一坨东西，麻烦。。。。

写点略搓，希望下次有机会在用到的情况下，能省点力气。。。


