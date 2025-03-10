## spring-base-lock

### 概览
- 分布式锁执行器支持自定义扩展，实现`LockExecutorResolver`接口
  - `RedissonLockExecutorResolver`: 基于Redisson的执行器，默认启用
  - `RedisLockExecutorResolver`: 基于原生Redis+lua的执行器
- 分布式锁key支持解析器扩展，可以自定义实现`LockKeyResolver`即可
  - `ExpressionLockKeyResolver.class`: 默认解析器，支持SpEL或${spring.xxx}
- 支持自定义降级行为
  - `fallback`: 降级函数，支持当前bean和其他bean两种方式，支持原参数+`DistributedLockException`和原参数两种方式
  - `fallbackBean`: 降级函数所在的bean，默认为当前bean，支持其他bean

### 使用说明
1. 当前分布式执行器默认启用Redisson实现，即`RedissonLockExecutorResolver`，可自定义
2. 支持手动调用和AOP注解两种方式实现
- AOP切面默认不开启，如需开启，需要在启动类添加@EnableLockAspect注解
```java
//默认基于redissonClient，如果应用配置多个redissonClient，需要配置redissonClientName属性指定bean
//如果不想基于redissonClient实现，可以不配置redissonClientName属性，redisTemplateName属性同理
@EnableLockAspect(redissonClientName = "redissonClient")
public class DemoApplication  {
}
```

- 手动调用入口：DistributedLockAction
```java
public void demo() {
    //定义分布式实现
    DistributedLock lock = new RedisDistributedLock(redisTemplate, lockKey, expireTime);
    //方法执行
    LockResult<Long> execute = DistributedLockAction.execute(lock, () -> {
        return System.currentTimeMillis();
    });
    System.out.println(result.getData());
}
```
- AOP入口类：DistributedLockAspect，注解调用示例如下，支持Spel表达式
```java
@Lock(key = "'GY:LOCK:TEST:' + #user.name")
public void test(User user){
    System.out.println("------------>>>>>>>>"+user);
}
```

3. 支持自定义降级行为，默认会抛出`DistributedLockException`异常，可以针对此异常自定义返回信息，也可自定义降级函数`fallback`，优先级高于异常处理
- 降级函数在当前bean
    ```java
    //降级函数fallback定义示例
    @Lock(key = "'GY:LOCK:TEST:' + #user.name", fallback = "fallbackMethod1")
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
   
    //场景一：降级fallback方法带原参数+DistributedLockException
    public void fallbackMethod1(User user, DistributedLockException e){
        System.out.println("fallback------------>>>>>>>>"+user);
    }
   
    //场景二：降级fallback方法带原参数，无DistributedLockException
    public void fallbackMethod2(User user){
        System.out.println("fallback------------>>>>>>>>"+user);
    }
    ```
- 降级函数在其他bean
    ```java
    //降级函数fallback和fallbackBean定义示例
    @Lock(key = "'GY:LOCK:TEST:' + #user.name", fallback = "fallbackMethod3", fallbackBean = FallbackHandler.class)
    public void test(User user){
        System.out.println("------------>>>>>>>>"+user);
    }
    
    @Component
    public class FallbackHandler  {
        //场景三：降级fallback方法带原参数+DistributedLockException
        public void fallbackMethod3(User user, DistributedLockException e){
            System.out.println("fallback------------>>>>>>>>"+user);
        }
        
        //场景四：降级fallback方法带原参数，无DistributedLockException
        public void fallbackMethod4(User user){
            System.out.println("fallback------------>>>>>>>>"+user);
        }
    }
    ```
### 获取锁方式
> 方法入口类：DistributedLockAction
- 仅尝试一次获取锁，没有获取到，则直接返回获取失败

```java 
/**
     * 功能描述：业务执行，包含加锁、释放锁(仅尝试一次获取锁)
     *
     * @param lock 分布式锁定义
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, DistributedLockCallback<T> runnable) {
        return execute(lock, 0, 0, runnable);
    }
``` 
- 一直尝试，直到获取成功

```java 
/**
     * 功能描述:业务执行，包含加锁、释放锁(一直尝试，直到获取成功)
     *
     * @param lock 分布式锁定义
     * @param sleepTimeMillis 睡眠重试时间，单位：毫秒
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, long sleepTimeMillis,
        DistributedLockCallback<T> runnable) {
        return execute(lock, Long.MAX_VALUE, sleepTimeMillis, runnable);
    } 
``` 
- 多次尝试获取锁，自定义超时时间

```java 
/**
     * 功能描述:业务执行，包含加锁、释放锁(多次尝试获取锁，自定义超时时间)
     *
     * @param lock 分布式锁定义
     * @param waitTimeMillis 等待超时时间，单位：毫秒
     * @param sleepTimeMillis 睡眠重试时间，单位：毫秒
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, long waitTimeMillis, long sleepTimeMillis,
        DistributedLockCallback<T> runnable) {
        boolean lockFlag = false;
        try {
            lockFlag = lock.tryLock(waitTimeMillis, sleepTimeMillis);
            if (!lockFlag) {
                return LockResult.wrapError();
            }
            T data = runnable.run();
            return LockResult.wrapSuccess(data);
        } finally {
            if (lockFlag) {
                lock.unlock();
            }
        }
    } 
``` 



