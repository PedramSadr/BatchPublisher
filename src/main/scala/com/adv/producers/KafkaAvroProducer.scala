package com.adv.producers

import java.util.Properties

import com.typesafe.config.Config
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer

object SchemaRegistryAwareNativeProducer {

  var avroProducer: Option[KafkaProducer[String, GenericRecord]] = None

  def apply(conf: Config) : KafkaProducer[String, GenericRecord] = {
    if (avroProducer.isEmpty) {
      val avroProps = kafkaProducersSettings(conf)
      avroProps.put("bootstrap.servers", conf.getString("adv.kafka.brokers"))
      avroProps.put("key.serializer", conf.getString("adv.kafka.key.serializer"))
      avroProps.put("value.serializer", conf.getString("adv.kafka.value.avro.serializer"))

      avroProps.put("schema.registry.url", conf.getString("adv.kafka.schema-registry"))
      avroProps.put("retries", conf.getString("adv.kafka.retry.Kafka-retries"))
      avroProps.put("max.in.flight.requests.per.connection", conf.getString("adv.kafka.retry.max_in_flight_requests"))
      avroProps.put("retry.backoff.ms", conf.getString("adv.kafka.retry.retry_Backoff.ms"))
      avroProps.put("reconnect.backoff.ms", conf.getString("adv.kafka.retry.reconnect_Backoff.ms"))

      if (conf.getBoolean("adv.kafka.security-enabled")) {
        avroProps.put("security.protocol", conf.getString("adv.kafka.security.protocol"))
        avroProps.put("sasl.mechanism", conf.getString("adv.kafka.sasl.mechanism"))
        avroProps.put("sasl.kerberos.service", conf.getString("adv.kafka.sasl.kerberos.service"))
      }

      avroProducer = Some(new KafkaProducer[String, GenericRecord](avroProps))
    }
    avroProducer.get  //deliberate use of get
  }


  def kafkaProducersSettings(conf: Config) : Properties = {
    val props = new Properties()
    props.put("batch.size", conf.getString("adv.kafka.batch.size"))
    props.put("linger.ms", conf.getString("adv.kafka.linger.ms"))
    props.put("buffer.memory", conf.getString("adv.kafka.buffer.memory"))
    props.put("request.timeout.ms", conf.getString("adv.kafka.buffer.memory")) //default is 30000
    props
  }
}



