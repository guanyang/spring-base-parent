### spring-base-parent

#### 概览
- 依赖规范，统一的父pom，三方依赖库、版本管理
- 常用组件、工具类封装，方便代码复用

#### 最新maven坐标
- [Maven central repository坐标](https://central.sonatype.com/artifact/io.github.guanyang/spring-base-parent)
```xml
<!--添加依赖管理-->
<dependencyManagement>
  <dependency>
    <groupId>io.github.guanyang</groupId>
    <artifactId>spring-base-parent</artifactId>
    <version>2.0.5</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
</dependencyManagement>
<!--添加具体依赖示例-->
<dependencies>
  <dependency>
    <groupId>io.github.guanyang</groupId>
    <artifactId>spring-base-core</artifactId>
  </dependency>
</dependencies>
```

#### 内容介绍

| 模块(artifactId)         | 说明          | 备注                                       |
|------------------------|-------------|------------------------------------------|
| spring-base-core       | 基础核心定义      | [参考文档](spring-base-core/README.md)       |
| spring-base-util       | 常用工具类合集     | [参考文档](spring-base-util/README.md)       |
| spring-base-csrf       | csrf组件      | [参考文档](spring-base-csrf/README.md)       |
| spring-base-sign       | 接口签名组件      | [参考文档](spring-base-sign/README.md)       |
| spring-base-xss        | 接口参数xss校验组件 | [参考文档](spring-base-xss/README.md)        |
| spring-base-lock       | 分布式锁组件      | [参考文档](spring-base-lock/README.md)       |
| spring-base-log        | 日志组件        | [参考文档](spring-base-log/README.md)        |
| spring-base-limit      | 限流组件        | [参考文档](spring-base-limit/README.md)      |
| spring-base-idempotent | 幂等组件        | [参考文档](spring-base-idempotent/README.md) |
| spring-base-mq         | 消息组件        | [参考文档](spring-base-mq/README.md)         |

#### Change Log
#### 2.0.5
- 优化【spring-base-lock】模块代码，减少资源占用
- 优化【spring-base-idempotent】模块代码，切换底层锁实现
- 优化【spring-base-xss】模块代码性能，去掉反射校验，采用json反序列化器实现

#### 2.0.4
- 添加springboot3 processor配置提示支持

#### 2.0.2
- 更新maven配置
- 更新workflow配置

#### 2.0.0-SNAPSHOT
- 调整项目groupId为`io.github.guanyang`
- 调整项目package为`io.github.guanyang`

#### 1.1.1-springboot3-SNAPSHOT
- 升级springboot版本到3.5.5
- 升级jdk版本到25

#### 1.1.0-SNAPSHOT
- 更新【spring-base-mq】模块代码
  - `Properties`标准化管理，添加IDE配置提示，提升配置体验
  - 添加Kafka消息队列支持


#### 1.0.9-SNAPSHOT
添加【spring-base-mq】模块代码，提供统一的消息处理框架，该组件具有以下特点：

1. 注解驱动：通过`@DynamicEventStrategy`和`@EnableMQ`注解简化消息处理配置
2. 事件驱动架构：基于`IEventType`和`IMessageType`接口实现灵活的事件和消息类型管理
3. AOP切面支持：通过切面实现事件处理的统一拦截和管理
4. 多消息队列支持：目前支持RocketMQ，后续方便扩展其他消息队列
5. 可扩展性：提供丰富的扩展点，支持自定义消息处理逻辑
6. 消息幂等支持：基于Redisson实现，需要添加redis相关配置
7. 事件日志记录：自定义`EventLogService`实现，可记录事件处理日志

#### 1.0.8-SNAPSHOT
- 优化【spring-base-lock】模块代码，分布式锁执行器支持自定义扩展，实现`LockExecutorResolver`接口
  - `RedissonLockExecutorResolver`: 基于Redisson的执行器，默认启用
  - `RedisLockExecutorResolver`: 基于原生Redis+lua的执行器

#### 1.0.7-SNAPSHOT
- 优化【spring-base-lock】模块代码
  - 支持自定义降级行为，定义`fallback`和`fallbackBean`
- 优化【spring-base-limit】模块代码
  - 支持`TIME_WINDOW`、`TOKEN_BUCKET`和`SLIDING_WINDOW`三种限流算法
  - 支持自定义降级行为，定义`fallback`和`fallbackBean`
  - 支持动态设置限流大小，支持`limitExpression`和`capacityExpression`
- 优化【spring-base-idempotent】模块代码
  - 支持自定义降级行为，定义`fallback`和`fallbackBean`

#### 1.0.6-SNAPSHOT
- 优化【spring-base-lock】模块代码
  - 基于`ScheduledExecutorService`实现锁续期，避免业务处理过长导致锁失效
- 优化【spring-base-limit】模块代码
  - 优化限流核心逻辑，支持多种限流key解析器，也可以自定义扩展

#### 1.0.5-SNAPSHOT
- 优化【spring-base-lock】模块代码
  - 优化锁调用核心逻辑及超时处理，支持毫秒级
- 添加【spring-base-idempotent】幂等组件模块，默认基于`Redis`实现幂等
- 优化全局统一版本号revision管理

#### 1.0.4-SNAPSHOT
- 升级框架依赖组件
- 优化【spring-base-core】模块代码
  - 优化`TraceUtils`工具，支持链路追踪自定义配置
  - 优化`IStdEnum`工具，优化枚举高效检索能力

#### 1.0.3-SNAPSHOT
- 升级框架依赖组件
- 模块【spring-base-util】能力增强
  - 添加`DataLoadUtils`工具，实现数据分批加载，避免单次处理数据过大，同时支持`CompletableFuture`多线程任务执行处理
- 优化【spring-base-sign】模块代码
- 优化【spring-base-core】模块SPI代码

#### 1.0.2-SNAPSHOT
- 升级框架依赖组件，解决安全漏洞
- 添加【spring-base-limit】全局限流模块，默认实现`Redis`限流，支持SPI扩展
- 优化【spring-base-lock】模块代码
  - 优化锁调用逻辑
  - 优化Spel表达式支持能力
- 优化【spring-base-sign】模块代码
  - 优化签名及验签逻辑
  - 增加验签时间戳偏移校验，增加时间戳偏移自定义能力

#### 1.0.1-SNAPSHOT
- 添加Filter和FilterChain支持
- 完善Exception类型及ExceptionHandlerI处理
- 模块【spring-base-util】添加FileFilter过滤清洗，防止文件上传违规内容
  - 支持常见图片文件头尾字节码检查，防止恶意篡改文件扩展名上传，例如：jpg、png、gif、bmp
  - 支持图片文件字节码流内容清洗，剔除夹带恶意代码
- 模块【spring-base-core】添加statemachine状态机，支持Fluent API调用
- 模块【spring-base-core】添加SPI扩展工厂，方便扩展定义实现

##### 1.0.0-SNAPSHOT
- 初始版本