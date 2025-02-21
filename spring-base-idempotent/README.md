## spring-base-idempotent

### 概览
1. 幂等逻辑是基于AOP+注解实现，需要在方法上添加`@Idempotent`注解
2. 幂等时间支持自定义，默认基于Redis+lua实现幂等，参考[spring-base-lock](../spring-base-lock/README.md)
3. `IdempotentAspect`切面支持扩展，重写`internalValidate`、`internalProceed`方法实现自定义幂等逻辑
4. 幂等key支持多种解析器，也可以自定义实现`IdempotentKeyResolver`即可
- `DefaultIdempotentKeyResolver.class`: 默认解析器，基于方法全量参数md5值实现
- `ExpressionIdempotentKeyResolver.class`: 基于Spel表达式实现

### 使用说明
1. maven坐标引入
    ```xml
    <dependency>
        <groupId>org.gy.framework</groupId>
        <artifactId>spring-base-idempotent</artifactId>
        <!--注意修改版本号-->
        <version>1.0.6-SNAPSHOT</version>
    </dependency>
    ```

2. 当前AOP切面默认不开启，如需开启，需要在启动类添加@EnableLockAspect注解
    ```java
    //默认基于redis，如果应用配置多个StringRedisTemplate，需要配置redisTemplateName属性指定bean
    //如果不想基于redis实现，可以不配置redisTemplateName属性
    @EnableIdempotent(redisTemplateName = "stringRedisTemplate")
    public class DemoApplication  {
    }
    ```
3. 注解调用示例如下，支持Spel表达式
   ```java
   @Idempotent(keyArg = "'GY:TEST:' + #user.name", timeout = 5, keyResolver = ExpressionIdempotentKeyResolver.class)
   public void test(User user){
       System.out.println("------------>>>>>>>>"+user);
   }
   ```
4. 如果校验不通过，会抛出`IdempotentException`异常，错误码为`429`，可以针对此异常自定义返回信息，自定义示例：
   ```java
   @ExceptionHandler(IdempotentException.class)
   public ResponseEntity<Void> handleException(HttpServletRequest request, IdempotentException e) {
     // 自定义异常处理
   }
   ```


