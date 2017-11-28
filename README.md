# springcloud-quickstart
这是一个基于gradle构建工具的springCloud微服务框架超级简单入门教程。

## 包含了以下几个简单示例
### 服务注册和发现 discovery.
### 服务路由和负载均衡 routing.
### 调用链追踪 call-chain.
### 集中配置管理 config.
### 服务网关 api-gateway.
### 断路器 待补充.
### 容器化运行方案
1. 先新建一个容器网络，以便多个容器(微服务)之间可以相互通信，命令为：           
        ```docker network create springcloud-quickstart```        
2. 修改根路径内的gradle.properties文件中的"hostMachineIp"配置为宿主机IP地址，这很重要！          
3. 在根目录执行gradle dockerBuild命令，构建完毕后，可使用docker images查看。           
4. 由于几乎所有的其他微服务组件都依赖服务发现，因此先启动服务注册服务端：          
        使用 ```docker run --network springcloud-quickstart -p 8761:8761 com.example/eureka-server:0.0.1-SNAPSHOT``` 运行，        
        发现服务(eureka-server)需要端口暴露，以便我们可以在容器外面访问到它的控制台，地址是：`http://localhost:8761`，此外，建议映射与内部端口一致。           
5. 启动其他服务，启动方式依次类推，除了zipkin-server和zuul网关，其他微服务组件是可以不暴露端口到外部的：          
        列举几个关键节点启动命令：         
* zuul网关启动：        
        ```docker run --network springcloud-quickstart -p 8769:8769 com.example/zuul:0.0.1-SNAPSHOT```            
* zipkin调用链追踪：          
        ```docker run --network springcloud-quickstart -p 9411:9411 com.example/zipkin-server:0.0.1-SNAPSHOT```           
* 配置服务器启动：        
        ```docker run --network springcloud-quickstart -p 8888:8888 com.example/config-server:0.0.1-SNAPSHOT```          
* 其他：       
        ```docker run --network springcloud-quickstart com.example/<applicationName>:0.0.1-SNAPSHOT```        

**请按顺序学习，更多细节待补充...** 
### 一些心得
1. 我使用的是alpine+jre8，gradle dockerBuild 命令会从docker hub下载alpine-jre基础镜像，第一次可能会比较久，它可以遍历所有子project，并自动构建出所有微服务的镜像，alpine+jre整个基础镜像是80m左右，主要是jre比较大，再加上springCloud微服务的N多个jar包，最终应用镜像大小是120m左右。算是目前我能做到的最小的镜像。  
小归小，但是也有缺点：
* alpine系统内置的不是我们熟悉的bash shell，而是ash shell。
* 内置的jre，不提供jdk的很多调试命令，爱搞jvm调试的你们懂得。<br/>
2. 我们使用的是se.transmode.gradle:gradle-docker插件，有兴趣可以GitHub查看它的使用说明。<br/>

### 本教程是教你如何使用spring cloud，以及构建镜像和本地运行集群，如果你需要学习更高级的devops和集群部署技术，比如docker swarm, kubernetes等，请多多star给我动力，我后面逐渐补充。
