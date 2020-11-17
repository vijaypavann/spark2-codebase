package learning_spark

import util.InitSpark

final case class Person(name: String, country: String, age: Int)

object PersonApp extends InitSpark with App {

    // implicit val empEncoder = Encoders.bean(Person)

    val emps = Seq( ("Vijay", "IN", 29), ("Pavan", "CN", 39), ("Kumar", "UK", 49) )

    // println(spark.version)

    val sumHundred = spark.range(1, 100).agg(("id", "sum"),("id", "avg"),("id", "max"))
    sumHundred.show()
    // println(f"Sum 1 to 100 = $sumHundred")

    // String : Dataset[Person]
    /*val personSchema = "name String, country String, age Int"
    val personDs  = spark.createDatase
    personDs.printSchema()
    personDs.show()*/
}

