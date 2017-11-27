#### springcloud-quickstart
 这是一个基于gradle构建工具的springCloud微服务框架超级简单入门教程。
 
 
包含了以下几个非常单纯的示例：<br/>
1、服务注册和发现 discovery <br/>
2、服务路由和负载均衡 routing <br/>
3、调用链追踪 call-chain <br/>
4、集中配置管理 config <br/>
5、服务网关 api-gateway<br/>
6、断路器 待补充...<br/>
7、容器化运行方案：<br/>
    根目录内的build.gradle脚本不要修改。<br/>
    在根目录执行gradle dockerBuild命令，构建完毕后，可使用docker images查看。 <br/>
    镜像构建完毕后，使用docker run -p 8761:8761 com.example/eureka-server:0.0.1-SNAPSHOT 运行，其他服务启动方式依次类推，建议端口暴露映射与内部端口一致，不然你需要改application配置文件了。
    

请按顺序学习，更多细节待补充...
<br/><br/><br/>
一些心得：<br/>
1、我使用的是alpine+jre8，gradle dockerBuild 命令会从docker hub下载alpine-jre基础镜像，第一次可能会比较久，它可以遍历所有子project，并自动构建出所有微服务的镜像，
    alpine+jre整个基础镜像是80m左右，主要是jre比较大，再加上springCloud微服务的N多个jar包，最终应用镜像大小是120m左右。算是目前我能做到的最小的镜像。<br/>
    小归小，但是也有缺点：<br/>
    alpine系统内置的不是我们熟悉的bash shell，而是ash shell。<br/>
    内置的jre，不提供jdk的很多调试命令，爱搞jvm调试的你们懂得。<br/>
2、我们使用的是se.transmode.gradle:gradle-docker插件，有兴趣可以GitHub查看它的使用说明。<br/>
3、本教程是教你如何使用spring cloud，以及构建镜像和本地运行集群，如果你需要学习更高级的devops和集群部署技术，比如docker swarm, kubernetes等，请多多star给我动力，我后面逐渐补充。