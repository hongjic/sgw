# SGW
SGW is a simple non-blocking micro-service gateway built upon Netty, it stands between different types of http clients and all micro-service backends (mostly using faster RPC protocols).

The main purpose of this project is to build a protocol conversion and request dispatching layer, and aims to handle large throughput (which results in using Netty as the base framework). 

Other features include:

* Service discovery/load balancing
* Filters
* Spring style router configuration

[中文文档](./README_CHN.md)


## Dependencies
* use Gradle to build project.
* Netty 4.1
* Thrift 0.10.0
* CuratorFramework 

## Target functions

0. Non-blocking IO

1. Dynamic Routers
	
	* Router configuration using "routing.yaml"
	
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
	* Spring style router configuration
		
		
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

	* Routing templates: 
	
		```
			@RequestParser
		   	public Object[] parse(FullHttpRequest request, @PathVar("id") int id) {
		   		...
		   	}
		```
		
	* Routing configuration hot replacement (under development)
	

2. protocol conversion from HTTP to Thrift
	
	currently hard coded, plan to enable configuration on thrift protocols.
	
	* protocol：TMultiplexedProtocol, TCompactProtocol
	* transport： TFramedTransport

	Also planed to provide user interfaces to help support more protocols.
	
3. service discovery/load balancing

	* cache service node metadata in memory
	* receive Zookeeper update in real time
	* using round-robin load balancing strategy. (plan to support customization) 

4. filters
	
	* preRouting filters: filter requests when requests arrive.
	* postRouting filters: filter responses before sending response.
	* routing filters: filter requests before sending to backend services.
	
5. circuit breaker (under development)
	
	behave like hystrix.
	* built purely on filters


## Request - Response lifecycle
1. decoding http requests from network
2. pre-routing filters
3. Routing: find the matched backend service and use load balancing to get the target service node.
4. routing filter
5. convert http request to thrift request (`@RequestParser`)
6. get a service connection (either established or new) and send the encoded thrift request to the backend. 
7. decode thrift response
8. convert thrift response back to http response (`@ResponseGenerator`)
9. post-routing filter
10. send http response back to client.

<!--
1. **请求路由和服务发现** 接受客户端http请求，通过配置好的路由信息启动路由，找到http请求定义`HttpRequestDef`在路由中找到对应的下游服务信息`RpcInvokerDef`：rpc协议，服务名，方法名，数据转换器。 然后通过服务发现获取服务地址等其他信息，创建`RpcInvoker`实例。
2. **Http请求转换成RPC参数** 这个部分由业务逻辑决定，继承 `FullHttpRequestParser` 实现无状态的转换器。在`HttpParamConvertor`中被调用。
3. **连接下游服务，创建RPC channel** `RpcInvoker.connectAsync()`
4. **序列化RPC请求** `ThriftEncoder`
5. **反序列化RPC响应** `ThriftDecoder
6. **写回Http channel**  `RpcFinalHandler`
7. **RPC结果转换成Http响应** 这个部分也有业务逻辑决定，继承`FullHttpResponseGenerator` 实现，在`ResultHttpConvertor`中被调用。-->

## About Extension


## Demo
[sgw.demo.DemoServer](./src/main/java/demo/DemoServer.java)

<!--## 运行
1. 启动`examples.thrift_service.ThriftEchoServer` 端口hardcode为9090
2. 启动`sgw.core.GatewayServer`  默认绑定8080端口，目前service discovery是hardcode的，直接会连接到localhosst:9090
3. http客户端POST http://localhost:8080/aaa 请求体附上一端字符串string
4. http响应体："This is return result: " + string

-->
