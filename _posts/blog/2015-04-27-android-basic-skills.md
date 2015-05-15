---
layout: post
title: android 基本技能 
description: 常见的一些开发技巧的总结。 
category: blog
---


[android resource](https://github.com/wasabeef/awesome-android-ui)

## blur fragment 或者blur的实现
blur fragment的实现与windows gdi的半透明实现原理是一样的，都是将背后的背景抠出来然后在这基础之上做效果，blur、半透明、灰度等。
blur的源码就网上搜了。


## pullRefresh 
这其实是一个ViewGroup，主要的几个点在：
onTouchEvent、onMeasure、layout、onDraw等

onTouchEvent是确定是否显示下拉更新的drawable。
onMeasure是确定当前下拉更新的drawable占用了一块空白后，其他child应当顺应往下移动一部分。onDraw等就只是绘制了。
期间drawable的in、out动画、过程动画（如更新的菊花）等等。

##easing function
动画曲线，就是一坨函数了，由于不同函数的不同阶段斜率不同，震动不同，最后落地到动画的行为也就不同了。。。。

动画主要由Animator、Interpolator，Evaluator，
Interpolator是确定时间线，
TypeEvaluator确定值。


## view朝向检测
对于大多数ListView都是垂直朝向的，但是总有的时候可能是水平的,需要判断下,
`OrientationHelper` 这个辅助函数能够省去一坨水平还是垂直的判断，并作出不同的判断方式。

## pull refresh
这个用的挺多的，原理也比较简单，参照了几个库的实现，均是采用继承自ViewGroup作为rootView，在自定义的ViewGroup中进行pull refresh or release refresh等。
实现方法： 主要重载onMeasure、onLayout及onDispatchTouchEvent几个函数，在每次touch move的时候，记录下当前顶部的位置，在measure及onlayout的时候使用，主要在onlayout时使用，因为在onlayout的时候负责每个view的位置。然后在event up的时候采取动作 或者 在 move到一定距离后采取 refresh。 这里的refresh就是 一个 回调了。

这个时代有一个概念，顺滑如狗， 所以在scroll的过程中， 添加一个Scroller到这个里面来，会增加顺滑度。

还有一个需要注意的就是，尽可能的在xml里面实现pull refresh的attribute。  如header view， pull refresh ， release refresh ， pull ratio等。

## eventbus
eventbus 用的还算比较多，所以就想看看里面到底怎么实现的？
看到真相的我也是泪牛满面，实现简单粗暴，还是有挺多值得修改的，原理就是每次register的时候，通过反射去找与eventbus定义的规则一致，然后确定到底是async？normal、background等method。然后放到一个大大的集合里面，每次post的时候，就去找这个事件类型都有谁要了？然后根据上面提到的method，one by one的send过去。