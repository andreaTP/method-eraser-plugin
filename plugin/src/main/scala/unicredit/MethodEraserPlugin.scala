package unicredit

import scala.tools.nsc.{ Global, Phase }
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }
import scala.tools.nsc.transform.{ Transform, TypingTransformers }
import scala.tools.nsc.symtab.Flags
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.ast.TreeDSL

import java.nio.file.Files.readAllBytes
import java.nio.file.Paths.get

import scala.collection.mutable

class MethodEraserPlugin(val global: Global) extends Plugin {
  import global._

  val name = "method-eraser--plugin"
  val description = "Want to delete method from classes by name"
  val components = List[PluginComponent](MethodEraserComponent, MethodEraserCheckComponent)

  lazy val config: mutable.Set[String] = 
      mutable.Set((try
        new String(readAllBytes(get("./method_eraser.config"))).split("\n").toSeq
      catch {
        case err: Throwable =>
          println("Method eraser configuration file is missing")
          Seq()
      }): _*)

  private object MethodEraserCheckComponent extends PluginComponent {
    val global = MethodEraserPlugin.this.global
    import global._

    override val runsAfter = List("method-eraser")
    override val runsRightAfter = Some("method-eraser")

    val phaseName = "method-eraser-check"

    override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        config.foreach(m =>
          unit.warning(null, "METHOD ERASER ERROR: method "+m+" not found")
        )
      }
    }

  }

  private object MethodEraserComponent extends PluginComponent  with Transform with TreeDSL {
    val global = MethodEraserPlugin.this.global
    import global._

    override val runsAfter = List("parser")
    override val runsRightAfter = Some("parser")

    val phaseName = "method-eraser"

    def newTransformer(unit: CompilationUnit) =
      new AggregateEraserTransformer(unit)
      
    class AggregateEraserTransformer(unit: CompilationUnit) extends Transformer {

      val erasers = config.map(m => new EraserTransformer(unit, m))

      override def transform(tree: Tree): Tree = {
        val iter = erasers.iterator
        var count = 0
        while(iter.hasNext && !iter.next.check(tree)) {
          count += 1
        }
        if (count == erasers.size)
          super.transform(tree)
        else
          CODE.UNIT
      }
    }

    class EraserTransformer(unit: CompilationUnit, initMethodName: String) {
      import scala.reflect.runtime.universe

      var methodName = initMethodName

      def setNewMethodName(toRemove: String) = {
        val nn = methodName.replaceFirst(toRemove, "")

        methodName =
          if (nn.startsWith(".")) nn.replaceFirst(".", "")
          else nn
      }

      def check(tree: Tree): Boolean = {
        tree match {
          case pd @ PackageDef(pid, stats) if (methodName.startsWith(pid.toString)) =>
            setNewMethodName(pid.toString)
            false
          case cd @ ClassDef(mods, name, tparams, impl) if (methodName.startsWith(name.toString)) =>
            setNewMethodName(name.toString)
            false
          case dd @ DefDef(Modifiers(flags, privateWithin, annotations), name, tparams, vparamss, tpt, rhs) 
            if (methodName == name.toString) =>
            unit.warning(tree.pos, "METHOD ERASED")
            config -= initMethodName
            true
        case any =>
            false
      }
    }
    }
  }
}
