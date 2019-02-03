package com.adv.utils

import java.io.File

case class  KafkaJsonMessage(topic : String, jsonMsg : String, kafkaID : String, filePath : String)
object JsonFileUtills {

  def readFile(filePath: String): String = {
    import Control._
     using(scala.io.Source.fromFile(filePath)) { source => {
      source.getLines.reduceLeft(_+_)
    }}
  }

  def readFailedMessages(fileNames : List[File]) : List[KafkaJsonMessage] = {
    fileNames.map(f => {
      KafkaJsonMessage(f.getParent.substring(f.getParent.lastIndexOf(File.separator) + 1, f.getParent.length),
          readFile(f.getAbsolutePath),
          f.getName.substring(0, f.getName.indexOf("_")),
          f.getAbsolutePath)
    }
    )
  }
}

object Control {
  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
}

