package com.adv.utils

object KafkaAvroUtils {

  def sendAvroBinMessage(fileInfo: KafkaAvroMessage,
                         producerConfig: KafkaProducer[String, GenericRecord],
                         sentFolderPath: String,
                         onSuccess: (String, String, String, String) => Unit,
                         onFailure: (String, String, Exception) => Unit): Unit = {
    try {
      val specificData: SpecificData = SpecificData.get();
      LoggerUtils.log.info("Sending Message in Avro Format " + fileInfo.avroMsg.toString)
      producerConfig.send(new ProducerRecord[String, GenericRecord](fileInfo.topic, fileInfo.kafkaID, fileInfo.avroMsg), new Callback() {
        override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = {
          if (e != null) {
            onFailure(fileInfo.kafkaID, fileInfo.topic, e)
          } else {
            onSuccess(fileInfo.kafkaID, fileInfo.topic, fileInfo.filePath, sentFolderPath)
          }
        }
      })
    } catch {
      case e: Exception =>
        e.printStackTrace()
        onFailure(fileInfo.kafkaID, fileInfo.topic, e)
    }
    ()
  }
}
