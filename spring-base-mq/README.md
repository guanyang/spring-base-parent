## spring-base-mq

### 概览

当前组件提供统一的消息处理框架，该组件具有以下特点：

1. 注解驱动：通过`@DynamicEventStrategy`和`@EnableMQ`注解简化消息处理配置
2. 事件驱动架构：基于`IEventType`和`IMessageType`接口实现灵活的事件和消息类型管理
3. AOP切面支持：通过切面实现事件处理的统一拦截和管理
4. 多消息队列支持：目前支持RocketMQ和Kafka，后续方便扩展其他消息队列
5. 可扩展性：提供丰富的扩展点，支持自定义消息处理逻辑
6. 消息幂等支持：基于Redisson实现，需要添加redis相关配置
7. 事件日志记录：自定义`EventLogService`实现，可记录事件处理日志

### 依赖配置
- 添加依赖管理
```xml
<!--添加依赖管理-->
<dependencyManagement>
  <dependency>
    <groupId>org.gy.framework</groupId>
    <artifactId>spring-base-parent</artifactId>
      <!--注意调整到最新版本-->
    <version>1.1.0-SNAPSHOT</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
</dependencyManagement>
```
- 配置MQ依赖，默认集成RocketMQ
```xml
<dependencies>
    <!--默认集成RocketMQ依赖-->
    <dependency>
        <groupId>org.gy.framework</groupId>
        <artifactId>spring-base-mq</artifactId>
    </dependency>
</dependencies>
```
- 配置MQ依赖，添加Kafka支持
```xml
<dependencies>
    <dependency>
        <groupId>org.gy.framework</groupId>
        <artifactId>spring-base-mq</artifactId>
        <!--排除RocketMQ-->
        <exclusions>
            <exclusion>
                <artifactId>rocketmq-spring-boot-starter</artifactId>
                <groupId>org.apache.rocketmq</groupId>
            </exclusion>
        </exclusions>
    </dependency>
    <!--添加kafka依赖-->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
</dependencies>
```

### 使用说明

#### 1. 启用MQ功能

在Spring Boot启动类上添加`@EnableMQ`注解：
- 【注意】如果`IEventType`、`IMessageType`的实现类不在启动类包路径下面，则需要添加`@EnableMQ`注解的`basePackages`或`basePackageClasses`属性

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

    NORMAL(DEFAULT_NORMAL, "普通消息", ROCKETMQ),

    ORDERLY(DEFAULT_ORDERLY, "顺序消息", ROCKETMQ),

    KAFKA_DEFAULT(DEFAULT_KAFKA, "kafka消息", KAFKA);
    /**
     * 标识，必须唯一
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;
    /**
     * MQ类型
     */
    private final MqType mqType;
}

interface MessageTypeCode {
    String DEFAULT_NORMAL = "normal";
    String DEFAULT_ORDERLY = "orderly";
    String DEFAULT_KAFKA = "defaultConfig";
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
  "tag": "tag_a7fa55a231be",//消息标签，默认不设置
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

    //异步发送消息（延时消息），需要指定延时等级delayTimeLevel，kafka暂时不支持
    //sendService.asyncSend("测试消息对象", IEventType.DEMO_EVENT,2);

    //异步发送消息（顺序消息），需要指定顺序消费orderlyKey，例如订单号
    //sendService.asyncSend("测试消息对象", IEventType.DEMO_EVENT,"orderlyKey"); 

    //异步发送消息（消息自定义扩展），例如指定消息tag，kafka暂时不支持
    //sendService.asyncSend("test", DefaultEventType.DEMO_EVENT, req -> req.setTag("tag1"));

}
```
#### 7. 全局配置（消息幂等及重试支持）
```yml
spring:
  base-mq:
    global-config:
      idempotent-expire-millis: 7200000 #消息幂等性过期时间，单位毫秒，默认2小时
      idempotent-key-prefix: default:mq:idempotentKey #幂等性key前缀，默认为default:mq:idempotentKey
      retry-times: 5 #重试次数，默认5次
      initial-interval: 2000 #重试间隔时间，默认2000毫秒，仅kafka有效
      multiplier: 1.5 #重试间隔时间倍数，默认1.5倍，仅kafka有效
      max-interval: 10000 #重试最大间隔时间，默认10000毫秒，仅kafka有效
