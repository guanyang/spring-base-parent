## spring-base-idempotent

### 概览
1. 幂等逻辑是基于AOP+注解实现，需要在方法上添加`@Idempotent`注解
2. 幂等时间支持自定义，默认基于Redis+lua实现幂等，参考[spring-base-lock](../spring-base-lock/README.md)
3. 幂等key支持多种解析器，也可以自定义实现`IdempotentKeyResolver`即可
   - `DefaultIdempotentKeyResolver.class`: 默认解析器，基于方法全量参数md5值实现
   - `ExpressionIdempotentKeyResolver.class`: 表达式解析器，支持SpEL或${spring.xxx}
4. 支持自定义降级行为
   - `fallback`: 降级函数，支持当前bean和其他bean两种方式，支持原参数+`IdempotentException`和原参数两种方式
   - `fallbackBean`: 降级函数所在的bean，默认为当前bean，支持其他bean

### 使用说明
1. 当前AOP切面默认不开启，如需开启，需要在启动类添加@EnableLockAspect注解
    ```java
    //默认基于redis，如果应用配置多个StringRedisTemplate，需要配置redisTemplateName属性指定bean
    //如果不想基于redis实现，可以不配置redisTemplateName属性
    @EnableIdempotent(redisTemplateName = "stringRedisTemplate")
    public class DemoApplication  {
    }
    ```
2. 注解调用示例如下，支持Spel表达式
   ```java
   @Idempotent(key = "'GY:TEST:' + #user.name", timeout = 5, keyResolver = ExpressionIdempotentKeyResolver.class)
   public void test(User user){
       System.out.println("------------>>>>>>>>"+user);
   }
   ```
3. 支持自定义降级行为，默认会抛出`IdempotentException`异常，可以针对此异常自定义返回信息，也可自定义降级函数`fallback`，优先级高于异常处理
- 基于异常自定义返回信息
   ```java
   @ExceptionHandler(IdempotentException.class)
   public ResponseEntity<Void> handleException(HttpServletRequest request, IdempotentException e) {
     // 自定义异常处理
   }
   ```
- 降级函数在当前bean
    ```java
    //降级函数fallback定义示例
    @Idempotent(key = "'GY:TEST:' + #user.name", timeout = 5, keyResolver = ExpressionIdempotentKeyResolver.class, fallback = "fallbackMethod1")
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
   
    //场景一：降级fallback方法带原参数+IdempotentException
    public void fallbackMethod1(User user, IdempotentException e){
        System.out.println("fallback------------>>>>>>>>"+user);
    }
   
    //场景二：降级fallback方法带原参数，无IdempotentException
    public void fallbackMethod2(User user){
        System.out.println("fallback------------>>>>>>>>"+user);
    }
    ```
- 降级函数在其他bean
    ```java
    //降级函数fallback和fallbackBean定义示例
    @Idempotent(key = "'GY:TEST:' + #user.name", timeout = 5, keyResolver = ExpressionIdempotentKeyResolver.class, fallback = "fallbackMethod3", fallbackBean = FallbackHandler.class)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
    
    @Component
    public class FallbackHandler  {
        //场景三：降级fallback方法带原参数+IdempotentException
        public void fallbackMethod3(User user, IdempotentException e){
            System.out.println("fallback------------>>>>>>>>"+user);
        }
        
        //场景四：降级fallback方法带原参数，无IdempotentException
        public void fallbackMethod4(User user){
            System.out.println("fallback------------>>>>>>>>"+user);
        }
    }
    ```


