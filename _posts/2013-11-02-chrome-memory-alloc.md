---
layout: post
title: chrome在windows平台对malloc、alloc等的替换方式
description: 平时很少关注malloc和alloc等内存分配的hook，也许hook就是自己实现了一套内存分配的接口，这种方式不适合全局使用，若想全局使用还需要替换掉整个runtime的hook。
category: blog
backgrounds:
    - https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/desk.jpeg
thumb: https://dl.dropboxusercontent.com/u/18322837/cdn/Streetwill/thumbs/drum-rudiment.jpeg
---

## 背景

这里的标准内存申请释放指的是语言本身使用的malloc、free、calloc、new、delete等。

平台windows 7. 其他平台得绕道了- -！。

## 热身

使标准库的malloc、free等函数调用我们自己的实现通常有两种方法，一种，iat hook等形式，另外一种就是替换标准库为新实现。

第一种实现估计是老生常谈了，没啥新颖的，但是会杀毒软件等当为病毒等玩意儿干，狗拿耗子啊。

第二种方法，在没有看过chrome的allocator工程的具体实现之前或者说没看过prep_libc.py这个脚本以前，一直都没有这个概念，又一次感觉弱爆了。

技术要点

其实第二种说白了也很简单，实现就是通过visual studio 提供lib命令，将库里面的默认malloc等实现给移除。链接库的时候，使用新的libcmt.lib进行链接即可。由于是已经将malloc等函数给移除了，所以这时就需要自己实现一份就行了，然后链接，不会出现二义性。世界变得又美好了。

这份新实现的malloc等函数就可以任意的实现了（chrome中在base/allocator/allocator_shim.cc中）。如：chrome用tcmalloc等来替换原生的内存申请函数。至于tcmalloc的性能什么的自行google。

来一段内存申请的函数瞧瞧

```cpp
void* malloc(size_t size) __THROW {
  void* ptr;
  for (;;) {
#ifdef ENABLE_DYNAMIC_ALLOCATOR_SWITCHING
    switch ( allocator) {
      case JEMALLOC :
        ptr = je_malloc( size);
        break;
      case WINHEAP :
      case WINLFH :
        ptr = win_heap_malloc( size);
        break;
      case TCMALLOC :
      default:
        ptr = do_malloc( size);
        break;
    }
#else
    // TCMalloc case.
    ptr = do_malloc( size);
#endif
    if ( ptr)
      return ptr ;

    if (! new_mode || !call_new_handler (true))
      break;
  }
  return ptr;
}
```
就一堆宏了，说到底都交给tcmalloc去干苦力去，没啥好讲的。

在进入prep_libc.py脚本之前，来一段lib里面的内容看看，

从%VCInstallDir%VC\lib\libcmt.lib中导出关于malloc.obj部分的信息

```
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\mt_obj\malloc.obj
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\mt_obj\msize.obj
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\mt_obj\realloc.obj
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\mt_obj\recalloc.obj

\libcmtd.lib

f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\xmt_obj\malloc.obj
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\xmt_obj\msize.obj
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\xmt_obj\realloc.obj
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\xmt_obj\recalloc.obj
```
其余内存省略了。。


从chrome的allocator工程中导出的信息看一下
```cpp
..\..\build\Debug\obj\allocator\allocator_shim.obj
..\..\build\Debug\obj\allocator\jemalloc.obj
```
注意：

allocator工程的内存申请释放函数在allocator_shim.obj中。

好叻，现在的任务就是讲libcmtd.lib中的malloc.obj给移走。剩余部分不变，然后依赖上添加allocator工程即可。工程属性中添加上忽略libcmtd.lib。不忽略就会依赖默认的了- -！


大体思路已经屡清了。就是将%VCInstallDir%VC\lib\libcmtd.lib拷贝一份，拷贝内容的malloc.obj等内容是被移除的，然后依赖allocator库。去掉默认libcmtd的依赖。

上prep_libc.py

