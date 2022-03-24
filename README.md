# saas-datasource-spring-boot-starter

一个支持SaaS多租户动态添加和切换数据源的快速启动工具。

**在了解完如何使用后请务必仔细阅读[注意事项](#注意事项)和[最佳实践](#最佳实践)**

简单示例项目请前往 <a href="https://gitee.com/air-soft/saas-datasource-samples" target="_blank">saas-datasource-samples</a>

完整解决方案请前往 <a href="https://gitee.com/air-soft/airboot-saas-datasource" target="_blank">Airboot-SaaS-DataSource</a>

## 介绍

### 适用场景

`saas-datasource-spring-boot-starter`（以下简称“本工具”）适用于SaaS场景中 **共享数据源，独立Schema** 或 **独立数据源** 的多租户架构，支持多种方式自动或手动切换租户数据源，并可在**运行时动态添加租户数据源**，使用轻量，简单方便。

注意，本工具并不适用于 **共享Schema，共享数据表** 的SaaS多租户架构（即租户仅在表中用tenantId来区分），如果想采用此架构，可参考或直接使用 <a href="https://gitee.com/air-soft/airboot-saas" target="_blank">Airboot-SaaS</a> ，是基于Mybatis-Plus的一套完整解决方案。

数据源是一个较抽象的概念，比如一个共享VIP的数据库集群算一个数据源，一个独立的数据库服务器，一个服务器上的Mysql或Oracle服务实例，甚至一个数据服务中的独立Schema，都可以称为一个数据源，这取决于架构设计时所考虑的切分粒度，具体业务具体分析。

现在为方便起见，**我们假设一个数据源指的是一台数据服务器**，那么上述提到的三种SaaS架构的主要区别如下：

|   |  独立数据源   |  共享数据源，独立Schema  |  共享Schema，共享数据表  |
|  :----:  |  :----:  |  :----:  |  :----:  |
|  特性  | 每个租户都有自己独立的数据服务器，相互之间完全隔离  | 每一台数据服务器上都存在数量不定的多个租户，每个租户拥有自己的Schema，Schema之间完全隔离 | 所有租户都在一台数据服务器上的一个Schema中，仅通过数据表内的tenantId来做租户区分  |
|  优点  |  拥有最高的隔离性、安全性和性能  | 具备一定的隔离性和安全性，成本适中，性能较高，扩展方便 | 成本最低，设计简单，全局数据统计方便  |
|  缺点  |  成本太高，全局数据统计不方便  | 一个数据服务器有问题会影响到多个租户，全局数据统计不方便 | 隔离性和安全性最低，编码时必须严格注意tenantId，如有误操作很容易影响大片租户，随着租户数据量增加性能容易到达瓶颈  |
|  适用场景  |  混合云，对隔离性和安全性要求较高的租户，土豪  | 比较适中的方案，适合大部分SaaS场景，但在全局数据统计上要自己进行架构设计 |  适合低成本的小型项目，对隔离性和安全性要求不高，在可预见的未来数据量不大  |

本工具兼容 **共享数据源，独立Schema** 和 **独立数据源** 两种架构（本质上取决于你提供什么样的`jdbcUrl`），使用本工具后，通常情况下开发者无需关心租户切换或tenantId等问题，在开发体验上与单租户（即非SaaS）开发无异。

请根据自身产品的业务特点及架构选型决定是否使用本工具。

### 版本对应说明

本工具基于 <a href="https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter" target="_blank">dynamic-datasource-spring-boot-starter</a> 和 `Druid数据库连接池`开发，可整合 `Mybatis-Plus` 或 `Mybatis` ，由于这些开源项目也在不断更新中，尤其像`dynamic-datasource-spring-boot-starter`这几年经历过数次大小重构，因此本工具需要针对其不同版本做出适配。

为了兼容可能存在的老旧项目，本工具在起始版本会对应`dynamic-datasource-spring-boot-starter`较早期的版本，而后续更新中会逐步对应不同的版本区间，**使用本工具的开发者请务必确认好当前项目中这几个jar包所对应的版本区间**，具体对应关系如下：

|  saas-datasource-spring-boot-starter   |  dynamic-datasource-spring-boot-starter  |  mybatis-plus-boot-starter  |  mybatis-spring-boot-starter  |
|  :----:  |  :----:  |  :----:  |  :----:  |
| 1.0.0  | version <= 2.4.2 | <div align="left">根据`@SaaS`注解的位置分为两种情况：<br/>1. 如果注解在Mapper上，则 version <= 3.0.7.1，若高于此版本dynamic-datasource会报错；<br/>2. 如果注解不在Mapper上，则可使用目前最新版本 version <= 3.5.1 (latest)。<br/>按[最佳实践](#最佳实践)，推荐上述第二种情况，注解不要放在Mapper上。</div>  | version <= 2.2.2 (latest) |

## 快速使用

### 引入依赖

```
<dependency>
    <groupId>com.air-software</groupId>
    <artifactId>saas-datasource-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 配置默认数据源

通常情况下，我们会将租户数据源配置保存在一个公共库里，**不必担心在切换数据源时会频繁查库**，因为本工具会将已获取过的数据源缓存起来，如果切换时缓存中没有对应数据源，才会查库（具体看你的`SaaSDataSourceProvider`怎么实现）。

因此，项目启动时的默认数据源推荐配置为公共库，此处的配置风格参照`dynamic-datasource-spring-boot-starter`，举例如下：

```
spring:
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
  datasource:
    dynamic:
      primary: common
      datasource:
        common:
          url: jdbc:mysql://localhost/saas_common?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&autoReconnect=true&autoReconnectForPools=true&allowMultiQueries=true
          username: root
          password: 123456
          driver-class-name: com.mysql.jdbc.Driver
      druid:
        filters: stat
        initial-size: 1
        max-active: 20
        max-pool-prepared-statement-per-connection-size: 50
        max-wait: 60000
        min-evictable-idle-time-millis: 300000
        min-idle: 1
        pool-prepared-statements: true
        stat-view-servlet:
          allow: true
        test-on-borrow: false
        test-on-return: false
        test-while-idle: true
        time-between-eviction-runs-millis: 60000
        validation-query: SELECT 'x'
```

### 实现数据源提供者

开发者需自行实现SaaS多租户数据源提供者，一个简单的实现示例如下：

```
@Component
public class MySaaSDataSourceProvider implements SaaSDataSourceProvider {
    
    @Resource
    private DataSourceConfigMapper dataSourceConfigMapper;
    
    @Resource
    private SaaSDataSourceCreator saasDataSourceCreator;
    
    public static String SCHEMA_URL_SUFFIX = "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&autoReconnect=true&autoReconnectForPools=true&allowMultiQueries=true";
    
    @Override
    public DataSource createDataSource(String dsKey) {
        DataSourceConfig dataSourceConfig = dataSourceConfigMapper.selectById(dsKey);
        String jdbcUrl = dataSourceConfig.getHost() + dataSourceConfig.getSchemaName() + SCHEMA_URL_SUFFIX;
        
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setUrl(jdbcUrl);
        dataSourceProperty.setUsername(dataSourceConfig.getUsername());
        dataSourceProperty.setPassword(dataSourceConfig.getPassword());
        // 此处存在拼写错误，应为poolName，在dynamic-datasource的后续版本中有修复。
        dataSourceProperty.setPollName(dsKey);
        
        return saasDataSourceCreator.createDruidDataSource(dataSourceProperty);
    }
    
}
```

### 启用注解

在SpringBoot主启动类上添加`@EnableSaaSDataSource`注解，表示启用SaaS数据源功能。

```
@SpringBootApplication
@EnableSaaSDataSource
public class SaaSApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SaaSApplication.class, args);
    }
}
```

在需要切换数据源的类或方法上标记`@SaaS`注解，此注解中可设置**租户标识字段名称**，默认值为`dsKey`。

比如我在使用注解时设置为`@SaaS("tenantId")`，那么我在Request Session或Header中就需要用`tenantId`字段来设置租户标识，而这个租户标识在首次切换至此租户时，会传递至你自己实现的`SaaSDataSourceProvider`中，以此来获取租户对应数据源。

注意：如果使用了`SaaSDataSource`上下文来手动切换数据库，则`@SaaS`中的值会被忽略。

### 切换数据源

本工具共提供三种方式来切换数据源，按优先级从高到底排列如下：

- SaaSDataSource.switchTo(String/Long dsKey)
- Request Session
- Request Header

你可以在任意地方多次调用`SaaSDataSource.switchTo`来手动切换数据源，他会影响到你下一次即将执行的数据库操作，常用于拦截器、定时任务、异步操作、循环刷库，跨库统计、消息消费等场景。

## 注意事项

以下注意事项**非常重要**，请开发者务必仔细阅读：

1. 公共库中的表最好不要跟租户库中的业务表有重合，因为当切换数据源失败时，会自动切换回应用启动时配置的默认数据源（通常为公共库），此时如果公共库中存在同名业务表的话，那在明面上是不会报错的，只不过数据都到公共库里了，这样不利于排查问题。
2. 为安全起见，尽量不要使用Header模式，因为前端传递的数据永远是不可信的。如果要使用前端直接传递的值，一定要配合权限控制，比如整个系统的超级管理员想要自由切换至不同租户，此时就需要使用前端传值。这也是我保留了Header模式，但优先级降为最低的原因。
3. **事务中无法切换数据源**，首先一定要注意`@SaaS`的标记位置，至少应在最外层事务或更上一层的调用方标记此注解，即保证注解在事务开启前发挥作用，以切换到正确的数据源。其次不要在事务内调用`SaaSDataSource.switchTo`，而应在事务开启前调用。**如果没有在事务开启前通过注解或手动切换至正确的数据源，则事务会在默认数据源上执行。**
4. 本工具**不提供分布式事务的实现**，也未做过相关测试，如果需要分布式事务请开发者自行实现和测试，理论上本工具兼容分布式事务。
5. 在定时任务、异步操作、消息消费等无法获取Request上下文的场景下，**一定要记得处理业务前调用`SaaSDataSource.switchTo`来手动切换数据源**。

## 最佳实践

基于上述注意事项，结合现代Web开发的技术倾向，可以得出以下几条最佳实践：

1. 为保障注解在事务开启前发挥作用，**在Web项目中推荐将`@SaaS`标记在`Controller`层**，一般这就是事务的顶层了。大部分项目中都会有一个`BaseController`作为所有Controller的父类，将`@SaaS`注解标记在父类上，对所有Controller都会起作用。
2. 现代Web项目中使用Token的情况已逐步超过Session，在Token场景下，我们可以将`dsKey`放入Token中，或为安全起见将`dsKey`放入Redis，而Redis Key放入Token中。随后我们在拦截器中解析Token之后，使用获得的`dsKey`调用`SaaSDataSource.switchTo`来切换数据源，这样在编写业务代码时就无需关心租户切换问题了。（不要忘了最后在拦截器的`afterCompletion`中调用`SaaSDataSource.clear`方法）
