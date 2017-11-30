# springcloud-quickstart
这是一个基于gradle构建工具的springCloud微服务框架超级简单入门教程。
spring cloud为开发人员提供了快速搭建分布式系统的一整套解决方案，包括配置管理、服务发现、断路器、路由、微代理、事件总线、全局锁、决策竞选、分布式会话等等。它可以直接在PC上使用Java的main方法运行集群。
另外说明spring cloud是基于springboot的，所以需要开发中对springboot有一定的了解。

## spring cloud依赖管理
1. 申明gradle全局公共变量：/gradle.properties文件。我们主要用它来定义springCloud版本号，springboot版本号，以及其他一些公共变量
```properties
## dependency versions.
springBootVersion=1.5.8.RELEASE
springCloudVersion=Edgware.RELEASE
### docker configuration
#gradle docker plugin version
transmodeGradleDockerVersion=1.2
#This configuration is for docker container environment to access the local machine host，in Chinese is "宿主机" ip.
hostMachineIp=10.40.20.54
```
2. 在/build.gradle文件内申明springboot gradle插件
```gradle
buildscript {
    repositories {
        maven { url "https://repo.spring.io/libs-milestone/" }
        jcenter()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}
```
3. 在/build.gradle文件内为所有gradle project引入springcloud公共依赖
```gradle
allprojects {
    apply plugin: 'org.springframework.boot'
    repositories {
        maven { url "https://repo.spring.io/libs-milestone/" }
        jcenter()
    }
    dependencyManagement {
        imports {
            //spring bom helps us to declare dependencies without specifying version numbers.
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
        }
    }
}
```
4. /settings.gradle文件
它的作用是帮我们在IDE内自动组织项目结构（project structures）的，帮我们避开idea/eclipse内配置工程结构的复杂操作，有兴趣可以读一下源码。

## 服务注册中心/discovery/eureka-server
1. 本示例使用的是Spring Cloud Netflix Eureka ,eureka是一个服务注册和发现模块，公共依赖部分已经在根路径的build.gradle中给出，
eureka-server模块自身依赖在/discovery/eureka-server/build.gradle文件配置如下：
```gradle
dependencies {
    compile('org.springframework.cloud:spring-cloud-starter-eureka-server')
}
```
2. eureka是一个高可用的组件，不依赖后端缓存，每一个实例注册之后需要向注册中心发送心跳，是在eureka-server的内存中完成的，在默认情况下erureka-server也是一个eureka client，必须要指定一个server地址。eureka-server的配置文件appication.yml：
```yml
server:
  port: 8761
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```
另请注意一点：很多网上的教程以及spring官方的教程上将'service-url'写成'serviceUrl'这是错误的！<br/>
3. eureka-server的springboot入口main application类：
```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```
启动这个main方法，然后访问 http://localhost:8761  <br/>
代码详见/discovery/eureka-server模块。<br/>
4. eureka-client服务注册客户端（service provider）
服务提供方，比如一个微服务，作为eureka client身份可以将自己的信息注册到注册中心eureka-server内。<br/>
/discovery/eureka-demo-client/build.gradle文件指定依赖如下：
```gradle
dependencies {
    compile "org.springframework.cloud:spring-cloud-starter-eureka"
}
```
springboot入口main类：com.example.EurekaDemoClientApplication.java
```java
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class EurekaDemoClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaDemoClientApplication.class, args);
    }
    @Value("${server.port}")
    private int port;

    @RequestMapping("/hi")
    public String hi() {
        return "hi, my port=" + port;
    }
}
```
application.yml
```
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8763
spring:
  application:
    name: eureka-demo-client
```
执行main方法启动springboot后，可以访问http://localhost:8763/hi 查看springboot restApi效果。
访问http://localhost:8761 (eureka-server控制台)查看服务注册效果。
依次类推，再启动另外一个/discovery/eureka-demo-client0，请再次查看服务注册效果。

