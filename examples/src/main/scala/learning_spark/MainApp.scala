package learning_spark

object MainApp {

  private def foo(args: Array[String]): String = args.foldLeft (" ") ( (a,b) => a+b )

  def main(args: Array[String]): Unit = {
    println("Hello Pavan... " + foo(args) )
  }
}
