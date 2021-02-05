package pt.unl.fct.miei.usmanagement.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pt.unl.fct.miei.usmanagement.manager.Mode;

@Getter
@Setter
@Configuration
@ConfigurationProperties("manager.services")
public class ManagerServicesConfiguration {

    private Mode mode;

}
