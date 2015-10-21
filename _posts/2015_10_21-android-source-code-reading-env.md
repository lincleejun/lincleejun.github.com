---
layout: post
title: android 源码阅读环境搭建
description: 提供android studio 及 eclipse 阅读 源码的环境搭建.
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/desk.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/drum-rudiment.jpeg
---

# 背景

最近正准备系统的看一下 android 源码, 所以需要搭建一个舒服阅读源码的环境, 到目前为止,android studio还不支持NDK部分,也就是使用android studio 看 c++ 就基本废了.. 只能当文本阅读器看了. eclipse能够看 c++部分源码,但是eclipse整体的UI太丑及内容组织不爽,所以就分别利用两者的优点,在需要看Java部分时,使用android studio, 看c++部分时 使用 eclipse 各司其职.


# android studio 源码环境搭建.
1.若源码已经编译成功,则在源码的根目录执行 `development/tools/idegen/idegen.sh`
若发现提示 `idegen.jar` 等相关错误, 则可以考虑第二步,

2.对于一部分人可能只是希望看一下代码,而不想编译,这种方法适用该类人群.
下载[idegen.jar](http://jayfeng-files.stor.sinaapp.com/idegen.jar), 然后执行 `cp idegen.jar out/host/linux-x86/framework/idegen.jar`, 这里idegen.jar的路径是自己下载后idegen的路径.

若成功则会在根目录生成android.ipr 和 android.iml 两个文件.  用android studio 打开android.ipr就行了.

参考链接 [点我](http://www.cnblogs.com/qianxudetianxia/p/3721202.html)

# eclipse 源码环境.
由于android studio 对c++代码支持的很差,所以在看c++代码时,还是希望有跳转等.
所以再搭建一个eclipse的环境.下面是搭建的步骤

1.拷贝.classpath文件
.classpath文件是android提供的现成文件,但是需要我们将他拷贝到根目录来.
假设我们当前在android源码的根目录,执行:
`cp development/ide/eclipse/.classpath .`
由于eclipse会使用这个文件,并修改所以需要修改这个文件的权限.
`chmod u+w .classpath`

2.新建.project文件
这里还是假设在android源码的根目录.执行
```xml
<?xml version="1.0" encoding="UTF-8"?>  
<projectDescription>  
    <name>Android_OuyangPeng</name>  
    <comment></comment>  
    <projects>  
    </projects>  
    <buildSpec>  
        <buildCommand>  
            <name>org.eclipse.jdt.core.javabuilder</name>  
            <arguments>  
            </arguments>  
        </buildCommand>  
    </buildSpec>  
    <natures>  
        <nature>org.eclipse.jdt.core.javanature</nature>  
    </natures>  
</projectDescription>

```

最好在根目录撸一眼,是否都创建完成.

3.创建eclipse工程
这里需要创建一个空的 java 工程, 注意:是java空工程,别建错了,这样可能会打乱android源码的组织路劲,
建立新工程时, project name 就随意起一个看着舒服的, location 选择android的源码路劲,然后点击下一步,等一会儿,喝口水,最后点击finish就完事了...
就可以用elipse 看源码了.

参考链接 [点我](http://blog.csdn.net/ouyang_peng/article/details/10401585)

