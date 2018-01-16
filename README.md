# Simple Gateway

The goal is to build a non-blocking gateway server that acts like a proxy between the client and the downstream services. 

It receives Https requests from clients, convert it into a corresponding RPC request to the backend and send it back to the client in Http response format.

The project is being built upon Netty. Still under early development.