package pt.unl.fct.miei.usmanagement.manager.config;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;

@Configuration
public class AwsInstanceProfileCredentials {

	@Bean
	public AmazonEC2 amazonEC2() {
		InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(true);
		return AmazonEC2ClientBuilder.standard()
			.withCredentials(provider)
			.withRegion(AwsRegion.EU_WEST_3.getZone())
			.build();
	}

}
