### spring-base-csrf

#### 概览
- csrf防护组件，支持referer验证、双重cookie验证，支持多种验证方式
  
#### 使用说明
- 添加maven依赖

```
<dependency>
    <groupId>org.gy.framework</groupId>
    <artifactId>spring-base-csrf</artifactId>
    <version>${project.version}</version>
</dependency>
```

- 添加配置

```
csrf:
  ## 验证方式(referer/token)(多种使用";"分隔)
  types: token
  ## referer验证方式下的url(多种使用";"分隔，使用token方式可以不配置)
  #referers: http://www.xxx.com
  ## token验证方式下的token参数名(使用referer方式可以不配置)
  paramName: csrf_token
  ## token验证方式下的cookie名(使用referer方式可以不配置)
  tokenName: csrf_token_cookie
  ## token验证方式下的token生成地址(使用referer方式可以不配置)
  tokenUrl: /api/token
  ## token验证方式下的cookie有效期(单位:s)(使用referer方式可以不配置)
  tokenMaxAge: 7200

```

- 在需要csrf验证的Controller方法加上@CsrfCheck注解