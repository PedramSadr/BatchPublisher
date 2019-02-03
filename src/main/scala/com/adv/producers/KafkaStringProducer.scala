package com.adv.producers

import java.io.File
import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer._

/** Creates kafka string producer with various properties.  */

object KafkaStringProducer {


  var producer: Option[KafkaProducer[String, String]] = None


  def createProducerSettings(conf: Config): KafkaProducer[String, String] = {
    val props = kafkaProducersSettings(conf)
    if (producer.isEmpty) {
      val stringProps = new Properties(props)
      stringProps.put("bootstrap.servers", conf.getString("adv.kafka.brokers"))
      stringProps.put("key.serializer", conf.getString("adv.kafka.key.serializer"))
      stringProps.put("value.serializer", conf.getString("adv.kafka.value.serializer"))
      stringProps.put("acks", conf.getString("adv.kafka.acks"))

      stringProps.put("retries", conf.getString("adv.kafka.retry.Kafka-retries"))
      stringProps.put("max.in.flight.requests.per.connection", conf.getString("adv.kafka.retry.max_in_flight_requests"))
      stringProps.put("retry.backoff.ms", conf.getString("adv.kafka.retry.retry_Backoff.ms"))
      stringProps.put("reconnect.backoff.ms", conf.getString("adv.kafka.retry.reconnect_Backoff.ms"))

      if (conf.getBoolean("adv.kafka.security-enabled")) {
        stringProps.put("security.protocol", conf.getString("adv.kafka.security.protocol"))
        stringProps.put("sasl.mechanism", conf.getString("adv.kafka.sasl.mechanism"))
        stringProps.put("sasl.kerberos.service", conf.getString("adv.kafka.sasl.kerberos.service"))
      }

      producer = Some(new KafkaProducer[String, String](stringProps))
    }

    producer.get //deliberate use of get
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

