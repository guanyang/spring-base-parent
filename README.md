### spring-base-parent

#### 概览
- 依赖规范，统一的父pom，三方依赖库、版本管理
- 常用组件、工具类封装，方便代码复用

#### 最新maven坐标
```
<dependency>
  <groupId>org.gy.framework</groupId>
  <artifactId>spring-base-parent</artifactId>
  <version>1.0.1-SNAPSHOT</version>
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
```
初试版本
```

#### 1.0.1-SNAPSHOT
```
1. 添加Filter和FilterChain支持
2. 完善Exception类型及ExceptionHandlerI处理
3. 模块【spring-base-util】添加FileFilter过滤清洗，防止文件上传违规内容
  - 支持常见图片文件头尾字节码检查，防止恶意篡改文件扩展名上传，例如：jpg、png、gif、bmp
  - 支持图片文件字节码流内容清洗，剔除夹带恶意代码
4. 模块【spring-base-core】添加statemachine状态机，支持Fluent API调用
5. 模块【spring-base-core】添加SPI扩展工厂，方便扩展定义实现
```