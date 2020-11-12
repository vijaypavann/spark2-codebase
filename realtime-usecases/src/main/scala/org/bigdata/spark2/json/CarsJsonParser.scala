package org.bigdata.spark2.json

import org.apache.spark.sql.Row
import org.bigdata.spark2.util.InitSpark

case class PersonCar(name: String, age: Long, car_model: String)

object CarsJsonParser extends InitSpark {

  import  spark.implicits._

  private  def getFieldIndex(name: String, row: Row): Int = {
    row.fieldIndex(name)
  }

  private  def getName(row: Row): String = {
    row.getString(getFieldIndex("name", row))
  }

  private  def getAge(row: Row): Long = {
    row.getLong(getFieldIndex("age", row))
  }

  def main(args: Array[String]): Unit = {
    val jsonPath = "realtime-usecases/src/main/resources/json/nested/cars/cars.json"
    val personCarsDF = spark.read.option("multiline","true").json(jsonPath)

    // getName(row) + getAge(row)
    personCarsDF.flatMap(row =>  {
      // row.getStruct(row.fieldIndex("cars"))
      val carsRow = row.getSeq[Row](row.fieldIndex("cars"))
      carsRow.flatMap(car => {
        val brand = car.getString(1)
        var models = car.getSeq[String](0)
        // Seq(PersonCar(getName(row), getAge(row), brand+"--"))
        var personCarList: Seq[PersonCar] = Seq()
        models.foreach(model => {
          personCarList = personCarList :+ PersonCar(getName(row), getAge(row), brand+"--"+model)
        } )
        personCarList
      })
      // carsRow.mkString(",")
    } ).show(false)

    // personCarsDF.show(false)
  }
}
