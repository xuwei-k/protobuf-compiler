package protobuf_compiler

import org.scalatest.FunSpec

final class CoreTest extends FunSpec {

  describe("Core"){
    describe("compile success"){
      val JavaConversionsImport = "import scala.collection.JavaConversions"

      val input = ProtoFile("aaa", """ syntax = "proto3"; message BBB{ int32 ccc = 1;}; service ddd{ rpc eee(BBB) returns (BBB) {} }""")

      def extensions(files: Seq[ScalaFile]): Map[String, Int] = {
        files.map(_.name.split('.').last).groupBy(identity).map{case (k, v) => k -> v.size}
      }

      it("no options"){
        val input = ProtoFile("aaa", """ syntax = "proto3"; message BBB{ int32 ccc = 1;} """)
        val req = GenerateRequest(input :: Nil, Nil, Set.empty)
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 2, result)
        assert(extensions(result.files) == Map("scala" -> 2), result)
        assert(!result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(!result.files.exists(_.src.contains("io.grpc")))
      }

      List(
        (Language.Java, Map("java" -> 1)),
        (Language.CPP, Map("cc" -> 1, "h" -> 1)),
        (Language.Ruby, Map("rb" -> 1)),
        (Language.Python, Map("py" -> 1)),
        (Language.Objc, Map("h" -> 1, "m" -> 1)),
        (Language.CSharp, Map("cs" -> 1))
      ).foreach{
        case (lang, expect) =>
          it(lang.name) {
            val req = GenerateRequest(input :: Nil, Nil, Set(lang))
            val result = Core.compile(req)
            assert(!result.error, result)
            assert(extensions(result.files) == expect, result)
          }
      }

      it("java conversions"){
        val req = GenerateRequest(input :: Nil, List("java_conversions"), Set.empty)
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 3, result)
        assert(extensions(result.files) == Map("scala" -> 2, "java" -> 1), result)
        assert(result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(!result.files.exists(_.src.contains("io.grpc")))
      }

      it("grpc"){
        val req = GenerateRequest(input :: Nil, List("grpc"), Set.empty)
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 3, result)
        assert(extensions(result.files) == Map("scala" -> 3), result)
        assert(!result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(result.files.exists(_.src.contains("io.grpc")))
      }

      it("grpc and java_conversion"){
        val req = GenerateRequest(input :: Nil, List("java_conversions", "grpc"), Set.empty)
        val result = Core.compile(req)
        assert(!result.error, result)
        assert(result.files.size == 4, result.files.map(_.name))
        assert(extensions(result.files) == Map("scala" -> 3, "java" -> 1), result)
        assert(result.files.exists(_.src.contains(JavaConversionsImport)))
        assert(result.files.exists(_.src.contains("io.grpc")))
      }
    }

    it("compile failure"){
      val input = ProtoFile("foo", """ invalid proto file """)
      val req = GenerateRequest(input :: Nil, Nil, Set.empty)
      val result = Core.compile(req)
      assert(result.error, result)
      assert(result.files.isEmpty, result)
    }
  }

}
