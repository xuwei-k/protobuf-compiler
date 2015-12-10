package protobuf_compiler

import java.io.{ByteArrayOutputStream, PrintStream}

import com.github.os72.protocjar.Protoc
import com.trueaccord.scalapb.compiler.PosixProtocDriver
import sbt.IO
import sbt.Path._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NonFatal

object Core {
  def compile(request: GenerateRequest): CompileResult = {
    request.lang.map{
      case Language.Scala =>
        compileScala(request)
      case lang =>
        compile0(request.files, lang)
    }.reduce(_ merge _)
  }

  private[this] def compile0(files: List[ProtoFile], lang: Language): CompileResult = {
    IO.withTemporaryDirectory { inputDir =>
      IO.withTemporaryDirectory { outputDir =>
        val sourceFiles = files.map { f =>
          val file = inputDir / f.name
          IO.write(file, f.src)
          file.absolutePath
        }

        val args = List(
          "-v300",
          "-I" + inputDir.absolutePath,
          s"--${lang.name}_out=${outputDir.absolutePath}"
        ) ++ sourceFiles

        val (returnCode, stdout) = withStdOut {
          com.github.os72.protocjar.Protoc.runProtoc(args.toArray)
        }

        println(s"${lang.name} returnCode " + returnCode)

        val resultFiles = lang.extensions.map(ext => outputDir ** s"*.$ext").reduce(_ +++ _).get.sortBy(_.getName).map { src =>
          println((src.name, src.length))
          ScalaFile(src.getName, IO.readLines(src).mkString("\n"))
        }
        CompileResult(resultFiles.sortBy(_.name), stdout, !returnCode.contains(0))
      }
    }
  }

  private[this] def compileScala(request: GenerateRequest): CompileResult = {
    IO.withTemporaryDirectory { inputDir =>
      IO.withTemporaryDirectory { outputDir =>
        val c = new PosixProtocDriver

        val sourceFiles = request.files.map { f =>
          val file = inputDir / f.name
          IO.write(file, f.src)
          file.absolutePath
        }

        val args = List(
          "-v300",
          "-I" + inputDir.absolutePath,
          s"--scala_out=${request.options.mkString(",")}:${outputDir.absolutePath}"
        ) ++ sourceFiles

        val (returnCode, stdout) = withStdOut {
          c.buildRunner(a => Protoc.runProtoc(a.toArray))(args)
        }

        println("scala returnCode " + returnCode)

        val scalaFiles = (outputDir ** "*.scala").get.sortBy(_.getName).map { scalaSource =>
          println((scalaSource.name, scalaSource.length))
          ScalaFile(scalaSource.getName, IO.readLines(scalaSource).mkString("\n"))
        }
        CompileResult(scalaFiles.sortBy(_.name), stdout, !returnCode.contains(0))
      }
    }
  }

  private[this] def withStdOut[A](action: => A): (Option[A], String) = {
    val outStream = new ByteArrayOutputStream
    val encode = "UTF-8"
    sbt.Using.bufferedOutputStream(outStream) { out =>
      val p = new PrintStream(out, true, encode)
      this.synchronized {
        val originalOut = System.out
        val originalErr = System.err
        try {
          System.setOut(p)
          System.setErr(p)
          try{
            val f = Future(action)(ExecutionContext.global)
            val r = Await.result(f, 5.seconds)
            p.flush()
            p.close()
            out.close()
            out.flush()
            Some(r) -> outStream.toString(encode)
          }catch {
            case NonFatal(e) =>
              e.printStackTrace()
              val oo = new ByteArrayOutputStream
              val pp = new PrintStream(oo, true, encode)
              e.printStackTrace(pp)
              None -> oo.toString(encode)
          }
       } finally {
          System.setOut(originalOut)
          System.setErr(originalErr)
          p.close()
        }
      }
    }
  }
}
