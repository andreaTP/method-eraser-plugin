//package demo
//package unicredit
package demo.unicredit


class Foo2 {
  def hello() = "hi"
}

class Foo3 {
  def hello() = "hi there"
}

object Demo2 extends App {
  val foo = new Foo3
  println(foo.hello()+" planet")
}
