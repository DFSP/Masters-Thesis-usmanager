/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package works.weave.socks.orders.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Component
public final class RestProxyTemplate {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	RestTemplate restTemplate;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Value("${proxy.host:}")
	private String host;

	@Value("${proxy.port:}")
	private String port;

	@PostConstruct
	public void init() {
		if (host.isEmpty() || port.isEmpty()) {
			return;
		}
		int portNr = -1;
		try {
			portNr = Integer.parseInt(port);
		}
		catch (NumberFormatException e) {
			logger.error("Unable to parse the proxy port number");
		}
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		InetSocketAddress address = new InetSocketAddress(host, portNr);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
		factory.setProxy(proxy);

		restTemplate.setRequestFactory(factory);
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
}
