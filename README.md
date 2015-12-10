# protobuf-compiler

[![Build Status](https://travis-ci.org/xuwei-k/protobuf-compiler.svg?branch=master)](https://travis-ci.org/xuwei-k/protobuf-compiler)

<http://protobuf-compiler.herokuapp.com>

example

```
curl -X POST http://protobuf-compiler.herokuapp.com -d '{"files": [{"name": "foo", "src": "syntax = \"proto3\"; message A{int32 b = 1;}; service C{rpc d(A) returns (A){}}"}], "options": ["grpc", "java_conversions"], "language":["scala"]}'
```

- <http://protobuf-compiler.herokuapp.com/gist/d8e4114f1218121641d2?options=java_conversions>
- <http://protobuf-compiler.herokuapp.com/gist/d8e4114f1218121641d2?lang=all>
- <http://protobuf-compiler.herokuapp.com/gist/d8e4114f1218121641d2?lang=ruby&lang=python>
