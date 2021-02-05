package pt.unl.fct.miei.usmanagement.manager.config;

import com.google.gson.Gson;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;

@Component
@ConfigurationPropertiesBinding
public class HostAddressConverter implements Converter<String, HostAddress> {

	@Override
	public HostAddress convert(String hostAddressJson) {
		return new Gson().fromJson(hostAddressJson, HostAddress.class);
	}

}
