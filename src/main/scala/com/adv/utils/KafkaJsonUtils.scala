package com.adv.utils

object KafkaJsonUtils {

def trySend(fileInfo: KafkaJsonMessage,kafkaProducer: KafkaProducer[String, String],
                       sentFolderPath: String,
                       onSuccess: (String, String, String, String) => Unit,
                       onFailure: (String, String, Exception) => Unit): Unit = {
    try {
      kafkaProducer.send(new ProducerRecord[String, String](fileInfo.topic, fileInfo.kafkaID, fileInfo.jsonMsg), new Callback() {
        override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = {
          if (e != null) {
            onFailure(fileInfo.kafkaID, fileInfo.topic, e)
          }
          else {
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
