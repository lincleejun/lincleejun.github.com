---
layout: post
title: android studio 源码调试路径
description: 解决android 在调试时，没法在sdk层打断点，或者sdk层的代码与文件不一致的问题。
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/desk.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/drum-rudiment.jpeg
---

#背景
 最近正在跟踪一个SwipeRefreshLayout的一个问题，由于我手机是小米的，所以没法看到sdk里面的内容，只有采用各种复写的形式，以及猜测，看问题，效率十分低下，然后就找一个编译了内核的哥们帮忙调一下！ 在他那儿，直接将断点甩在了ViewGroup里面看，我顿时震惊了。。。所以立马跑回来自己编译一个，这提高效率的事怎么能不干呢？

 然后就出现了几个问题。
 1.我的开发机是一台mac，但我并不是很想用mac来编译android内核，一方面编译时间长，另外一方面，磁盘空间也严重短缺（穷屌丝）。。。
 2.若是用ubuntu编译，然后再mac上调试可以嘛？

 本来第一个就不是问题，第二个得到了肯定的解答后，就立马实施了。写下来，防止下一次在需要搭建类似的环境。。直接扔出来就行。


 # 编译android
 目前最新的系统是 6， 棉花糖，但是我的N5 不想用6， 还是用5吧，所以果断clone了一个android-5.1.1-rc14版本。
 搭建环境就按照官网一步一步的来。
 由于该部分不是这里的重点，所以这儿就不描述了。
 完成之后，就烧到手机上。 


在mac上拷贝一份同样版本的android源码，觉得从官网下载源码的速度慢的话，就将ubuntu上的源码拷贝过来，扔掉out、.repo目录，也不是很大。  注意一定要同样版本的源码。。

 高潮部分来了。先下载 [FileTool](./FileTool.java) ，然后修改FileTool里面的文件路径为你的android源码路径。
 使用java执行以下，这个普通的java程序。 并重定向结果到一个文件里面去，  


 mac 上打开 ` ~Library/Preferences/AndroidStudio1.3/options/jdk.table.xml ` 将所有的 `sourcepath`的内容替换为刚刚生成的那个文件。 重启android studio， 让android studio 索引完成调试就可以了。