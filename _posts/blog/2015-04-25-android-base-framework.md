---
layout: post
title: android 道听途说
description: 翻看源码时看到的一些小信息，记录下来
category: blog
---


## ViewStub

关键点：初始化的一个View的Visibility为Gone，
调用了SetVisibility之后，会Inflate一个View出来，替换掉自己。
在Inflated完成之后，会通过接口通知Inflate的结果出去。

## window - windowManager
window是属于Activity的一个顶层窗口，负责Window该有元素的操作，如：rootView，Menu，然后通过Callback的形式将消息通知出去。
