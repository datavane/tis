

//val testmap = Map(("baisui", "17"))
//
//Thread.currentThread().getId
//
//for ((key) <- testmap) {
//  println(key);
//}

def test(): (Int, String) = {
  (1, "hello")
}

print(test._1 + "," + test._2)