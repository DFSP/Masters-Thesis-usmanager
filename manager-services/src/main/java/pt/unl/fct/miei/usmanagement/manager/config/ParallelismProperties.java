package pt.unl.fct.miei.usmanagement.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("parallelism")
public class ParallelismProperties {

	private int threads;

}
