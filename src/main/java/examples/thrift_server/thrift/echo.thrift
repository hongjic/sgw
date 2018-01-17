namespace java examples.thrift_server

service EchoService {
    string echo(1:string param);
}