```

#### 8. 配置消息队列属性

##### 8.1 RocketMQ配置
- `defaultNormalListener`：默认实现的普通消费监听器，可直接使用，参考`DefaultNormalListener`
- `defaultOrderlyListener`：默认实现的顺序消费监听器，可直接使用，参考`DefaultOrderlyListener`

```yaml
spring:
  base-mq:
    rocketmq:
      normal:        #配置标识，需要与IMessageType的code一致
        nameServer: dev.rocketmq:9876    #【必须】nameServer地址，格式: `host:port;host:port`
        topic: normal_topic_dev    #【必须】消息topic
        producer:     #【可选】producer配置，需要发送消息则配置
          groupName: NormalProducerGroupDev    #【producer存在时必须】组名称，保证唯一
          instanceName: DEFAULT   #同一个组定义多个实例，需要定义不同的实例名称，避免冲突，默认：DEFAULT
          sendMessageTimeout: 3000    #消息发送超时时间，单位：毫秒，默认：3000
          retryTimesWhenSendFailed: 5   #消息同步发送失败重试次数，默认：2
          retryTimesWhenSendAsyncFailed: 5    #消息异步发送失败重试次数，默认：2
        consumer:        #【可选】consumer配置，需要监听消息则配置
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
##### 8.2 Kafka配置
- `defaultKafkaListener`：默认实现的消费监听器，可直接使用，参考`DefaultKafkaListener`

```yml
spring:
  base-mq:
    kafka:
      defaultConfig: #配置标识，需要与IMessageType的code一致，必须唯一
        bootstrapServers:
          - dev.kafka:9092
        topic: default_topic_dev
        producer:
          retries: 5 #消息发送失败重试次数
          key-serializer: org.apache.kafka.common.serialization.StringSerializer #key序列化器，默认：StringSerializer
          value-serializer: org.apache.kafka.common.serialization.StringSerializer #value序列化器，默认：StringSerializer
          acks: 1 #消息发送成功确认，0：不确认，1：Leader确认，-1/all：所有ISR确认
        consumer:
          group-id: default_consumer_group_dev #【consumer存在时必须】消费组ID，保证唯一
          enable-auto-commit: false #是否自动提交消费位点
          key-deserializer: org.apache.kafka.common.serialization.StringDeserializer #key反序列化器，默认：StringDeserializer
          value-deserializer: org.apache.kafka.common.serialization.StringDeserializer #value反序列化器，默认：StringDeserializer
          auto-offset-reset: latest #自动偏移量，latest：最新，earliest：最旧，none：无，默认：latest
          max-poll-records: 50 #每次拉取最大消息数，默认：500
        listener:
          concurrency: 4 #监听器容器中线程数，不能超过topic分区数
          ack-mode: MANUAL_IMMEDIATE #手动提交确认模式，参考AckMode枚举，建议：MANUAL_IMMEDIATE
          listener-bean-name: defaultKafkaListener #消费监听器bean名称
```

#### 9. 自定义扩展

可以通过实现以下接口进行自定义扩展：
- `EventLogService`：自定义事件日志服务
- `TraceService`：自定义追踪服务
- `EventMessageDispatchService`：自定义消息分发服务
- `EventMessageConsumerService`：自定义消息消费者服务
- `EventMessageProducerService`：自定义消息生产者服务
- `EventAnnotationMethodProcessor`：自定义事件注解方法处理器
- `EventMessageHandler`：自定义消息处理器

#### 10. 核心组件
- `DynamicEventStrategyAspect`：动态事件策略切面处理
- `DynamicEventStrategyRegister`：动态事件策略注册器
- `EventMessageProducerRegister`：消息生产者服务注册器
- `EventMessageServiceManager`：事件消息服务管理器
- `CommonServiceManager`：通用服务管理器
- `MqManager`：MQ管理器