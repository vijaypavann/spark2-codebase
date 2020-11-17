package org.bigdata.spark2.sql

import org.apache.spark.sql.Column
import org.bigdata.spark2.util.InitSpark
import org.apache.spark.sql.functions.{col, datediff, to_date}

object Holidays extends InitSpark {

  def convertToDate(columnName: String): Column = {
    to_date(col(columnName), "dd.MM.yyyy")
  }

  def main(args: Array[String]): Unit = {

    val orderDataSqlQuery = "SELECT * FROM VALUES (1, '26.12.2012', '06.01.2013', 'IN'), " +
      "(2, '05.08.2020', '29.10.2020', 'CN') as order_data(id, date1, date2, country) "

    val dateDiffCol: Column = datediff(col("parsed_date2"), col("parsed_date1"))

    val order_df = spark.sql(orderDataSqlQuery)
      .withColumn("parsed_date1", convertToDate("date1"))
      .withColumn("parsed_date2", convertToDate("date2"))
      .withColumn("daysdiff", dateDiffCol).alias("od")

    order_df.show(false)

    val holidayListSqlQuery = "SELECT * FROM VALUES (1, '30.12.2012', 'IN'), (2, '31.12.2012', 'IN'), " +
      "(3, '01.01.2013', 'IN'), (4, '25.10.2020', 'CN'), (5, '01.01.2013', 'CN') " +
      "as holiday_list(hid, holiday_date, country)"

    val holiday_df = spark.sql(holidayListSqlQuery)
      .withColumn("parsed_holiday_date", convertToDate("holiday_date")).alias("hd")

    holiday_df.show(false)

    val joinCond = col("hd.parsed_holiday_date").geq(col("od.parsed_date1")) &&
      col("hd.parsed_holiday_date").leq(col("od.parsed_date2")) &&
      col("hd.country").equalTo(col("od.country"))

    val holiday_parsed_df = holiday_df.join(order_df, joinCond)
      .groupBy("parsed_date2", "parsed_date1", "hd.country").count
      .withColumnRenamed("count", "holidays").alias("hdp")

    holiday_parsed_df.show(false)

    val dateJoinCond = col("hdp.parsed_date1").equalTo(col("od.parsed_date1")) &&
      col("hdp.parsed_date2").equalTo(col("od.parsed_date2")) &&
      col("hdp.country").equalTo(col("od.country"))

    val joinedDf = order_df.join(holiday_parsed_df, dateJoinCond)
      .withColumn("holidaysdiff", col("daysdiff").minus(col("holidays")) ).cache()

    joinedDf.show(false)

    joinedDf.select("id", "date1", "date2", "od.country", "daysdiff", "holidays", "holidaysdiff")
      .show(false)

  }
}
