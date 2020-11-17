package org.bigdata.spark2.json

import org.apache.spark.sql.types.{ArrayType, LongType, Metadata, StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row}
import org.bigdata.spark2.util.InitSpark

case class PersonCar(name: String, age: Long, car_model: String)

object CarsJsonParser extends InitSpark {

  import  spark.implicits._
  val NULLS_EXPECTED: Boolean = true
  private  def getFieldIndex(name: String, row: Row): Int = {
    row.fieldIndex(name)
  }

  private  def getName(row: Row): String = {
    row.getString(getFieldIndex("name", row))
  }

  private  def getAge(row: Row): Long = {
    row.getLong(getFieldIndex("age", row))
  }

  def parseData(personCarsDF: Dataset[Row]): Unit = {

    val formattedDs = personCarsDF.flatMap(row =>  {
      val carsRow = row.getSeq[Row](row.fieldIndex("cars"))
      carsRow.flatMap(car => {
        val brand = car.getString(1)
        val models = car.getSeq[String](0)
        // var personCarList: Seq[PersonCar] = Seq()
        models.map(model => {
          // personCarList = personCarList :+
            PersonCar(getName(row), getAge(row), brand+"--"+model)
        })
        //personCarList
      })
    })
    formattedDs.show(false)
    formattedDs.printSchema()
    // formattedDs.write.partitionBy("age").parquet("personcars-dataset")
  }

  def main(args: Array[String]): Unit = {

    val jsonPath = "realtime-usecases/src/main/resources/json/nested/cars/cars.json"

    val models: StructField = StructField("models", ArrayType(StringType, NULLS_EXPECTED), NULLS_EXPECTED, Metadata.empty)
    val carType: StructType = new StructType().add(models)
      .add(StructField("name", StringType, NULLS_EXPECTED, Metadata.empty))
    val cars: StructField = StructField("cars", ArrayType(carType, NULLS_EXPECTED), NULLS_EXPECTED, Metadata.empty)

    val schema: StructType = new StructType()
      .add(StructField("name", StringType, NULLS_EXPECTED, Metadata.empty))
      .add(StructField("age", LongType, NULLS_EXPECTED, Metadata.empty))
      .add(cars)

    val personCarsDF = spark.read.schema(schema).option("multiline","true").json(jsonPath)

    personCarsDF.show(false)
    personCarsDF.printSchema()
    //println(personCarsDF.schema)
    parseData(personCarsDF)
    // row.getStruct(row.fieldIndex("cars"))
  }
}
