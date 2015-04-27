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
