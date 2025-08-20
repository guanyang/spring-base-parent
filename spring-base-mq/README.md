## spring-base-mq

### 概览

当前组件提供统一的消息处理框架，该组件具有以下特点：

1. 注解驱动：通过`@DynamicEventStrategy`和`@EnableMQ`注解简化消息处理配置
2. 事件驱动架构：基于`IEventType`和`IMessageType`接口实现灵活的事件和消息类型管理
3. AOP切面支持：通过切面实现事件处理的统一拦截和管理
4. 多消息队列支持：目前支持RocketMQ，后续方便扩展其他消息队列
5. 可扩展性：提供丰富的扩展点，支持自定义消息处理逻辑

### 使用说明

#### 1. 启用MQ功能

在Spring Boot启动类上添加`@EnableMQ`注解：

```java
@EnableMQ
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```


#### 2. 定义事件类型

实现`IEventType`接口定义业务事件类型：

```java
// 枚举方式实现事件类型
@Getter
@AllArgsConstructor
@CommonService //注册到spring容器，方便统一服务管理
public enum DefaultEventType implements IEventType {
    DEMO_EVENT(DEFAULT_DEMO, "示例事件");
    /**
     * 标识
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;
}

interface EventTypeCode {
    String DEFAULT_DEMO = "DEMO_EVENT";
}
```

#### 3. 定义消息类型

实现`IMessageType`接口定义业务消息类型：

```java
// 枚举方式实现消息类型
@Getter
@AllArgsConstructor
@CommonService //注册到spring容器，方便统一服务管理
public enum DefaultMessageType implements IMessageType {

    NORMAL(DEFAULT_NORMAL, "普通消息"),

    ORDERLY(DEFAULT_ORDERLY, "顺序消息");
    /**
     * 标识
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

}

interface MessageTypeCode {
    String DEFAULT_NORMAL = "normal";
    String DEFAULT_ORDERLY = "orderly";
}
```
#### 4. 消息报文统一格式

```json
{
  "requestId": "requestId_af91566ba30e",//请求唯一标识
  "timestamp": 1747197048412,//事件毫秒时间戳
  "eventTypeCode": "DEMO_EVENT",//事件类型编码（必须）
  "bizKey": "bizKey_a7fa55a231be",//业务唯一标识，如果有则幂等处理
  "data": {                        //业务数据示例，后续基于不同事件，定义不同的data结构
      "age": 20,
      "name": "test"
  },
  "delayTimeLevel": 0,    //延迟消息级别，18个等级（1~18），默认不延迟
  "orderlyKey": "orderlyKey_1ab3c128b5db"  //顺序消息key，默认不设置
}
```

#### 5. 使用事件策略（消息消费）

在业务方法上使用`@DynamicEventStrategy`注解：

```java
@Service
public class UserService {

    @DynamicEventStrategy(eventTypeCode = EventTypeCode.DEFAULT_DEMO)
    public void handleUserRegister(User user) {
        // 业务逻辑处理
        System.out.println("处理用户注册: " + user);
    }
}
```

#### 6. 消息发送

```java
@Test
void sendMessageTest(){
    //messageTypeCode指定消息类型，当前使用DEFAULT_NORMAL类型作为示例
    EventMessageProducerService sendService = EventMessageServiceManager.getSendService(MessageTypeCode.DEFAULT_NORMAL);
    //异步发送消息（普通消息）
    sendService.asyncSend("测试消息对象", IEventType.DEMO_EVENT);

    //异步发送消息（延时消息），需要指定延时等级delayTimeLevel
    //sendService.asyncSend("测试消息对象", IEventType.DEMO_EVENT,2);

    //异步发送消息（顺序消息），需要指定顺序消费orderlyKey，例如订单号
    //sendService.asyncSend("测试消息对象", IEventType.DEMO_EVENT,"orderlyKey"); 

}
```


#### 7. 配置消息队列属性

在`application.yml`中配置RocketMQ等相关属性：

```yaml
rocketmq:
  normal:        #配置标识，需要与IMessageType的code一致
    nameServer: dev.rocketmq:9876    #【必须】nameServer地址，格式: `host:port;host:port`
    topic: normal_topic_dev    #【必须】消息topic
    producer:     #producer配置
      groupName: NormalProducerGroupDev    #【producer存在时必须】组名称，保证唯一
      instanceName: DEFAULT   #同一个组定义多个实例，需要定义不同的实例名称，避免冲突，默认：DEFAULT
      sendMessageTimeout: 3000    #消息发送超时时间，单位：毫秒，默认：3000
      retryTimesWhenSendFailed: 5   #消息同步发送失败重试次数，默认：2
      retryTimesWhenSendAsyncFailed: 5    #消息异步发送失败重试次数，默认：2
    consumer:        #consumer配置
      groupName: NormalConsumerGroupDev    #【consumer存在时必须】组名称，保证唯一
      instanceName: DEFAULT   #同一个组定义多个实例，需要定义不同的实例名称，避免冲突，默认：DEFAULT
      messageModel: CLUSTERING  #消费模式，CLUSTERING集群，BROADCASTING广播，默认：CLUSTERING
      selectorExpression: '*'   #消费tag表达式定义，*匹配所有，格式：tag1||tag2，默认：*
      consumeMessageBatchMaxSize: 1   #批量消费消息数量，默认：1
      consumeThreadMin: 16    #消费线程最小值定义，默认：20
      consumeThreadMax: 16    #消费线程最大值定义，默认：20
      consumeFromWhere: CONSUME_FROM_LAST_OFFSET    #消费位置定义，参考ConsumeFromWhere枚举，默认：CONSUME_FROM_LAST_OFFSET
      listenerBeanName: defaultNormalListener  #消费监听器bean名称
```


#### 8. 自定义扩展

可以通过实现以下接口进行自定义扩展：
- `EventLogService`：自定义事件日志服务
- `TraceService`：自定义追踪服务
- `EventMessageDispatchService`：自定义消息分发服务
- `EventMessageConsumerService`：自定义消息消费者服务
- `EventMessageProducerService`：自定义消息生产者服务

#### 9. 核心组件
- `DynamicEventStrategyAspect`：动态事件策略切面处理
- `DynamicEventStrategyRegister`：动态事件策略注册器
- `RocketMqManager`：RocketMQ管理器
- `EventMessageServiceManager`：事件消息服务管理器
- `CommonServiceManager`：通用服务管理器