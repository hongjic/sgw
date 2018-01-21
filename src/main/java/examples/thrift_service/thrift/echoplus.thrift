namespace java examples.thrift_server

service EchoPlusService {
    string echo1(1:string param1, 2:string param2);

    string echo2(1:string param3, 2:string param4);
}