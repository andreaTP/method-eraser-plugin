package unicredit

import scala.tools.nsc.{ Global, Phase }
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }
import scala.tools.nsc.transform.{ Transform, TypingTransformers }
import scala.tools.nsc.symtab.Flags
import scala.tools.nsc.plugins.Plugin
import scala.tools.nsc.ast.TreeDSL

class MethodEraserPlugin(val global: Global) extends Plugin {
  import global._

  val name = "method-eraser--plugin"
  val description = "Want to delete method from classes by name"
  val components = List[PluginComponent](MethodEraserComponent)

  private object MethodEraserComponent extends PluginComponent  with Transform with TreeDSL {
    val global = MethodEraserPlugin.this.global
    import global._

    override val runsAfter = List("parser")

    val phaseName = "method-eraser"

    def newTransformer(unit: CompilationUnit) = new EraserTransformer(unit)

    class EraserTransformer(unit: CompilationUnit) extends Transformer {
      import scala.reflect.runtime.universe

      override def transform(tree: Tree): Tree = {
        tree match {
          case defDef: DefDef if (showRaw(defDef.name) == "hello") =>
            //println("NODE -> "+nodePrinters.nodeToString(defDef))
          
            println("show -> "+showRaw(defDef.name))
          //println("is a defdef!!"+defDef.name.fullName)
          //super.transform(defDef)
          CODE.UNIT
        case _ =>
          super.transform(tree)
      }
    }
    }
  }
}
