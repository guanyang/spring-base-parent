### spring-base-sign

#### 概览
- 接口签名、验签组件，方便接口添加验签特性、访问接口生成签名

#### 使用说明
- 添加maven依赖
```
  <dependency>
    <groupId>org.gy.framework</groupId>
    <artifactId>spring-base-sign</artifactId>
    <version>${project.version}</version>
  </dependency>
```

##### 验签
- 添加配置
```
sign:
  client:
    apps:
      -
        appId: 1    // 分配给客户端的appId
        appKey: test1    // 分配给客户端的appSecret
        clockSkew: 30   // 签名允许的时间偏移，单位：秒，默认30s
      -
        appId: 2
        appKey: test2
```

- 定义请求参数类，该类实现SignedReq接口
- 在请求参数中需要参与签名的字段上添加@SignParam注解
```
@SignParam注解name属性用于该字段在生成签名时的健值，不指定则默认使用字段名
```
- 在需要验签的Controller方法加上@SignCheck注解
- 验签失败会抛出SignInvalidException异常，可以针对此异常定义返回调用方的信息

##### 签名
- 使用ParamSignUtils.sign(req, key)生成签名
```
req类上使用@SignParam注解标识参与签名的字段，@SignParam注解name属性用于该字段在生成签名时的健值，不指定则默认使用字段名
```

##### 签名规则
- 把请求参数键值对按字典序排序，然后进行拼接，例如：`key1=value1&key2=value2`
- 再拼接上appKey键值对`appKey=${appKey}`，例如：`key1=value1&key2=value2&appKey=${appKey}`
- 将上述拼接之后的字符串进行md5签名即可
