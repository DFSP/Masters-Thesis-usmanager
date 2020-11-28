/*
package pt.unl.fct.miei.usmanagement.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	private final KafkaTemplate<String, String> template;
	private final CountDownLatch latch = new CountDownLatch(3);

	public Application(KafkaTemplate<String, String> template) {
		this.template = template;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args).close();
	}

	@Override
	public void run(String... args) throws Exception {
		this.template.send("test", "foo1");
		this.template.send("test", "foo2");
		this.template.send("test", "foo3");
		latch.await(60, TimeUnit.SECONDS);
		log.info("All received");
	}

	@KafkaListener(topics = "test")
	public void listen(ConsumerRecord<?, ?> cr) {
		log.info(cr.toString());
		latch.countDown();
	}

}
*/
