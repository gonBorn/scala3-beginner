package extensionDemo

object Sugar {

  def main(args: Array[String]): Unit = {
    // 在元组前面追加新元素
    val a: (Int, Int) = (1, 2)
    println(3 *: a)
  }

}
