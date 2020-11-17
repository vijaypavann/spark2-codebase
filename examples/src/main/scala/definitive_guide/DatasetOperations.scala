package definitive_guide

import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.functions.{col, expr, lit}
import util.InitSpark

case class Customer(customerName: String, customerAge: Int)

object DatasetOperations extends InitSpark{

  val NULLS_EXPECTED: Boolean = true

  def main(args: Array[String]): Unit = {

    val srcFile: String = "examples/src/main/resources/customer_data/"

    val customerEncoder: Encoder[Customer] = ExpressionEncoder[Customer]

    //Encoders.bean[Customer]((Customer.getClass.cast(Customer)))

    val tupleEncoder: Encoder[(String, Integer)] = Encoders.tuple(Encoders.STRING, Encoders.INT)

    val customerSchema = new StructType(
      Array(
      StructField("customerName", StringType, NULLS_EXPECTED),
      StructField("customerAge", IntegerType, NULLS_EXPECTED) ) )

    val customerDS = spark.read.schema(customerSchema).csv(srcFile).as[Customer](customerEncoder).cache()

    customerDS.printSchema()
    customerDS.explain(true)
    println(customerDS.count())

    customerDS.filter(recd => recd.customerAge > 50).show()

    val oldCustomers = customerDS.selectExpr("customerName", "customerAge",
      "(customerAge > 50) as oldCustomers").filter("oldCustomers")

    oldCustomers.show()

    val mapped = oldCustomers.map { recd =>
      (recd.getString(0), Integer.valueOf(recd.getInt(1)) )
    } (tupleEncoder)
      .withColumnRenamed("_1", "customer_name")
      .withColumnRenamed("_2", "customer_age")

    mapped.show()

    mapped.write.csv("out/dataset-operations")
    spark.stop()
  }
}