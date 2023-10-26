### spring-base-parent

#### 概览
- 依赖规范，统一的父pom，三方依赖库、版本管理
- 常用组件、工具类封装，方便代码复用

#### 最新maven坐标
```xml
<dependency>
  <groupId>org.gy.framework</groupId>
  <artifactId>spring-base-parent</artifactId>
  <version>1.0.3-SNAPSHOT</version>
</dependency>
```

#### 内容介绍

| 模块(artifactId)    | 说明          | 备注                                  |
|-------------------|-------------|-------------------------------------|
| spring-base-core  | 基础核心定义      | [参考文档](spring-base-core/README.md)  |
| spring-base-util  | 常用工具类合集     | [参考文档](spring-base-util/README.md)  |
| spring-base-csrf  | csrf组件      | [参考文档](spring-base-csrf/README.md)  |
| spring-base-sign  | 接口签名组件      | [参考文档](spring-base-sign/README.md)  |
| spring-base-xss   | 接口参数xss校验组件 | [参考文档](spring-base-xss/README.md)   |
| spring-base-lock  | 分布式锁组件      | [参考文档](spring-base-lock/README.md)  |
| spring-base-log   | 日志组件        | [参考文档](spring-base-log/README.md)   |
| spring-base-limit | 限流组件        | [参考文档](spring-base-limit/README.md) |

#### Change Log
##### 1.0.0-SNAPSHOT
- 初始版本

#### 1.0.1-SNAPSHOT
- 添加Filter和FilterChain支持
- 完善Exception类型及ExceptionHandlerI处理
- 模块【spring-base-util】添加FileFilter过滤清洗，防止文件上传违规内容
  - 支持常见图片文件头尾字节码检查，防止恶意篡改文件扩展名上传，例如：jpg、png、gif、bmp
  - 支持图片文件字节码流内容清洗，剔除夹带恶意代码
- 模块【spring-base-core】添加statemachine状态机，支持Fluent API调用
- 模块【spring-base-core】添加SPI扩展工厂，方便扩展定义实现


#### 1.0.2-SNAPSHOT
- 升级框架依赖组件，解决安全漏洞
- 添加【spring-base-limit】全局限流模块，默认实现`Redis`限流，支持SPI扩展
- 优化【spring-base-lock】模块代码
  - 优化锁调用逻辑
  - 优化Spel表达式支持能力
- 优化【spring-base-sign】模块代码
  - 优化签名及验签逻辑
  - 增加验签时间戳偏移校验，增加时间戳偏移自定义能力

#### 1.0.3-SNAPSHOT
- 升级框架依赖组件
- 模块【spring-base-util】能力增强
  - 添加`DataLoadUtils`工具，实现数据分批加载，避免单次处理数据过大，同时支持`CompletableFuture`多线程任务执行处理
- 优化【spring-base-sign】模块代码
- 优化【spring-base-core】模块SPI代码