package com.adv.utils

import java.io.{ByteArrayInputStream, File, ObjectInputStream}
import java.nio.file.{Files, Paths}
case class  KafkaAvroMessage(topic : String, avroMsg : SpecificRecordBase, kafkaID : String, filePath : String)
object AvroFileUtils extends App{

  def readFile(filePath: String): SpecificRecordBase = {
    SpecificData.get.addLogicalTypeConversion(new TimeConversions.TimestampConversion)
    SpecificData.get.addLogicalTypeConversion(new TimeConversions.DateConversion)
    SpecificData.get.addLogicalTypeConversion(new TimeConversions.TimeConversion)

      val bytes = Files.readAllBytes(Paths.get(filePath))
      val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
      val value = ois.readObject
      val tst = value.asInstanceOf[value.type]
      ois.close
    tst.asInstanceOf[SpecificRecordBase]

  }

  def readAvroFailedMessages(fileNames : List[File]) : List[KafkaAvroMessage] = {
    fileNames.map(f => {
      val tst = f.getAbsolutePath.substring(f.getAbsolutePath.indexOf("Avro_"), f.getAbsolutePath.length).split('_')
      KafkaAvroMessage(f.getParent.substring(f.getParent.lastIndexOf(File.separator) + 1, f.getParent.length),
        readFile(f.getAbsolutePath),
        tst(1) + "_" + tst(2),
        f.getAbsolutePath)
    }
    )
  }
}