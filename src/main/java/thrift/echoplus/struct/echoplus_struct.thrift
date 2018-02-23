namespace java echoplus.struct

struct Input {
  1: optional i32 id,
  2: required string message
}

struct Output {
  1: optional i32 id,
  2: required string message,
  3: optional string append
}