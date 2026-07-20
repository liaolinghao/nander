# SERVER统一WEB能力构件授权模块

本构件属于SERVER统一WEB能力的授权模块，提供以下功能：

1、提供JWT创建与解析功能。

2、session采用redis管理，以支持分布式微服务有关的session集中管理。

3、集成Spring Security，采用JWT执行认证授权。

4、提供了给控制器利用的获取登录信息与注销登录信息涉及的公共方法，继承BaseController即可直接使用。

5、设计一套权限表达式，由四段标识符+”:“组成，具体为：服务名称:模块名称:对象名称:操作名称，每段可用*代替，表示不作限制。具体含义如下：

服务名称：对应平台的标识符，比如：后台管理工作台标识为admin，客户工作台标识为customer，客户经理工作台标识为manager。

模块名称：对应平台功能模块的标识符，比如：系统配置模块标识为config。

对象名称：对应平台功能模块操作数据对象的标识符，比如：角色数据标识为role。

操作名称：对应平台功能模块操作数据对象的动作类型的标识符，比如：删除动作标识为delete，查询动作标识为query。备注：该段可以省略。

使用时，在对应接口上添加注解即可。如：@PreAuthorize("!@jwtSecurityUtils.isEnableJwtSecurity() or hasRole('ROLE_SUPER') or (hasRole('ROLE_ADMIN') and hasPermission('/role/view','admin:config:role'))")

备注：本构件实现了一套基于JWT执行权限验证的处理流程，同时提供了三个特殊的服务接口，用于支持其他方式的权限验证处理。具体说明如下：

INonStandardJwtParserService：如果服务存在自己独有的认证对象构建方式，可通过提供实现该接口的服务组件来完成自行解析JWT并构建认证对象。

IAppSecretLoaderService：可通过实现该接口的服务组件来完成应用秘钥的动态获取以支持解析对应JWT并构建认证对象的过程。

IAppKeyAndSecretLoaderService：可通过实现该接口的服务组件来完成“应用键：应用秘钥”的动态获取以支持解析对应JWT并构建认证对象的过程。

6、提供了用于Controller类方法上的审计日志注解@AuditLog(platform = "xxx", module = "xxx", description = "xxx", mode = x)，具体服务通过提供继承BaseAuditAop的自定义切面器并实现processAuditLog方法完成审计日志的处理。

## JWT格式

### 认证设备

```
{
    // 是否有状态token
    "isStateful": true,
    // 设备ID
	"id": 9381748320280577,
    // 租户ID
	"tenantId": 0,
	// 认证对象访问渠道
	"channel": "WEB",
	// JWT认证对象类型
	"type": "device",
	// 授予接入应用的基本权限列表
	"grantedAuthorityList": [{
		"role": "roleName1"
	}, {
		"role": "roleName2"
	}],
	// 基本信息
	"infos": {
		"xxx": "xxx"
	}
	// 自定义权限数据包
	"authorities": "xxx"	
}
```

### 认证应用

```
{
    // 是否有状态token
    "isStateful": true,
    // 应用ID
	"id": 9381748320280577,
	// 租户ID
	"tenantId": 0,
	// 认证对象访问渠道
	"channel": "WEB",
	// JWT认证对象类型
	"type": "client",
	// 授予接入应用的基本权限列表
	"grantedAuthorityList": [{
		"role": "roleName1"
	}, {
		"role": "roleName2"
	}],
	// 基本信息
	"infos": {
		"xxx": "xxx"
	},
	// 自定义权限数据包
	"authorities": "xxx"	
}
```

### 认证用户

```
{
    // 是否有状态token
    "isStateful": true,
    // 用户ID
	"id": 9381748320280577,
	// 租户ID
	"tenantId": 0,
	// 认证对象访问渠道
	"channel": "WEB",
	// JWT认证对象类型
	"type": "user",
	// 授予接入应用的基本权限列表
	"grantedAuthorityList": [{
		"role": "roleName1"
	}, {
		"role": "roleName2"
	}],
	// 基本信息
	"infos": {
		"mobilephone": "18799372845",
		"email": "26098134 @qq.com",
		"nickname": "xxx",
		"name": "xxx"
	},
	// 用户在接入应用的自定义权限数据包
	"authorities": "xxx",
	// 用户角色列表
	"roles": [{
		"code": "roleName1"
	}, {
		"code": "roleName2"
	}],
	// 用户权限列表
	"permissions": [{
		"id": 1,
		"name": "销售数据分析",
		"pattern": "admin:analysis:sales"
	}, {
		"id": 2,
		"name": "数据分析",
		"pattern": "admin:analysis:*"
	}],
	// 用户所属组织列表
	"orgs": [{
	    "eId": 1944159896105355,
		"id": 1944159896207360,
		"name": "xxx",
		"isLeader": true
	}]
}
```

## 配置

本构件有以下重要配置，描述如下：

### 用于创建JWT

```
bigbird:
  server:
    web:
      auth:
        jwt:
          enable: true
          # 前端请求后端接口时，对于需要认证授权接口，请求头需要包含：Authorization: Bearer xxxx 其中，xxxx是token
          # jwt请求头标识
          header: Authorization
          # jwt请求头令牌前缀
          token-start-with: Bearer
          # 使用Base64对该令牌进行编码，chinatelecom_intelligent-business-enterprise-platform_bigbird-dribgib_mroftalp-esirpretne-ssenisub-tnegilletni_moceletanihc
          base64-secret: Y2hpbmF0ZWxlY29tX2ludGVsbGlnZW50LWJ1c2luZXNzLWVudGVycHJpc2UtcGxhdGZvcm1fYmlnYmlyZC1kcmliZ2liX21yb2Z0YWxwLWVzaXJwcmV0bmUtc3NlbmlzdWItdG5lZ2lsbGV0bmlfbW9jZWxldGFuaWhj
          # 令牌过期时间，此处单位：分钟
          token-validity-in-minutes: 10
          # 刷新令牌过期时间，此处单位：分钟
          refresh-token-validity-in-minutes: 30
          # 配置不需要认证的接口
          without-api: /auth/**,/error/**,/health/**,/common/**,/open/**
          # token签发者，比如：后端管理员操作系统~administer，后端客户操作系统~customer
          issuer: administer
```

## JWT创建与解析处理器

```
// JWT创建与解析处理器
@Autowired
private JwtSecurityProcessor jwtSecurityProcessor;
```

## 构件依赖

日志组件、Redis构件、SERVER核心能力构件。
