# springcloud-quickstart
这是一个基于gradle构建工具的springCloud微服务框架超级简单入门教程。
spring cloud为开发人员提供了快速搭建分布式系统的一整套解决方案，包括配置管理、服务发现、断路器、路由、微代理、事件总线、全局锁、决策竞选、分布式会话等等。它可以直接在PC上使用Java的main方法运行集群。
另外说明spring cloud是基于springboot的，所以需要开发中对springboot有一定的了解。

## spring cloud依赖管理--根build.gradle文件
1. 定义gradle全局公共变量：gradle.properties文件。我们主要用它来定义springCloud版本号，springboot版本号，以及其他一些公共变量
```
## dependency versions.
springBootVersion=1.5.8.RELEASE
springCloudVersion=Edgware.RELEASE
### docker configuration
#gradle docker plugin version
transmodeGradleDockerVersion=1.2
#This configuration is for docker container environment to access the local machine host，in Chinese is "宿主机" ip.
hostMachineIp=10.40.20.54
```
2. 申明springboot gradle插件
```
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
3. 为所有gradle project引入springcloud公共依赖
```
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
4. settings.gradle文件
它的作用是帮我们在IDE内自动组织项目结构（project structures）的，帮我们避开idea/eclipse内配置工程结构的复杂操作有兴趣可以读一下源码。
```
def dir = new File(settingsDir.toString())
def projects = new HashSet()
def projectSymbol = File.separator + 'src'
dir.eachDirRecurse { subDir ->
    def subDirName = subDir.canonicalPath
    def isSubProject = true
    if (subDirName.endsWith(projectSymbol)) {
        for (String projectDir in projects) {
            if (subDirName.startsWith(projectDir)) {
                isSubProject = false
                break
            }
        }
        if (isSubProject) {
            projects << subDirName
            def lastIndex = subDirName.lastIndexOf(projectSymbol)
            def gradleModulePath = subDirName.substring(dir.canonicalPath.length(), lastIndex).replace(File.separator, ':')
            println "include " + gradleModulePath
            include gradleModulePath
        }
    }
}
```
## 服务注册中心 /discovery/eureka-server
1. 本示例使用的是Spring Cloud Netflix的Eureka ,eureka是一个服务注册和发现模块，公共依赖部分已经在根路径的build.gradle中给出，
eureka-server模块自身依赖在/discovery/eureka-server/build.gradle文件配置如下：
```
dependencies {
    compile('org.springframework.cloud:spring-cloud-starter-eureka-server')
}
```
2. eureka是一个高可用的组件，不依赖后端缓存，每一个实例注册之后需要向注册中心发送心跳，是在eureka-server的内存中完成的，在默认情况下erureka-server也是一个eureka client，必须要指定一个server地址。eureka-server的配置文件appication.yml：
```
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
请注意一点，很多网上的教程，还有spring官方的教程上将service-url写成serviceUrl这是错误的！
3. eureka-server的springboot入口main application类：
```
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```
详见/discovery/eureka-server模块。
4.
### 服务路由和负载均衡 routing.
待补充
### 调用链追踪 call-chain.
待补充
### 集中配置管理 config.
待补充
### 服务网关 api-gateway.
待补充
### 断路器 待补充.
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
