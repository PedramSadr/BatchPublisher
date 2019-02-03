package com.adv.batch.publisher

import java.io.File
import java.lang.Exception
import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.adv.producers.{KafkaStringProducer, SchemaRegistryAwareNativeProducer}
import com.adv.utils._
import com.adv.producers.KafkaStringProducer
import com.adv.utils._
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]): Unit = {
//    require(args.length == 1)
    //Passing Application config path

    if(!Files.exists(Paths.get(args(0)))){
      LoggerUtils.log.error("Application configuration file is not available.")
      System.exit(-100)
    }

    val conf = ConfigFactory.parseFile(new File(args(0)))
    val nativeProducerProps = KafkaStringProducer.createProducerSettings(conf)
    val avroProducerProps = SchemaRegistryAwareNativeProducer(conf)
    val ftdPath = conf.getString("adv.ftd.folder.location")
    val files = FileUtils.allFiles(new File(ftdPath)).partition(file => file.getAbsolutePath.contains("Avro_"))
    val jsonMessages = JsonFileUtills.readFailedMessages(files._2)
    val avroMessages = AvroFileUtils.readAvroFailedMessages(files._1)
    LoggerUtils.log.debug(s"Number of JSON files : ${jsonMessages.length}")
    val sentFolderPath = conf.getString("adv.sent.ftd.folder.location")
    try {
            jsonMessages.foreach(m => {
              KafkaJsonUtils.trySend(m, nativeProducerProps, sentFolderPath, onSuccess, onFailure)
            })
      avroMessages.foreach(m => {
        KafkaAvroUtils.sendAvroBinMessage(m, avroProducerProps, sentFolderPath, onSuccess, onFailure)
      }
      )
    }catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      nativeProducerProps.close()
      avroProducerProps.close()
    }


  }

  def onSuccess(key: String, topic: String, filePath: String, sentFolder: String): Unit = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(("yyyyMMdd-HHmmss"))
    val datePart = LocalDateTime.now.format(dateTimeFormatter)
    LoggerUtils.log.info(s"$key,${topic},$datePart,ACKNOWLEDGED")

    import java.nio.file.{Files, Paths}
    val sourcePath = Paths.get(filePath)
    val destPath = Paths.get(sentFolder + File.separator + filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length))

    Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING)
  }

  def onFailure(key: String, topic: String, e: Exception): Unit = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(("yyyyMMdd-HHmmss"))
    val datePart = LocalDateTime.now.format(dateTimeFormatter)
    e.printStackTrace()
    LoggerUtils.log.error(s"SEND FAILED : $key,${topic},$datePart, ${e.getMessage}")
  }
}

