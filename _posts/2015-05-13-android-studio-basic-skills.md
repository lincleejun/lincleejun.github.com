---
layout: post
title: android studio or gradle 使用技能
description: 基本技能 
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/freezing.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/peak.jpeg
---

android studio 自己定义了一套格式aar， 在项目中如何生成这种格式呢？
其实很简单，打开工程的 `build.gradle` 文件，最顶上若有 `apply plugin: 'com.android.application'` or `apply plugin: 'com.android.library'`等字样的，就表明生成对应的内容，若为application，则生成apk，若为library则生成aar，  若没有上面对应字样的，加上即可生成想要的数据格式。

