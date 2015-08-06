---
layout: post
title: 宏的妙用
description: 少见的宏的用法，针对特定情况，还特别有用。
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/desk.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/drum-rudiment.jpeg
tags: c++ macro
---

尽管宏在C++中名声不怎么好，但是有时候使用起宏来还是挺方便直观的，一些小技巧能够省下许多代码篇幅也是很爽的。

 

背景

有时候需要定义一些枚举和这些枚举对应的字符串，如：错误代码及错误描述，经常我们按照如下方式进行

复制代码
```cpp
enum ErrorCode {
    kOK,
    kFailed,
    kUnkown,
    kFoo,
    kBar,
};

const char* error_details_ [] = {
    "kOk",
    "kFailed",
    "kUnkown",
    "kFoo",
    "kBar",
};
```
复制代码
当错误代码数量不是很大时，这种维护可能还没问题，当遇到枚举的值发生变更及修改调整等。可能就需要付出惨烈的代价了，因为量大，所以修改起来比较麻烦，还有位置ID的对应等，于是产生了下面一种升级描述方式。

复制代码
```cpp
struct ErrorDescrible {
    int error_code;
    const char* describle;
} error_details_[] = {
    {kOK, "kOK"},
    {kFailed, "kFailed"},
    {kUnkown, "kUnkown"},
    {kFoo, "kFoo"},
    {kBar, "kBar"},
};
```
复制代码
这种方式呢，针对于枚举的值进行任何的变更都将没太大的影响，不再受枚举的值得约束了，同样修改字符串也变得比较直观了，没那么多束手束脚的内容。

但是问题又来了，假如有100个这种类似的枚举、枚举值及字符串，枚举就得写两次，明显效率低下有没有什么更好的改进方法呢？既能省下重复的代码行，又能完成目前这种简单的需求？

这里呢可以使用宏对这些重复的代码进行扩展。

先上代码看看：

复制代码
```cpp
#define ERROR_EXPAND(MAP)\
    MAP(OK, 1, "OK")\
    MAP(Failed, 2, "Failed")\
    MAP(Unkown, 3, "Unkown")\
    MAP(Foo, 4, "Foo")\
    MAP(Bar, 5, "Bar")\

enum NewErrorCode {
#define MAP(code, value, describle) k##code = value,
    ERROR_EXPAND(MAP)
#undef MAP
};

struct ErrorDescrible {
    int error_code;
    const char* describle;
} error_details_[] = {
#define MAP(code, value, describle) {k##code, describle},
    ERROR_EXPAND(MAP)
#undef MAP
};
```
复制代码
完成的功能基本一样，而且还扩展了枚举的值不在是从0开始了，可以说是任意的了。有了这个这个妈妈再也不怕我重复写这块代码了。简洁直观，任意修改，调动。

 

看懂了之后其实很简单，就是通过宏扩展将宏一行一行的展开，根据需要挑选出本结构关心的字段，然后拼凑成一个简要的代码，这样就不会再多出写重复的代码。

 

原理分析：

将ERROR_EXPAND看成一个元组或者数组的话，map就是应用在其上的一个修士，将现有的内容转换成我想要的。通过不同的MAP宏扩展成不同的内容，完成不同的功能，

 

当与枚举关联两三个时，优势比较明显，但是若枚举扩展时变换太多，会导致代码不那么直观，也无法调试。

 

 

pros：

     减少枚举的重复，使代码读起来更轻松。

 

cons：

     缺点就是当不熟悉这个枚举时，或者不熟悉如何展开时，对许多宏的展开还是略有点茫然。

 

参考：

  libuv的源码。