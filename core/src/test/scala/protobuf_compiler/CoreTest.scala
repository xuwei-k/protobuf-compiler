package protobuf_compiler

import org.scalatest.FunSpec

final class CoreTest extends FunSpec {

  describe("Core"){
    describe("compile success"){
      val JavaConversionsImport = "import scala.collection.JavaConversions"

      val input = ProtoFile("aaa", """ syntax = "proto3"; message BBB{ int32 ccc = 1;}; service ddd{ rpc eee(BBB) returns (BBB) {} }""")

      it("no options"){
        val input = ProtoFile("aaa", """ syntax = "proto3"; message BBB{ int32 ccc = 1;} """)
        val req = GenerateRequest(input :: Nil, Nil)
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 2, result)
        assert(!result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(!result.files.exists(_.src.contains("io.grpc")))
      }

      it("java conversions"){
        val req = GenerateRequest(input :: Nil, List("java_conversions"))
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 2, result)
        assert(result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(!result.files.exists(_.src.contains("io.grpc")))
      }

      it("grpc"){
        val req = GenerateRequest(input :: Nil, List("grpc"))
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 3, result)
        assert(!result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(result.files.exists(_.src.contains("io.grpc")))
      }

      it("grpc and java_conversion"){
        val req = GenerateRequest(input :: Nil, List("java_conversions", "grpc"))
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 3, result)
        assert(result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(result.files.exists(_.src.contains("io.grpc")))
      }
    }

    it("compile failure"){
      val input = ProtoFile("foo", """ invalid proto file """)
      val req = GenerateRequest(input :: Nil, Nil)
      val result = Core.compile(req)
      assert(result.error, result)
      assert(result.files.isEmpty, result)
      assert(result.message.contains("No syntax specified for the proto file."), result)
    }
  }

}
