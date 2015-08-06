---
layout: post
title: gralde android应用时遇到的一些坑
description: 描述个人在为了加一个库时遇到的一系列的问题，目前可能这里面所提到的东西不够全面，只是点皮毛，希望逐渐将其完善
category: blog
---


## 背景
现有有一个辅助debug的库，希望在debug的情况下使用起来，帮助更友好的调试，而release得时候，这个库直接不可见，而且最好是不用更改现有的gradle installDebug、gradle installRelease的使用方式。于是有了以下的一对「坑爹」之旅。

## 解决方法

1.通过exclude folder的方法
大体思路，就是在debug的时候，编译某个folder下得内容，而release得时候就将这个目录下得内容给去掉，由于我还更改了Application的名字，还使用到了 placeHolder。

然后问题就来了，发现在build.gradle中，里面的config都是静态写死的，除了已经提供的变量是独立的外，自己定义的都是共享的数据，所以若我想debug包含某个目录，release 不包含，或者默认不包含，debug包含均是做不到的- -~~。
可能你看完上面那句话你不会信，堂堂一个gradle居然连这个feature都搞不定？还真比较难搞！！！因为debug、release都是固定写死了！你要去控制共享的sourceSets下面的java srcDirs 这下子就等于 多个buildtype下得动作只和！ 所以目的肯定是达不到了！

又想假如我能够得到当前的编译类型是谁？ 动态给他添加不就可以了吗？兴高采烈的我，果断去Google了一把，结果失望而归！没有现成的告诉你现在到底是编译哪一个，编译类型是谁？。。。这果断不靠谱呀！ 于是我又开始各种搜，在[stack overflow](http://stackoverflow.com/questions/25739163/how-to-get-current-buildtype-in-android-gradle-configuration)上搜到了一个帖子，告诉我如何能得到当前的buildtype，然后就将代码果断撸过来，运行起来，结果发现，获取的当前build type时机居然是编译完成后，这更不靠谱了！ 都编译完了，要你何用？   

于是找到还有doFirst之类的函数，也就是说，我能再这个task执行之前去执行一些我想要的动作，看起来还挺符合我的预期的！结果运行结果把我吓尿了！在这个task执行前还是没法拿到这个类型，因为执行的当前task是通过depends去执行的我拿current build type的task的。看起来又不靠谱了！

通过这个task的折腾呢，get到一个新技能就是task的执行分三个阶段，1.doFirst，这个时候没执行任何task，2.depends on的task及自身的task，3.doLast，这就扫尾了！


其实到这儿的时候，我已经快崩溃了！这屌的一个gradle居然这么简单的事都干不鸟！于是我就快速的撸了一遍gradle user guide。 最后发现俩小小的线索。

我自己写一个task，间接依赖「installDebug」，这样，在debug的时候，就调用这个task，去添加一些路劲，参数啥的，看起来也挺美好的！
于是就撸起来了，写完后 运行我的task，  奇妙的可以正常编译了，对于我来说太fancy了！！  于是果断尝试了下release，结果发现不对，我的task还是被执行了！！ 这时候我就当头一棒，纳尼，我都没让你执行，你居然要执行？ 我还以为我姿势错了！ 赶紧写一个了一个空得task，里面就只调用println就行了！发现debug release都会运行！！   看到这个的我整个人都不好了！ 还没见过定义函数没人调用就自己跑起来的！  不科学啊！

于是继续撸「gradle user guide」看到两个不错的点一个是给task 后面添加一个<<, 另外一个是gradle 命令执行的是调用-x 可以不执行某个任务.

那好,有了这些利器,就果断用起来，操刀上阵运行后发现「<<」操作符其实是doLast的简写！天哪噜，要不要这么忽悠人！！！就等于没用呗！

还好还有一根救命稻草，继续试， gradle -x 执行的后果也是血淋淋的！毛用都没用！！ 

中途还用过其他的各种姿势！！！！我的天呀！对于gradle 我算是服了！ 进入完全抓狂的节奏！！

还尝试过task.ready等看来很虚幻的姿势，但 都是然并卵啊！！！！



然后突然想到使用另外一招，不这样exclude目录，作为一个依赖库给现有的代码依赖，在debug的时候依赖一个debug的库，release的时候依赖一个empty库就行！这样呢，就需要稍微改一下现有的代码，经过我再次的确认代码后，改动倒不是很大， 还能接受！！

2.新建library，让现有的代码来依赖！
开始见library的时候也挺狗血的！我建立了一个library，结果各种编译不过， 说apply plugin "com.android.library"不存在，我天，受不鸟饿了！然后让隔壁坐小哥试试，是不是我IDE出问题了！结果他也出现了类似的问题！！！！！
类似的问题！！！，哎，前前后后弄了20~30min，也不知由于什么原因，捣鼓自然能编译了！！！ 到这个时候，其实还没写任何代码，只感觉满脑子的「草泥马」飘过！！！
剩下的撸代码的过程就没啥了！



conclusion：

总体上说，gradle感觉还是太坑了！installDebug等tasks没给机会去hack，一大堆看起来很简单，但压根做不尿的事，还有就是增强了对gradle 的编译流程认识，以及以后遇到类似的问题的时候，记得要用library的形式给现有的库进行依赖，而不是给现有的库做patch，导致自讨苦吃。

其实还是，不得不吐槽下gradle。。。。。。





