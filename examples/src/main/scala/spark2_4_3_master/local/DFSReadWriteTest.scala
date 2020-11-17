package spark2_4_3_master.local

import java.io.File
import java.util
import java.util.Collections

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}

import scala.io.Source

object DFSReadWriteTest {

  private var localFilePath: File = new File(".")
  private var dfsPath: String = ""

  private def printUsage(): Unit = {
    println(
      """Usage: DFSReadWriteTest <localFile> <dfsDir>
        | <localFile> - (String) local file to be used
        | <dfsDir> - (String) DFS dir to be used
        |""".stripMargin)
  }

  private def parseArgs(args: Array[String]): Unit = {
    if (args.length != 2) {
      printUsage()
      System.exit(1)
    }

    var i = 0
    localFilePath = new File(args(0))
    if (!localFilePath.exists) {
      println(s"Given path ${args(i)} does not exist ")
      printUsage()
      System.exit(1)
    }

    if (!localFilePath.isFile) {
      println(s"Given file ${args(i)} does not exist ")
      printUsage()
      System.exit(1)
    }

    i += 1
    dfsPath = args(i)
  }

  def readFile(localFilePath: File): List[String] = {
    val bufferedSource = Source.fromFile(localFilePath)
    val lineIterator: Iterator[String] = bufferedSource.getLines()
    bufferedSource.close()
    val lineList: List[String] = lineIterator.toList
    lineList
  }

  def runLocalWordCount(fileContents: List[String]): Int = {
    val wordCountMap: Map[String, Int] = fileContents.flatMap(_.split("="))
      .flatMap(_.split("\\."))
      .filter(recd => recd.nonEmpty && !recd.startsWith("#"))
      .groupBy(w => w)
      .mapValues(_.size)

      // wordCountMap.foreach( tp => println(s"$tp._1 --> ${tp._2}") )
      wordCountMap.values.sum
  }

  def main(args: Array[String]): Unit = {

    parseArgs(args)

    val fileContents = readFile(localFilePath)
    val localWordCount = runLocalWordCount(fileContents)
    // println(s"$localWordCount")

    val spark: SparkSession = SparkSession.builder().appName("DFSReadWrite")
      .master("local[*]").getOrCreate()
    val fileRDD: RDD[String] = spark.sparkContext.parallelize(fileContents)
    val outPath = dfsPath + "/dfs_read_write"
    // fileRDD.saveAsTextFile(outPath)
    /*val rddStr = spark.sparkContext.textFile(outPath)
    val dfsCount = rddStr.flatMap(_.split("=")).flatMap(_.split("\\.")).filter(_.nonEmpty)
      .map(w => (w, 1)).countByKey().values.sum

    println(s"$dfsCount")*/
    // import spark.implicits._
    val ds = spark.read.textFile(outPath)
    val count = ds.flatMap(_.split("="))(Encoders.STRING)
      .flatMap(_.split("\\."))(Encoders.STRING)
      .filter(!_.startsWith("#"))
      .filter { _.nonEmpty }
      .groupBy("value").count()
      .agg(("count", "sum")).first().getAs[Long](0)
      // countDF.show(false)

    println(count)
  }
}
