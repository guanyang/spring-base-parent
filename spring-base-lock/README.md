## spring-base-lock

### 配置说明
依赖StringRedisTemplate 如果springboot中没有配置StringRedisTemplate，则不可使用

### 使用说明
1. 当前分布式采用redis+lua实现，后续可以扩展其他实现方式
2. 支持手动调用和AOP注解两种方式实现
- AOP切面默认不开启，如需开启，需要在启动类添加@EnableLockAspect注解
```
//如果应用配置多个StringRedisTemplate，需要切面注入指定bean，启动类添加如下注解，注意修改bean名称：
@EnableLockAspect(redisTemplateName = "stringRedisTemplate")
```
- 手动调用入口：DistributedLockAction
```
//定义分布式实现
DistributedLock lock = new RedisDistributedLock(redisTemplate, lockKey, expireTime);
//方法执行
LockResult<Long> execute = DistributedLockAction.execute(lock, () -> {
    return System.currentTimeMillis();
});
Assert.assertTrue(execute.success());
```
- AOP入口类：DistributedLockAspect，注解调用示例如下，支持Spel表达式
```
@Lock(key = "'GY:LOCK:TEST:' + #user.name")
public void test(User user){
    System.out.println("------------>>>>>>>>"+user);
}
```
### 获取锁方式
> 方法入口类：DistributedLockAction
- 仅尝试一次获取锁，没有获取到，则直接返回获取失败

``` 
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

``` 
/**
     * 功能描述:业务执行，包含加锁、释放锁(一直尝试，直到获取成功)
     *
     * @param lock 分布式锁定义
     * @param sleepTimeMillis 睡眠重试时间，单位：毫秒
     * @param runnable 执行体
     */
    public static <T> LockResult<T> execute(DistributedLock lock, long sleepTimeMillis,
        DistributedLockCallback<T> runnable) {
        return execute(lock, -1, sleepTimeMillis, runnable);
    } 
``` 
- 多次尝试获取锁，自定义超时时间

``` 
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



