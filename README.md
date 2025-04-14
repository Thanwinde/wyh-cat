# 一个仿照tomcat的低配山寨阉割版的tomcat：wyhCat

---

## 简介

​	wyhCat是一款基于Java开发的~~低效的，兼容性差的，优化欠佳的，功能较少的，无官方维护的，无生态的，不支持JSP的~~一个类tomcat服务器，它能部署所有按照servlet规范编写的WebApp，采用JDK 11。

​	采用HttpExchange来解析请求，手动将其转换为servlet规范并实现相关接口，利用URLClassesLoader来加载外来类

​	启动的话需要手动打成jar包，然后把对应的war文件/目录放在一起，然后执行

```
java -jar wyh-cat-1.0.jar -w Web.war
```

就可以启动了，Web.war是你对应的webapp

如果遇到中文乱码，windows请先在cmd输入**chcp 65001** 即可

实例程序位于webapp文件夹中

---

## 大致流程

​	思路很简单，就是利用JDK自己的Http实例来手动实现一个servlet框架，（你也可以自己实现Http实例，但是要处理TCP连接，握手等太过于复杂）

大致路线可以参照[简介 - 手写Tomcat - 廖雪峰的官方网站](https://liaoxuefeng.com/books/jerrymouse/introduction/index.html)的大致路线来看。

​	但是，这个教程极其简陋，许多重要的细节都在源码之中，光看哪点文字想要顺利做出来有点困难，建议对着源码边理解边写，配合AI注释。可以尝试看一下我的代码，我给每一个部分和重要的部分都加上了详细的注释。

​	手写tomcat肯定不简单，但也不是很难，最重要的是呢写完会对servlet有一个全新的认识。

具体来说的话：

- 学习servlet的层次与架构

- 用JDK自带的httpserver实现一个简单的Http实例，能实现一个hello world网页
- 然后尝试创建一个转换器，即继承了servlet规范又继承了HttpExchange的接口，把所有servlet的请求/响应接口全部用HttpExchange实现（servlet接口有很非常多，不用全部一下子实现，对着源码一个个实现，有些接口是不用实现的，因为用不到）
- 当你得到了servlet规范的请求，响应类之后，接下来就是servletContext的实现，意为servlet上下文，是servlet的心脏，管理并联系着servlet的所有组件
- 实现了servlet上下文后，得实现filter，listener，session，主要的难点就是在这，得反复品味源码才能知道如何、为什么能这样实现
- 最后就是从外部加载webapp，主要难点是对jvm如何创建类的过程要有一定理解，而且代码相对繁琐，经常出现不知名bug硬控你几个小时
- 这时候大致框架已然完成，你可以尝试加点东西进去，比如自定义filter顺序，实现缓冲池什么的

---

## 碎碎念

+ 最后外部加载时，java的类加载器貌似不能跨盘加载，得将解压目录设成与jar相同盘内
+ 在InputStream中的read方法，应该把byte转化成无符号int再返回，因为InputStream标记的是-1为终止符，java中byte范围是-128 - 128，而不是0 - 255，所以需要转成0-255形式（两种程序都能正常识别）
+ 不要CV，自己对着源码敲一遍，再尝试写一遍注释，你会有全新的理解
+ sendResopnseHeader只能最多调用一遍，注意
+ 最好完整的学习过网络，请求，servlet框架再来尝试实现
+ ~~tomcat真牛逼~~