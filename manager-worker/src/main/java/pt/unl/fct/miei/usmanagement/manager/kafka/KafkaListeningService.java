package pt.unl.fct.miei.usmanagement.manager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaListeningService {

	@KafkaListener(groupId = "manager", topics = "apps", autoStartup = "false")
	public void listenApps(ConsumerRecord<String, Object> cr) {
		log.info(cr.toString());
	}

	@KafkaListener(groupId = "manager", topics = "services", autoStartup = "false")
	public void listenServices(ConsumerRecord<String, Object> cr) {
		log.info(cr.toString());
	}

}
