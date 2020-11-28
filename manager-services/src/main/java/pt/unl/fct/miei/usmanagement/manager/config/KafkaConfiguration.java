package pt.unl.fct.miei.usmanagement.manager.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonSerializer;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.KafkaService;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

	private final KafkaService kafkaService;

	public KafkaConfiguration(KafkaService kafkaService) {
		this.kafkaService = kafkaService;
	}

	@Bean
	public KafkaAdmin admin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		KafkaAdmin kafkaAdmin = new KafkaAdmin(configs);
		kafkaAdmin.setBootstrapServersSupplier(kafkaService::getKafkaBrokersHosts);
		return kafkaAdmin;
	}

}
