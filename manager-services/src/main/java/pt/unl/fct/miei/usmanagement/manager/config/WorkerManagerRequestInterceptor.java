package pt.unl.fct.miei.usmanagement.manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import pt.unl.fct.miei.usmanagement.manager.services.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.users.User;
import pt.unl.fct.miei.usmanagement.manager.services.users.UsersService;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
public class WorkerManagerRequestInterceptor implements org.springframework.http.client.ClientHttpRequestInterceptor {

	private final MultiValueMap<String, String> headers;

	public WorkerManagerRequestInterceptor() {
		String username = "admin";
		String password = "admin";
		byte[] auth = String.format("%s:%s", username, password).getBytes();
		String basicAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
		this.headers = CollectionUtils.toMultiValueMap(Map.of("Authorization", List.of(basicAuthorization)));
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		request.getHeaders().addAll(headers);
		return execution.execute(request, body);
	}

}