### 服务路由和负载均衡/routing
以上/discovery/eureka-demo-client和/discovery/eureka-demo-client0我们可以把它看作是服务提供者service provider，这里开始定义服务消费者，即对服务提供者进行调用的的客户端。
当同一个微服务启动了多个副本节点后，我们对该服务的调用就需要一个负载均衡器来选择其中一个节点来进行调用，这就是springcloud-ribbon提供的功能。而feign则是对springcloud ribbon的一个封装，方便使用的。这里不深入介绍ribbon了，它本质就是一个借助服务注册发现实现的一个负载均衡器。
下面来分析feign源码：<br/>
/routing/routing-feign/build.gradle
```gradle
dependencies{
    compile "org.springframework.cloud:spring-cloud-starter-feign"
    compile "org.springframework.cloud:spring-cloud-starter-eureka"
}
```
com.example.RoutingDemoFeignApplication.java
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RoutingDemoFeignApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoutingDemoFeignApplication.class, args);
    }
}
```
com.example.CallHiService.java接口，指明service provider微服务名: eureka-demo-client
```java
@FeignClient(value = "eureka-demo-client")
public interface CallServiceHi {
    @RequestMapping(value = "/hi", method = RequestMethod.GET)
    String sayHiFromClientOne(@RequestParam(value = "name") String name);
}
```
com.example.HiController.java 方便我们验证负载均衡结果：
```java
@RestController
public class HiController {
    @Autowired
    private CallServiceHi hiServiceCaller;

    @RequestMapping("hi")
    public String hi(@RequestParam String name) {
        return hiServiceCaller.sayHiFromClientOne(name);
    }
}
```
application.yml需要指明服务注册中心的地址，从而可以获取到所有目标节点信息，从而实现负载的功能
```yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
server:
  port: 8765
spring:
  application:
    name: service-feign
