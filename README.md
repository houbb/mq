# mq

[mq](https://github.com/houbb/mq) 是基于 netty 实现的 java mq 框架，类似于 dubbo。

[![Build Status](https://travis-ci.com/houbb/mq.svg?branch=master)](https://travis-ci.com/houbb/mq)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.houbb/mq/badge.svg)](http://mvnrepository.com/artifact/com.github.houbb/mq)
[![](https://img.shields.io/badge/license-Apache2-FF0080.svg)](https://github.com/houbb/mq/blob/master/LICENSE.txt)
[![Open Source Love](https://badges.frapsoft.com/os/v2/open-source.svg?v=103)](https://github.com/houbb/nlp-common)

> [变更日志](https://github.com/houbb/mq/blob/master/CHANGELOG.md)

主要用于个人学习，由渐入深，理解 mq 的底层实现原理。

## 特性

- 基于 netty4 的客户端调用服务端

- p2p 调用

- serial 序列化支持

- timeout 超时处理

- register center 注册中心

- load balance 负载均衡

- callType 支持 oneway sync 等调用方式

- fail 支持 failOver failFast 等失败处理策略

- generic 支持泛化调用

- gracefully 优雅关闭

- mqInterceptor 拦截器

- filter 过滤器

- check 客户端启动检测服务是否可用

- heartbeat 服务端心跳

# 快速入门

## maven 引入

```xml
<dependency>
    <groupId>com.github.houbb</groupId>
    <artifactId>mq-all</artifactId>
    <version>${mq.version}</version>
</dependency>
```

ps: 如果本地 p2p 测试，register 注册中心可选。

## 测试

### 注册中心

```java
RegisterBs.newInstance().start();
```

### 服务端

```java
ServiceBs.getInstance()
         .register(ServiceIdConst.CALC, new CalculatorServiceImpl())
         .registerCenter(ServiceIdConst.REGISTER_CENTER)
         .expose();
```

### 客户端

```java
// 服务配置信息
ReferenceConfig<CalculatorService> config = ClientBs.newInstance();
config.serviceId(ServiceIdConst.CALC);
config.serviceInterface(CalculatorService.class);
// 自动发现服务
config.subscribe(true);
config.registerCenter(ServiceIdConst.REGISTER_CENTER);
// 拦截器测试
config.mqInterceptor(new CostTimeInterceptor());

CalculatorService calculatorService = config.reference();
CalculateRequest request = new CalculateRequest();
request.setOne(10);
request.setTwo(20);

CalculateResponse response = calculatorService.sum(request);
System.out.println(response);
```

# 前言

工作至今，接触 mq 框架已经有很长时间。

但是对于其原理一直只是知道个大概，从来没有深入学习过。

以前一直想写，但由于各种原因被耽搁。

## 技术准备

[Java 并发实战学习](https://houbb.github.io/2019/01/18/jcip-00-overview)

[TCP/IP 协议学习笔记](https://houbb.github.io/2019/04/05/protocol-tcp-ip-01-overview-01)

[Netty 权威指南学习](https://houbb.github.io/2019/05/10/netty-definitive-gudie-00-overview)

这些技术的准备阶段，花费了比较长的时间。

也建议想写 mq 框架的有相关的知识储备。

其他 mq 框架使用的经验此处不再赘述。

## 快速迭代

原来一直想写 mq，却不行动的原因就是想的太多，做的太少。

想一下把全部写完，结果就是啥都没写。

所以本次的开发，每个代码分支做的事情实际很少，只做一个功能点。

陆陆续续经过近一个月的完善，对 mq 框架有了自己的体会和进一步的认知。

代码实现功能，主要参考 [Apache Dubbo](https://dubbo.apache.org/zh/docs/introduction/)

# 文档

## 文档

文档将使用 markdown 文本的形式，补充 code 层面没有的东西。

## 代码注释

代码有详细的注释，便于阅读和后期维护。

## 测试

目前测试代码算不上完善。后续将陆续补全。

# mq 模块

| 模块 | 说明 |
|:---|:---|
| mq-common | 公共代码 |
| mq-broker | 注册中心 |
| mq-producer | 服务端 |
| mq-consumer | 客户端 |
| mq-all | 全部引用模块（简化包引用） |

# 代码分支

[release_0.0.1-server 服务端启动](https://github.com/houbb/mq/tree/release_0.0.1)

[release_0.0.2-client 客户端启动](https://github.com/houbb/mq/tree/release_0.0.2)

[release_0.0.3-客户端调用服务端](https://github.com/houbb/mq/tree/release_0.0.3)

[release_0.0.4-p2p 客户端主动调用服务端](https://github.com/houbb/mq/tree/release_0.0.4)

[release_0.0.5-serial 序列化](https://github.com/houbb/mq/tree/release_0.0.5)

[release_0.0.6-通用的反射调用](https://github.com/houbb/mq/tree/release_0.0.6)

[release_0.0.7-timeout 超时处理](https://github.com/houbb/mq/tree/release_0.0.7)

[release_0.0.8-register 注册中心](https://github.com/houbb/mq/tree/release_0.0.8)

[release_0.0.9-load balance 负载均衡](https://github.com/houbb/mq/tree/release_0.0.9)

[release_0.1.0-callType 调用方式](https://github.com/houbb/mq/tree/release_0.1.0)

[release_0.1.1-fail 失败策略](https://github.com/houbb/mq/tree/release_0.1.1)

[release_0.1.2-generic 泛化调用](https://github.com/houbb/mq/tree/release_0.1.2)

[release_0.1.3-gracefully 优雅关闭](https://github.com/houbb/mq/tree/release_0.1.3)

[release_0.1.4-mqInterceptor 拦截器](https://github.com/houbb/mq/tree/release_0.1.4)

# 文档说明

[0.0.1-server 服务端启动](https://github.com/houbb/mq/blob/master/doc/dev/0.0.1-server%20服务端启动.md)

[0.0.2-client 客户端启动](https://github.com/houbb/mq/blob/master/doc/dev/0.0.2-client%20客户端启动.md)

[0.0.3-客户端调用服务端](https://github.com/houbb/mq/blob/master/doc/dev/0.0.3-客户端调用服务端.md)

[0.0.4-p2p 客户端主动调用服务端](https://github.com/houbb/mq/blob/master/doc/dev/0.0.4-p2p客户端主动调用服务端.md)

[0.0.5-serial 序列化](https://github.com/houbb/mq/blob/master/doc/dev/0.0.5-serial序列化.md)

[0.0.6-通用反射调用](https://github.com/houbb/mq/blob/master/doc/dev/0.0.6-通用反射调用.md)

[0.0.7-timeout 超时处理](https://github.com/houbb/mq/blob/master/doc/dev/0.0.7-timeout超时处理.md)

[0.0.8-register 注册中心](https://github.com/houbb/mq/blob/master/doc/dev/0.0.8-register注册中心.md)

[0.0.9-load balance 负载均衡](https://github.com/houbb/mq/blob/master/doc/dev/0.0.9-load-balance-负载均衡.md)

[0.1.0-callType 调用方式](https://github.com/houbb/mq/blob/master/doc/dev/0.1.0-callType-调用方式.md)

[0.1.1-fail 失败策略](https://github.com/houbb/mq/blob/master/doc/dev/0.1.1-fail-失败策略.md)

[0.1.2-generic 泛化调用](https://github.com/houbb/mq/blob/master/doc/dev/0.1.2-generic-泛化调用.md)

[0.1.3-gracefully 优雅关闭](https://github.com/houbb/mq/blob/master/doc/dev/0.1.3-gracefully-优雅关闭.md)

[0.1.4-mqInterceptor 拦截器](https://github.com/houbb/mq/blob/master/doc/dev/0.1.4-mqInterceptor-拦截器.md)

# 测试代码

从 v0.0.6 及其之后，为了让代码保持纯净，将测试代码全部放在 mq-example。

每个测试代码和实现版本一一对应。

ps: 这部分测试代码可以关注公众号【老马啸西风】，后台回复【mq】领取。

![qrcode](qrcode.jpg)

# 后期 ROAD-MAP

- [ ] all 模块

- [ ] check broker 启动检测
  
- [ ] 关闭时通知 register center

- [ ] 优雅关闭添加超时设置
  
- [ ] heartbeat 心跳检测机制

- [ ] 完善 load-balance 实现
  
- [ ] 失败重试的拓展
  
尝试其他服务端

指定重试策略（sisyphus）

- [ ] broker springboot 实现

- [ ] dead message 死信队列

