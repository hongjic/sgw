# SGW

本项目基于Netty搭建一个非阻塞的微服务网关，作为http客户端和thrift后台服务的隔离层层，把得到的http请求转换成thrift请求，再将收到的thrift结果转换成http响应，将不同的后台服务对http客户端透明化。网关作为一个IO heavy的应用，高吞吐量是非常重要的（所以选择Netty作为底层IO框架）

其他的主要功能包括：

* 服务发现/负载均衡
* 服务连接池
* 过滤器
* Spring风格的路由配置

## 依赖
* 使用gradle构建项目
* Netty 4.1
* Thrift 0.10.0
* CuratorFramework

## 目标功能

0. NIO

1. 可动态更新路由

	路由加载方式
	* 网关启动时通过加载配置文件`routing.yaml`
		
		```
		thriftServices:
  		  - http: POST /echo # http请求
    		convertor: # 解析请求和生成响应的类
    		service: echoservice # 下游服务名（zookeeper中的名字）
		    method: echo # 下游服务方法名
		    clazz: # thrift生成的类名
		  - http: ....
		  ...
		xxxServices:

		```
	* Spring风格路由配置
		
		
		```
		@ThriftRouter(http = {"POST", "/echo"}, service = "echoservice", method = "echo", args = EchoService.echo_args.class, result = EchoService.echo_result.class)
      public class EchoConvertor{
			
			@RequestParser
    		public Object[] parse(FullHttpRequest request) {
        		return new Object[] {request.content().toString(CharsetUtil.UTF_8)};
    		}

    		@ResponseGenerator
    		public String generate(String result) {
        		return result;
    		}
		}
		```
	* Routing匹配解析模版，路径参数注入: 
	
		```
			@RequestParser
		   	public Object[] parse(FullHttpRequest request, @PathVar("id") int id) {
		   		...
		   	}
		```
		
		
	* 通过客户端工具运行时更改路由配置 （未完成）
	
		作为一种特殊的请求，同样在netty中处理。
	* 路由的数据结构保证读取的速度，对路由的更新通过创建一个新的hashmap实例的方式防止线程阻塞。

2. http和rpc转换

	thrift: thrift使用的协议暂时是hard code的
	* protocol：TMultiplexedProtocol, TCompactProtocol
	* transport： TFramedTransport
	
3. 服务发现/负载均衡

	* 服务发现和netty线程分开，作为一个background thread运行。
	* 使用一个zookeeper连接，配置所有的服务，每个服务对应一个本地缓存实例，缓存实时从zookeeper获得更新。
	* 负载均衡默认采用轮询。负载均衡的数据结构保证获取服务实例的速度，节点更新的操作在zookeeper线程中串行执行。

4. 动态添加（删除）过滤器
	
	网关提供两种类型的过滤器。preRouting, routing和postRouting过滤器。（和Zuul过滤器类似）
	* preRouting: 刚解析完http请求后
	* routing: 调用下游服务前  （未完成）
	* postRouting: 生成http响应后
	
	加载过滤器方式：
	* 网关启动前通过代码配置
	* 运行时通过客户端工具  （未完成）
		1. 加载新的filter
		2. disable已加载的filter
		3. enable被disable的filter
	
5. 熔断 （未完成）
	
	熔断完全基于过滤器实现。通过记录下游服务响应的不同状态的次数（成功，失败，超时等），决定对于之后请求的操作（例：直接返回）。（原理和hystrix类似）

6. 错误处理
	
	request运行过程中抛出异常 和 请求被过滤器过滤等特殊情况，自动快速产生一个对应的http响应。

## 请求-响应生命周期
1. 从数据流中解码，得到的http请求
2. preRouting过滤器。
3. 查询http请求对应的下游服务，选择服务节点（服务发现，负载均衡）
4. routing过滤器
5. 将http请求转换成thrift请求参数（这里内部调用配置的http请求解析器）
6. 连接服务节点（生成rpc channel），将thrift请求参数编码成数据流发送至下游服务
7. 从数据流中解码，得到的thrift响应
8. 将thrift响应转换成http响应（这里内部调用配置的http响应生成器）
9. postRouting过滤器
10. 将http响应编码成数据流发送回客户端

<!--
1. **请求路由和服务发现** 接受客户端http请求，通过配置好的路由信息启动路由，找到http请求定义`HttpRequestDef`在路由中找到对应的下游服务信息`RpcInvokerDef`：rpc协议，服务名，方法名，数据转换器。 然后通过服务发现获取服务地址等其他信息，创建`RpcInvoker`实例。
2. **Http请求转换成RPC参数** 这个部分由业务逻辑决定，继承 `FullHttpRequestParser` 实现无状态的转换器。在`HttpParamConvertor`中被调用。
3. **连接下游服务，创建RPC channel** `RpcInvoker.connectAsync()`
4. **序列化RPC请求** `ThriftEncoder`
5. **反序列化RPC响应** `ThriftDecoder
6. **写回Http channel**  `RpcFinalHandler`
7. **RPC结果转换成Http响应** 这个部分也有业务逻辑决定，继承`FullHttpResponseGenerator` 实现，在`ResultHttpConvertor`中被调用。-->

## 扩展
关于如何扩展
## Demo
[sgw.demo.DemoServer](./src/main/java/demo/DemoServer.java)


<!--## 运行
1. 启动`examples.thrift_service.ThriftEchoServer` 端口hardcode为9090
2. 启动`sgw.core.NettyGatewayServer`  默认绑定8080端口，目前service discovery是hardcode的，直接会连接到localhosst:9090
3. http客户端POST http://localhost:8080/aaa 请求体附上一端字符串string
4. http响应体："This is return result: " + string

-->
