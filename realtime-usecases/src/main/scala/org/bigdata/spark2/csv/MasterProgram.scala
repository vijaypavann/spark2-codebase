package org.bigdata.spark2.csv

import org.apache.spark.sql.Column
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.functions.{col, concat_ws, hour, min, minute}
import org.bigdata.spark2.util.InitSpark

object MasterProgram extends InitSpark {

  def main(args: Array[String]) {

    val csvOptions: Map[String, String] = Map("delimiter"->"\t", "header"->"true", "timestampFormat"->"HH:mm")
    val csvPath: String =  "realtime-usecases\\src\\main\\resources\\csv\\master_data\\"

    val masterProgramData = spark.read.options(csvOptions).csv(csvPath)
      .withColumn("start_time", col("start_time").cast(DataTypes.TimestampType))

    val startTime: Column = concat_ws(":", hour(col("stp")), minute(col("stp")))

    masterProgramData.groupBy("Master_Program")
      .agg(min("start_time").alias("stp"))
      .withColumn("start_time", startTime)
      .select("Master_Program", "start_time").sort("Master_Program").show(false)

  }
}
