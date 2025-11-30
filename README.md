# Android 搜索局域网下的所有网络打印机,三种打印方式 1，打印照片,2，打印自定义pdf文档3，自定义MedicalReportView(清晰度超级高!推荐使用此方式！！！)。

##      特意花时间写了个Demo开源出来，希望能帮助到需要的人！

####    如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q。

####    如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q。

####    如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q，3Q。



* 官方打印图片链接地址:
* //打印图片
* https://developer.android.google.cn/training/printing/photos?hl=zh-cn
* //打印自定义文档链接地址:
* https://developer.android.google.cn/training/printing/custom-docs?hl=zh-cn
 
## 总结：同一个局域网下,搜索wifi下的网络打印机,请使用自定义MedicalReportView画报告图，然后预览和打印报告，因为不仅更高效分辨率最清晰
## 总结：同一个局域网下,搜索wifi下的网络打印机,请使用自定义MedicalReportView画报告图，然后预览和打印报告，因为不仅更高效分辨率最清晰
## 总结：同一个局域网下,搜索wifi下的网络打印机,请使用自定义MedicalReportView画报告图，然后预览和打印报告，因为不仅更高效分辨率最清晰

## 常见问题解决方案 

#### 问题一：同一个局域网之下，App搜索不到想要的打印机

*  我这边佳能G580打印机，赠送的驱动光盘是G500。一开始我这边局域网下搜索不出来佳能580的打印机。
*  但是当我PC电脑(同一个局域网下)安装了佳能G500的光盘驱动之后，我在同样的局域网下就可以搜索出来G580打印机。所以一些品牌的网络打印机搜索不出来，请问相关客服或者安装相对应的驱动即可。

#### 问题二：连接的佳能G580打印机之后，点击打印，打印机显示：后端托盘中装入的纸张尺寸或者类型与打印设置不同，需要点击打印机的OK按键才会开始打印报告
*  我咨询了佳能的客服，客服的回应是：取出机身全部纸张，将后托盘重新放纸，按机身屏幕提示重新注册纸张尺寸与类型，再尝试打印
*  按照客服的步骤操作解决了，每次点击打印的时候需要再去打印机点击OK(此时打印机显示：后端托盘中装入的纸张尺寸或者类型与打印设置不同)，才能打印的报告的问题







## 动图欣赏

* 如果看不到gif动图，请科学上网查看gif效果图，或者下载项目之后本地打开。在picture文件夹/gif文件夹/1.gif




![](picture/gif/1.gif) 


 ## 报告截图欣赏
 
 ## 报告截图,报告中红色和蓝色是我故意用颜色设置的,方便Demo查看 实际应用中可以取消掉的
 
 ![](report/PDF报告在SD卡的文件.png) ![](report/使用SD卡本地图片报告示例.jpg) ![](report/使用在线网络图的报告示例.jpg)
 ![](report/自定义View_使用网络图片_打印报告预留图2.jpg)![](report/自定义View_使用网络图片_打印报告预留图2.jpeg)

## 使用指南

 * 请直接，下载Demo查看，通俗易懂，谢谢。

#### 最新更新功能（自定义MedicalReportView画报告图，图片来源于网络图，推荐使用此方式），PrintDemoA4Activity，可以直接下载Demo查看，功能查看代码即可。
#### 最新更新功能（自定义MedicalReportView画报告图，图片来源于网络图，推荐使用此方式），PrintDemoA4Activity，可以直接下载Demo查看，功能查看代码即可。
#### 最新更新功能（自定义MedicalReportView画报告图，图片来源于网络图，推荐使用此方式），PrintDemoA4Activity，可以直接下载Demo查看，功能查看代码即可。
* MedicalReportView画报告图，然后预览打印即可
* 1，自定义View
* 2，PrintDemoA4Activity界面直接点击打印报告,进行预览,和打印报告操作
* 备注：画报告的同时加载网络图片新增贝赛尔曲线loading图，报告好之后，点击具体报告中的图片，支持双支放大和缩小查看，支持90度，180度旋转。
* 
* 效果请看下载好项目的路径：PrinterDemo\picture\report\自定义View_使用网络图片_打印报告预览图1.jpeg
* 效果请看下载好项目的路径：PrinterDemo\picture\report\自定义View_使用网络图片_打印报告预览图2.jpeg
* 
  
 #### 主要功能（A，打印照片），PrintPictureReportActivity，可以直接下载Demo查看，功能查看代码即可。
 
 #### 主要功能（B，跳转,打印自定义文档（报告中图片是SD卡本地文件）），PrintPdfReportLocalImageActivity，自定义文档打印其实是生成pdf文件，然后打印pdf文件，因为比较复杂，这边我详细讲解下具体流程。也可以直接下载Demo，功能查看代码即可。
 * 打印自定义文档流程(此处我是打印报告医用报告，当前报告的所有文字,图标,都会根据xml获取到，具体像素值的xy坐标值,以及left，right，top，bottom的值。然后把win像素值转换成A4纸张的像素值来确定具体位置画成pdf报告,代码里面有转换工具类,直接转换win像素到android像素)
 * 1，先读取报告中选中的,图片模板,解析图片模板xml数据
 * 2，解析病例模板数据(A4，A3，B5)，其中的一种
 * 3，生成pdf文件
 * 4，打印自定义文档(打印生成的pdf文件)

#### 主要功能（C，跳转,打印自定义文档（文档中图片是网络图片）），PrintPdfReportNetImageActivity，自定义文档打印其实是生成pdf文件，然后打印pdf文件，因为比较复杂，这边我详细讲解下具体流程。也可以直接下载Demo，功能查看代码即可。
 * 打印自定义PDF文档流程(此处我是打印报告医用报告，当前报告的所有文字,图标,都会根据xml获取到，具体像素值的xy坐标,以及left，right，top，bottom的值。然后把win像素值转换成A4纸张的像素值来确定具体位置画成pdf报告,,代码里面有转换工具类,直接转换win像素到android像素)
 * 1，备注，逻辑是一样的只是报告中的图片是加载在线网络图片，在加载的时候会具有返回值的线程，如此,等待执行完毕，就能获取到线程执行结果，我们再对pdf的对象做关闭流的操作，不然会闪退的哦~
 * 2，先读取报告中选中的,图片模板,解析图片模板xml数据
 * 3，解析病例模板数据(A4，A3，B5)，其中的一种
 * 4，生成pdf文件
 * 5，打印自定义文档(打印生成的pdf文件)




## License

```text
Copyright 2023 LoveLin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```