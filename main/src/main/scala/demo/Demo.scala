//package demo
//package unicredit
package demo.unicredit


class Foo() {
	def hello() = "hello"
}
object Demo extends App {
  val foo = new Foo()

  println(foo.hello()+" world")
  
}
