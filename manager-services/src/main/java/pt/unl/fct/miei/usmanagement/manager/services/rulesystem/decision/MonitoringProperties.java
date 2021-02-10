package pt.unl.fct.miei.usmanagement.manager.services.rulesystem.decision;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties("monitoring")
public class MonitoringProperties {

	private final MonitoringProperties.Hosts hosts;
	private final MonitoringProperties.Services services;

	public MonitoringProperties() {
		this.hosts = new MonitoringProperties.Hosts();
		this.services = new MonitoringProperties.Services();
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	@Setter
	public static final class Hosts {

		private long period;
		private double maximumRamPercentage;
		private double maximumCpuPercentage;
		private int overworkEventCount;
		private int underworkEventCount;

	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	@Setter
	public static final class Services {

		private long period;
		private int replicateEventCount;
		private int migrateEventCount;
		private int stopEventCount;

	}
}
