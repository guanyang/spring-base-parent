## spring-base-limit

### 简介
- 当前组件主要用于应用访问频率限制，添加注解`LimitCheck`即可快速接入使用
- 默认支持基于`redis`实现的频率访问控制，需要应用配置`StringRedisTemplate`实例
- 支持SPI方式扩展实现，接口类：`org.gy.framework.limit.core.ILimitCheckService`
- 限流key支持多种解析器，也可以自定义实现`LimitKeyResolver`即可
  - `ExpressionLimitKeyResolver.class`: 默认解析器，基于Spel表达式实现
  - `GlobalLimitKeyResolver.class`: 全局级别限流Key解析器
  - `ClientIpLimitKeyResolver.class`: 客户端IP级别限流key解析器
  - `ServerNodeLimitKeyResolver.class`: 服务器节点级别限流key解析器

### 使用说明
1. 需要在启动类添加`@EnableLimitCheck`注解
```
//如果应用配置多个StringRedisTemplate，需要注入指定bean，启动类添加如下注解，注意修改bean名称：
@EnableLimitCheck(redisTemplateName = "stringRedisTemplate")
```

2. 注解调用示例如下，支持Spel表达式
```
//当前示例场景：300秒只能调用1次
@LimitCheck(key = "'GY:LOCK:TEST:' + #user.name", limit = 1, time = 300)
public void test(User user){
    System.out.println("------------>>>>>>>>"+user);
}
```



