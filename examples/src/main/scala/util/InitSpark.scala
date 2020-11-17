package util

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.{DataFrameReader, SparkSession}

trait InitSpark {

  val spark: SparkSession = SparkSession.builder().master("local[*]")
    .appName("Spark2 RealTime UseCases").getOrCreate()

  val reader: DataFrameReader = spark.read.options(Map("header" -> "true",
    "inferSchema" -> "true", "mode" -> "DROPMALFORMED"))

  private def init()  {
    spark.sparkContext.setLogLevel("ERROR")
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
    LogManager.getRootLogger.setLevel(Level.ERROR)
  }
  init()

  def close() {
    spark.close()
  }

}
