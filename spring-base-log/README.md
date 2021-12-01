### spring-base-log

#### 概览
- 针对所有请求，添加`x-trace-id`，方便追踪，参考`LogInterceptor`
- 增加日志输出切面，方便排查方法调用日志，参考`LogTraceAspect`

#### 使用说明
- 添加maven依赖
```
  <dependency>
    <groupId>org.gy.framework</groupId>
    <artifactId>spring-base-log</artifactId>
    <version>${current.version}</version>
  </dependency>
```
- 日志`@LogTrace`支持类、方法层级定义
- 定义到类层级，会记录当前类所有方法调用日志，示例如下
```
@Slf4j
@RestController
@RequestMapping("/test")
@LogTrace
public class TestController {
    ...
}

```
- 定义到方法级别，仅记录当前方法调用日志，示例如下：
```
@GetMapping("/log")
@LogTrace(fieldName = "dto", desc = "测试日志")
public Response log(@Valid TestRequestDTO dto) {
    return testService.test(dto);
}
```
- 输出日志内容如下：
```
{
    "invokeEndTime": 1638371312536,
    "responseBody": "{\"data\":{\"name\":null,\"time\":1638371312536},\"error\":0,\"msg\":\"success\",\"requestId\":\"e214d479e51042c5a8e139c1b974ade5\"}",
    "logStartTime": 1638371312111,
    "logEndTime": 1638371312537,
    "methodName": "test",
    "className": "org.gy.framework.demo.service.controller.TestController",
    "version": "v1",
    "requestBody": "{\"dto\":{\"name\":\"test\"}}",
    "clientIp": "127.0.0.1",
    "serverIp": "10.100.129.177",
    "invokeStartTime": 1638371312129,
    "invokeCostTime": 407,
    "desc": "default"
}
```