```
运行main方法，启动springboot，然后请多次访问http://localhost:8765/hi?name=happyyangyuan 查看负载效果。
预期的输出结果轮流为：hi happyyangyuan, my port=8763   /   hi happyyangyuan, my port=8762

### 调用链追踪/call-chain
/call-chain内的demo展示的是使用Spring Cloud Sleuth实现的分布式系统的调用链追踪方案，它兼容支持了zipkin，只需要在build.gradle文件中引入相应的依赖即可。<br/>
#### /call-chain/zipkin-server
顾名思义，就是springCloud Sleuth内置的zipkin-server集成，以来配置/call-chain/zipkin-server/build.gradle：
```gradle
dependencies {
    compile "io.zipkin.java:zipkin-server"
    compile "io.zipkin.java:zipkin-autoconfigure-ui"
}
```
com.example.ZipkinServerApplication.java，注意加入@EnableZipkinServer注解：
```java
@SpringBootApplication
@EnableZipkinServer
public class ZipkinServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinServerApplication.class, args);
    }
}
```
application.properties指明zipkin端口
```
server.port=9411
```
执行com.example.ZipkinServerApplication.java的main方法启动，然后访问http://localhost:9411
#### /call-chain/zipkin-clients调用链模拟
这里模拟调用链： zipkin-client --调用-->  zipkin-client0 --调用--> zipkin-client1  <br/>
##### /call-chain/zipkin-clients/build.gradle指定三个zipkinClients的公共依赖配置：
```gradle
subprojects{
    dependencies {
        compile 'org.springframework.cloud:spring-cloud-starter-zipkin'
        /*这两个依赖已经被传递依赖了，因此不需要申明
        compile "org.springframework.cloud:spring-cloud-sleuth-zipkin-stream"
        compile "org.springframework.cloud:spring-cloud-starter-sleuth"*/
        compile "org.springframework.cloud:spring-cloud-starter-feign"
        compile "org.springframework.cloud:spring-cloud-starter-eureka"
    }
}
```
注意到，服务调用我们使用的的是feign客户端，并且引入了服务发现eureka客户端和zipkin客户端。
##### zipkin客户端配置
配置文件依然是/call-chain/zipkin-clients/zipkin-client/src/main/resources/application.properties
```
server.port=8988
spring.zipkin.base-url=http://localhost:9411
spring.application.name=zipkin-client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
#The percentage of call-chaining messages to be sent to sleuth provider (zipkin for example).
#Value range is 0.0~1.0, defaults to 0.1
spring.sleuth.sampler.percentage=1
```
上面的配置文件，注意一下三点
- 给定zipkin-server端地址：spring.zipkin.base-url=http://localhost:9411
- 给定eureka-server地址：eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
- 方便调试给定调用链追踪数据采用比例为100%，对应配置值为1，取值范围是0~1：spring.sleuth.sampler.percentage=1
##### zipkin客户端ZipkinClientApplication代码逻辑
com.example.ZipkinClientApplication.java使用feign实现了对zipkin-client0的调用：
```java
@SpringBootApplication
@RestController
@EnableDiscoveryClient
@EnableFeignClients
public class ZipkinClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinClientApplication.class, args);
    }
    @Autowired
    private FeignServiceInterface feignService;
    @RequestMapping(value = "/")
    public String home() {
        return feignService.callServiceFeign("no name");
    }
    @FeignClient(value = "zipkin-client0")
    public interface FeignServiceInterface {
        @RequestMapping(value = "/", method = RequestMethod.GET)
        String callServiceFeign(@RequestParam(value = "name") String name);
    }
}
```
不知道你注意到没有，zipkin客户端激活功能不需要什么鬼类似“@EnableXxx”之类的注解，满足以下两点就可以激活zipkin客户端了数据采集了：
- 配置zipkin-server访问地址
- 引入zipkin 客户端依赖
就是这么简单！
其他两个zipkin-client0、zipkin-client1依次类推，我就不再粘贴代码了。
#### 验证调用链看效果
启动zipkin-server、zipkin-client、zipkin-client0、zipkin-client1，启动方式你们都懂的。
然后访问http://localhost:8988 即可将调用链日志数据发送给zipkin-server，然后你再访问http://localhost:9411 查看调用链的展示。界面操作太简单，我就不贴图了。
### 集中配置管理/config
待补充
### 服务网关/api-gateway
待补充
### 断路器 待补充
待补充
### 容器化运行方案
1. 先新建一个容器网络，以便多个容器(微服务)之间可以相互通信，命令为<br/>
```docker network create springcloud-quickstart```
2. 修改根路径内的gradle.properties文件中的"hostMachineIp"配置为宿主机IP地址，这很重要！
3. 在根目录执行gradle dockerBuild命令，构建完毕后，可使用docker images查看。
4. 由于几乎所有的其他微服务组件都依赖服务发现，因此先启动服务注册服务端，使用如下命令运行：<br/>
  ```docker run --network springcloud-quickstart -p 8761:8761 com.example/eureka-server:0.0.1-SNAPSHOT```<br/>
发现服务(eureka-server)需要端口暴露，以便我们可以在容器外面访问到它的控制台，地址是http://localhost:8761，建议端口映射与内部端口一致。
5. 启动其他服务，启动方式依次类推，除了zipkin-server和zuul网关，其他微服务组件是可以不暴露端口到外部的，列举几个关键节点启动命令。
  * zuul网关启动<br/>
  ```docker run --network springcloud-quickstart -p 8769:8769 com.example/zuul:0.0.1-SNAPSHOT```
  * zipkin调用链追踪<br/>
  ```docker run --network springcloud-quickstart -p 9411:9411 com.example/zipkin-server:0.0.1-SNAPSHOT```
  * 配置服务器启动<br/>
  ```docker run --network springcloud-quickstart -p 8888:8888 com.example/config-server:0.0.1-SNAPSHOT```
  * 其他<br/>
  ```docker run --network springcloud-quickstart com.example/<applicationName>:0.0.1-SNAPSHOT```

**请按顺序学习，更多细节待补充...** 
### 一些心得
1. gradle dockerBuild命令会遍历所有子project，并自动构建出所有微服务的镜像。我使用的是alpine+jre8，如果本地没有这个镜像，会从dockerHub下载alpine-jre基础镜像，第一次可能会比较久。alpine+jre整个基础镜像是80m左右，主要是jre比较大，再加上springCloud微服务的n多个jar包，最终应用镜像大小是120m左右。算是目前我能做到的最小的镜像。小归小，但是也有缺点：
* alpine系统内置的不是我们熟悉的bash shell，而是ash shell。
* 内置的jre，不提供jdk的很多调试命令，爱搞jvm调试的你们懂得。<br/>
2. 我们使用的是se.transmode.gradle:gradle-docker插件，有兴趣可以GitHub查看它的使用说明。<br/>

**本教程是教你如何使用spring cloud，以及构建镜像和本地运行集群，如果你需要学习更高级的devops和集群部署技术，比如docker swarm, kubernetes等，请多多star给我动力，我后面逐渐补充。**
