package org.bigdata.spark2.samples

import org.apache.spark.sql.SparkSession
import org.bigdata.spark2.util.InitSpark
import org.apache.spark.sql.functions.{min, max, avg}

object SqlFunctions extends InitSpark{

  def aggFunctions(spark: SparkSession): Unit =  {
    import spark.implicits._

    val simpleData = Seq(("James","Sales",3000),
      ("Michael","Sales",4600),
      ("Robert","Sales",4100),
      ("Maria","Finance",3000),
      ("Raman","Finance",3000),
      ("Scott","Finance",3300),
      ("Jen","Finance",3900),
      ("Jeff","Marketing",3000),
      ("Kumar","Marketing",2000)
    )
    val df = simpleData.toDF("employee_name","department","salary")
    df.show()
    df.groupBy("department").agg(("salary","min"), ("salary","max"), ("salary","avg") ).show(false)
    df.groupBy("department").agg(min("salary"),max("salary"),avg("salary") ).show(false)
    df.groupBy("department").agg(Map("salary"->"min", "salary"->"max", "salary"->"avg")).show(false)

  }

  def main(args: Array[String]) {
      aggFunctions(spark)
  }
}