```python
#!/usr/bin/env python
# Usage: prep_libc.py <VCLibDir> <OutputDir> <arch>
#
# VCLibDir is the path where VC is installed, something like:
#    C:\Program Files\Microsoft Visual Studio 8\VC\lib
# OutputDir is the directory where the modified libcmt file should be stored.
# arch is either 'ia32' or 'x64'

import os
import shutil
import subprocess
import sys

def run(command, filter=None):
  """Run |command|, removing any lines that match |filter|. The filter is
  to remove the echoing of input filename that 'lib' does."""
  popen = subprocess.Popen(
      command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
  out, _ = popen.communicate()
  for line in out.splitlines():
    if filter and line.strip() != filter:
      print line
  return popen.returncode

def main():
  bindir = 'SELF_X86'
  objdir = 'INTEL'
  vs_install_dir = sys.argv[1]
  outdir = sys.argv[2]
  if "x64" in sys.argv[3]:
    bindir = 'SELF_64_amd64'
    objdir = 'amd64'
    vs_install_dir = os.path.join(vs_install_dir, 'amd64')
  output_lib = os.path.join(outdir, 'libcmt.lib')
  shutil.copyfile(os.path.join(vs_install_dir, 'libcmt.lib'), output_lib)
  shutil.copyfile(os.path.join(vs_install_dir, 'libcmt.pdb'),
                  os.path.join(outdir, 'libcmt.pdb'))

  vspaths = [
    'build\\intel\\mt_obj\\',
    'f:\\dd\\vctools\\crt_bld\\' + bindir + \
      '\\crt\\src\\build\\' + objdir + '\\mt_obj\\',
    'F:\\dd\\vctools\\crt_bld\\' + bindir + \
      '\\crt\\src\\build\\' + objdir + '\\mt_obj\\nativec\\\\',
    'F:\\dd\\vctools\\crt_bld\\' + bindir + \
      '\\crt\\src\\build\\' + objdir + '\\mt_obj\\nativecpp\\\\',
    'f:\\binaries\\Intermediate\\vctools\\crt_bld\\' + bindir + \
      '\\crt\\prebuild\\build\\INTEL\\mt_obj\\cpp_obj\\\\',
    ]

  objfiles = ['malloc', 'free', 'realloc', 'new', 'delete', 'new2', 'delete2',
              'align', 'msize', 'heapinit', 'expand', 'heapchk', 'heapwalk',
              'heapmin', 'sbheap', 'calloc', 'recalloc', 'calloc_impl',
              'new_mode', 'newopnt', 'newaopnt']
  for obj in objfiles:
    for vspath in vspaths:
      cmd = ('lib /nologo /ignore:4006,4014,4221 /remove:%s%s.obj %s' %
             (vspath, obj, output_lib))
      run(cmd, obj + '.obj')

if __name__ == "__main__":
  sys.exit(main())
```

内容不多，看vspaths就是构造一个路径，与平台及体系结构有关。

具体路劲参照这个

```cpp
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\mt_obj\malloc.obj
```

objfiles就是我们要替换的一些obj文件

最后就是调用lib命令来对库中的一些obj进行移除。完事。


大家可能还有一个小疑问就是，

```
f:\dd\vctools\crt_bld\SELF_X86\crt\src\build\INTEL\mt_obj\malloc.obj
```

这个路劲，基本上没有人的电脑上有这么一个路劲，那他到底是什么呢？或者平时调试标准库的时候，就会弹出一个这么样的路径，提示我们有错。问题是都没有这个路劲？

再想想，由于是静态库，只是将我们的obj组织在了一块，说明原始obj放到了这么一个路径，这些苦都是微软提供给我们的，so。这个路径是微软编译这些库的时候的路径。那我们为什么能够找到他的呢？神秘的pdb现身说法了。他知道一切。这就是为什么，上面的python脚本中，要连带pdb一块拷走了。


所以呢，下次看到这个dd啥的路劲的时候，就不惊讶了。


## 结论

通过这个allocator长了两个见识，

1.lib命令的使用，有了这个玩意儿，我至少能够知道lib里面放了点啥不至于两眼一抹黑。

2.标准库的new和delete的hook，用这玩意儿，明显舒畅多了。又不至于mfc那种，各种宏。