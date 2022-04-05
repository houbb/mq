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

- timeout 超时处理

- broker 启动的 check 检测服务可用性

- load balance 负载均衡

- 基于 TAG 的消息过滤，broker 端实现

- 生产者的消息同步发送，ONE WAY 发送

- fail 支持 failOver failFast 等失败处理策略

- heartbeat 服务端心跳

- AT LEAST ONCE 最少一次原则

# 快速入门

## maven 引入

```xml
<dependency>
    <groupId>com.github.houbb</groupId>
    <artifactId>mq-all</artifactId>
    <version>${mq.version}</version>
</dependency>
```

## 测试

### 注册中心

```java
MqBroker broker = new MqBroker();
broker.start();
```

### 消费者

```java
final MqConsumerPush mqConsumerPush = new MqConsumerPush();
mqConsumerPush.start();

mqConsumerPush.subscribe("TOPIC", "TAGA");
mqConsumerPush.registerListener(new IMqConsumerListener() {
    @Override
    public ConsumerStatus consumer(MqMessage mqMessage, IMqConsumerListenerContext context) {
        System.out.println("---------- 自定义 " + JSON.toJSONString(mqMessage));
        return ConsumerStatus.SUCCESS;
    }
});
```

### 生产者

```java
MqProducer mqProducer = new MqProducer();
mqProducer.start();

String message = "HELLO MQ!";
MqMessage mqMessage = new MqMessage();
mqMessage.setTopic("TOPIC");
mqMessage.setTags(Arrays.asList("TAGA", "TAGB"));
mqMessage.setPayload(message);

SendResult sendResult = mqProducer.send(mqMessage);
System.out.println(JSON.toJSON(sendResult));
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

# 测试代码

这部分测试代码可以关注公众号【老马啸西风】，后台回复【mq】领取。

![qrcode](qrcode.jpg)

# 后期 ROAD-MAP

- [ ] all 模块

- [x] check broker 启动检测
  
- [x] 关闭时通知 register center

- [x] 优雅关闭添加超时设置
  
- [ ] heartbeat 心跳检测机制

- [ ] 完善 load-balance 实现

基于 weight 的负载均衡

- [ ] 失败重试的拓展

fail-fast

fail-over
  
指定重试策略（sisyphus）

最大重试次数，重试间隔时间

- [ ] offline message 离线消息

- [ ] 消费者 pull 策略实现

- [ ] broker springboot 实现

- [ ] dead message 死信队列

- [ ] 顺序消息

基于 sharding key

- [ ] 消息的回溯消费

- [ ] 事务消息

- [ ] 定时消息

- [ ] 流量控制

- [ ] 消息可靠性


