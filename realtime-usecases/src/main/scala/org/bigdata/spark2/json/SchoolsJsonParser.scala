package org.bigdata.spark2.json

import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.types.{ArrayType, DoubleType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.functions.{explode, col}
import org.bigdata.spark2.util.InitSpark

case class PersonSchool(name: String, school: String, year: Long)
case class PersonSchoolMarks(name: String, school: String, year: Long, english: Double, maths: Double)

object SchoolsJsonParser extends InitSpark {

  val NULLS_EXPECTED: Boolean = true

  private def parseStructJson(): Unit = {

    val jsonDataPath = "realtime-usecases/src/main/resources/json/nested/schools/struct/"
    val encoder = ExpressionEncoder[PersonSchoolMarks]

    val eng = StructField("english", DoubleType, NULLS_EXPECTED)
    val maths = StructField("maths", DoubleType, NULLS_EXPECTED)
    val structType = StructType(Array(eng, maths))
    val sname = StructField("sname", StringType, NULLS_EXPECTED)
    val year = StructField("year", LongType, NULLS_EXPECTED)
    val marks = StructType( Array(
      StructField("marks", ArrayType(structType, NULLS_EXPECTED), NULLS_EXPECTED),
      sname, year
    ))

    val schoolsSchema = new StructType(Array(
      StructField("schools", ArrayType(marks, NULLS_EXPECTED), NULLS_EXPECTED),
      StructField("name", StringType, NULLS_EXPECTED),
    ))
    // schoolsSchema.printTreeString()
    // println(schoolsSchema.treeString)

    val jsonDF = spark.read.schema(schoolsSchema).json(jsonDataPath)

    jsonDF.printSchema()
    jsonDF.show(false)

    val parsedJson:Dataset[PersonSchoolMarks] = jsonDF.flatMap(row => {
      val name = row.getString(row.fieldIndex("name"))
      val schools = row.getSeq[Row](row.fieldIndex("schools"))
      schools.flatMap(schoolRow => {
        val school = schoolRow.getString(schoolRow.fieldIndex("sname"))
        val year = schoolRow.getLong(schoolRow.fieldIndex("year"))
        val marks = schoolRow.getSeq[Row](schoolRow.fieldIndex("marks"))
        marks.map(marksRow => {
          val englishMarks = marksRow.getDouble(marksRow.fieldIndex("english"))
          val mathsMarks = marksRow.getDouble(marksRow.fieldIndex("maths"))
          PersonSchoolMarks(name, school, year, englishMarks, mathsMarks)
        })
      })
    })(encoder)

    parsedJson.printSchema()
    parsedJson.show(false)
  }

  private def parseBasicJson(): Unit = {

    val jsonDataPath = "realtime-usecases/src/main/resources/json/nested/schools/basic/"
    val personSchoolEncoder = ExpressionEncoder[PersonSchool]

    val school = StructType(Array(
      StructField("sname", StringType, NULLS_EXPECTED),
      StructField("year", LongType, NULLS_EXPECTED)
    ))
    val basicSchema = StructType(Array(
      StructField("name", StringType, NULLS_EXPECTED),
      StructField("schools", ArrayType(school, NULLS_EXPECTED), NULLS_EXPECTED)
    ))

    val df = spark.read.schema(basicSchema).json(jsonDataPath)
    df.printSchema()
    df.show(false)

    val personSchoolDs = df.flatMap(row => {
       val name = row.getString(row.fieldIndex("name"))
       val schools = row.getSeq[Row](row.fieldIndex("schools"))
      schools.map(schoolRow => {
        val school = schoolRow.getString(schoolRow.fieldIndex("sname"))
        val year = schoolRow.getLong(schoolRow.fieldIndex("year"))
        PersonSchool(name, school, year)
      })
    }) (personSchoolEncoder)

    personSchoolDs.printSchema()
    personSchoolDs.show(false)
  }

  private def explodeLogic(): Unit = {

    val jsonDataPath = "realtime-usecases/src/main/resources/json/nested/schools/struct/"
    val encoder = ExpressionEncoder[PersonSchoolMarks]

    val baseDf = spark.read.json(jsonDataPath)
    baseDf.printSchema()
    baseDf.show(false)

    val df = baseDf.withColumn("schools", explode(col("schools")))
    val parsedDf = df.flatMap(row => {
      val name = row.getString(row.fieldIndex("name"))
      val schools = row.getStruct(row.fieldIndex("schools"))
      val school = schools.getString(schools.fieldIndex("sname"))
      val year = schools.getLong(schools.fieldIndex("year"))
      val marks = schools.getSeq[Row](schools.fieldIndex("marks"))
      marks.map(subject => {
        val engMarks = subject.getDouble(subject.fieldIndex("english"))
        val mathMarks = subject.getDouble(subject.fieldIndex("maths"))
        PersonSchoolMarks(name, school, year, engMarks, mathMarks)
      })
    })(encoder)

    parsedDf.printSchema()
    parsedDf.show(false)

  }
  def main(args: Array[String]): Unit = {

    // parseStructJson()

    // parseBasicJson()

    explodeLogic()

    /*jsonDF.createOrReplaceTempView("jsonTable");

    spark.sql("SELECT * FROM jsonTable").show(false);*/

    spark.close()

  }
}
