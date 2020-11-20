package pt.unl.fct.miei.usmanagement.manager.management.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.configurations.Configuration;
import pt.unl.fct.miei.usmanagement.manager.configurations.Configurations;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;

import java.util.List;

@Slf4j
@Service
public class ConfigurationsService {

	private final Configurations configurations;

	public ConfigurationsService(Configurations configurations) {
		this.configurations = configurations;
	}

	public Configuration addConfiguration(String id) {
		log.info("Saving configuration {}", id);
		return configurations.save(Configuration.builder().id(id).build());
	}

	public Configuration addConfiguration(Configuration configuration) {
		return configurations.save(configuration);
	}

	public List<Configuration> getConfigurations() {
		return configurations.findAll();
	}

	public Configuration getConfiguration(String id) {
		return configurations.findById(id).orElseThrow(() -> new EntityNotFoundException(Configuration.class, "id", id));
	}

	public void removeConfiguration(String id) {
		log.info("Removing configuration {}", id);
		configurations.deleteById(id);
	}

	public void removeConfiguration(Configuration configuration) {
		log.info("Removing configuration {}", configuration.getId());
		configurations.delete(configuration);
	}

	public boolean isConfiguring(String id) {
		return configurations.isConfiguring(id);
	}

	public void reset() {
		configurations.deleteAll();
	}
}
