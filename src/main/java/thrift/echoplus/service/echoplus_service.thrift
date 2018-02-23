namespace java echoplus.service

include "../struct/echoplus_struct.thrift"

service EchoplusService {
  echoplus_struct.Output echo(1: echoplus_struct.Input input1, 2: string input2)
}