package spark2_4_3_master.local

import java.io.File

import org.apache.spark.SparkFiles
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}


object All {

  def fileTest(args: Array[String], spark: SparkSession): Unit = {
    if (args.length < 1) {
      System.err.println("Usage: FileTest [file]")
      System.exit(1)
    }
    val file: RDD[Row] = spark.read.text(args(0)).rdd
    val mapped: RDD[Int] = file.map(s => {
      println(s" ${s.mkString("---")}")
      s.length }).cache()
    for (iter <- 1 to 2) {
      val start = System.currentTimeMillis()
      for (x <- mapped) { println(s"x $x ${x+2} "); x + 2 }
      val end = System.currentTimeMillis()
      println(s"Iteration $iter took ${end-start} ms")
    }
  }

  def exceptionHandling(spark: SparkSession): Unit = {

    val sc = spark.sparkContext
      sc.parallelize(0 until sc.defaultParallelism).foreach {i => {
        if (math.random() > 0.75) { throw new Exception(s"Exception Handling Test in $i")}
      }}
  }

  def envVariablesTest(spark: SparkSession): Unit = {

    for (i <- 1 until 5) {
      println(s"Alive for $i out of 5 seconds")
      Thread.sleep(1000)
    }
  }

  def sparkFilesTest(args: Array[String], spark: SparkSession): Unit = {
    val file = args(0)
    val rdd: RDD[Boolean] = spark.sparkContext.parallelize(Seq(1)).map(_ => {
      val localLocation = SparkFiles.get(file)
      println(s"$file stored at $localLocation")
      new File(localLocation).isFile
    })
    println(s"Mounting of $file was ${rdd.collect().head}")
  }

  def multiBroadcastTest(spark: SparkSession): Unit = {
    val slices = 2
    val num = 1000000
    val sc = spark.sparkContext
    val array = (0 until num).toArray

    for(i <- 1 to 3) {
      println(s"For iteration $i \n =================")
      val bcAry = sc.broadcast(array)
      val bcAry1 = sc.broadcast(array)
      val startTime = System.nanoTime()
      val observedSlices = sc.parallelize(1 to 10, slices)
        .map(_ => (bcAry.value.length, bcAry1.value.length) )
      observedSlices.collect().foreach(println)
      println(s" %d Iteration took %.0f milliseconds"
        .format(i, (System.nanoTime - startTime) / 1E6) )
    }
  }

  def main(args: Array[String])  {
    val spark = SparkSession.builder().appName("AllExamples").master("local[*]").getOrCreate()
    // fileTest(args, spark)
    // exceptionHandling(spark)
    // envVariablesTest(spark)
    multiBroadcastTest(spark)
    // sparkFilesTest(args, spark)
    spark.stop()
  }
}
