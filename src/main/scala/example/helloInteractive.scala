import scala.io.StdIn.readLine

object helloInteractive{
  def main(args: Array[String])= {
    println("Please enter your name")
    val name = readLine()

    println("Hello " + name + "!")
  }
}