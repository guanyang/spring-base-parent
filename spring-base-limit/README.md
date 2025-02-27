## spring-base-limit

### 概览
- 当前组件主要用于应用访问频率限制，添加注解`LimitCheck`即可快速接入使用
- 默认支持基于`redis`实现的频率访问控制，需要应用配置`StringRedisTemplate`实例
- 支持SPI方式扩展实现，接口类：`org.gy.framework.limit.core.ILimitCheckService`
- 限流key支持多种解析器，也可以自定义实现`LimitKeyResolver`即可
  - `ExpressionLimitKeyResolver.class`: 默认解析器，基于Spel表达式实现
  - `GlobalLimitKeyResolver.class`: 全局级别限流Key解析器
  - `ClientIpLimitKeyResolver.class`: 客户端IP级别限流key解析器
  - `ServerNodeLimitKeyResolver.class`: 服务器节点级别限流key解析器
- 限流算法说明
  - TIME_WINDOW: 时间窗口限流(即固定窗口)，偏向控制请求数量，如果请求数量超过限制，则限流
  - TOKEN_BUCKET: 令牌桶限流，偏向控制请求速率，如果QPS超过限制，则限流
  - SLIDING_WINDOW: 滑动窗口限流，可以解决固定窗口存在的流量突刺问题，平滑的处理请求，限流的效果和你的滑动单位有关
- 限流支持自定义降级行为
  - `fallback`: 降级函数，支持当前bean和其他bean两种方式，支持原参数+`LimitException`和原参数两种方式
  - `fallbackBean`: 降级函数所在的bean，默认为当前bean，支持其他bean

### 使用说明
1. 需要在启动类添加`@EnableLimitCheck`注解
    ```java
    //默认基于redis，如果应用配置多个StringRedisTemplate，需要配置redisTemplateName属性指定bean
    //如果不想基于redis实现，可以不配置redisTemplateName属性
    @EnableLimitCheck(redisTemplateName = "stringRedisTemplate")
    public class DemoApplication  {
    }
    ```
2. 注解调用示例如下，默认Spel表达式解析器，可配置其他key解析器，定义`keyResolver`即可
    ```java
    //当前示例场景：默认TIME_WINDOW时间窗口限流，300秒只能调用1次
    @LimitCheck(key = "'GY:LIMIT:TEST:' + #user.name", limit = 1, time = 300)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
   
    //当前示例场景：TOKEN_BUCKET令牌桶限流，最大并发数为100，QPS限制为50
    @LimitCheck(key = "'GY:LIMIT:TEST:' + #user.name", limit = 50, capacity = 100, typeEnum = LimitTypeEnum.TOKEN_BUCKET)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
   
    //当前示例场景：SLIDING_WINDOW滑动窗口限流，每秒500次
    @LimitCheck(key = "'GY:LIMIT:TEST:' + #user.name", limit = 500, time = 1, typeEnum = LimitTypeEnum.SLIDING_WINDOW)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
    ```
3. 限流降级行为，默认会抛出`LimitException`异常，可以针对此异常自定义返回信息，也可自定义降级函数`fallback`，优先级高于异常处理
- 降级函数在当前bean
    ```java
    //降级函数fallback定义示例
    @LimitCheck(key = "'GY:LIMIT:TEST:' + #user.name", limit = 1, time = 300, fallback = "fallbackMethod1")
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
   
    //场景一：降级fallback方法带原参数+LimitException
    public void fallbackMethod1(User user, LimitException e){
        System.out.println("fallback------------>>>>>>>>"+user);
    }
   
    //场景二：降级fallback方法带原参数，无LimitException
    public void fallbackMethod2(User user){
        System.out.println("fallback------------>>>>>>>>"+user);
    }
    ```
- 降级函数在其他bean
    ```java
    //降级函数fallback和fallbackBean定义示例
    @LimitCheck(key = "'GY:LIMIT:TEST:' + #user.name", limit = 1, time = 300, fallback = "fallbackMethod3", fallbackBean = FallbackHandler.class)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
    
    @Component
    public class FallbackHandler  {
        //场景三：降级fallback方法带原参数+LimitException
        public void fallbackMethod3(User user, LimitException e){
            System.out.println("fallback------------>>>>>>>>"+user);
        }
        
        //场景四：降级fallback方法带原参数，无LimitException
        public void fallbackMethod4(User user){
            System.out.println("fallback------------>>>>>>>>"+user);
        }
    }
    ```

5. 动态设置限流大小
- `limitExpression`：限制数量表达式，支持SpEL或${spring.xxx}，优先级高于limit
- `capacityExpression`：令牌桶容量表达式，支持SpEL或${spring.xxx}，优先级高于capacity
    ```java
    //表达式配置示例
    @LimitCheck(key = "'GY:LIMIT:TEST:' + #user.name", limitExpression = "${limitCheck.limit:1}", time = 300, fallback = "fallbackMethod3", fallbackBean = FallbackHandler.class)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
    ```
