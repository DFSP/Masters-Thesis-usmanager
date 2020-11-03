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

import org.springframework.boot.context.properties.ConfigurationProperties;
import pt.unl.fct.miei.usmanagement.manager.ApiException;
import pt.unl.fct.miei.usmanagement.manager.api.EndpointsApi;
import pt.unl.fct.miei.usmanagement.manager.model.Endpoint;

import java.net.URI;

@ConfigurationProperties
public class OrdersConfigurationProperties {

	private final EndpointsApi endpointsApi = new EndpointsApi();

	private String getAppEndpoint(String service) {
		try {
			Endpoint endpoint = endpointsApi.getServiceEndpoint(service);
			return endpoint.getEndpoint();
		}
		catch (ApiException e) {
			e.printStackTrace();
			return "";
		}
	}

	public URI getPaymentUri() {
		return new ServiceUri(getAppEndpoint("payment"), "/paymentAuth").toUri();
	}

	public URI getShippingUri() {
		return new ServiceUri(getAppEndpoint("shipping"), "/shipping").toUri();
	}

	private class ServiceUri {

		private final String hostname;
		private final String endpoint;

		private ServiceUri(String hostname, String endpoint) {
			this.hostname = hostname;
			this.endpoint = endpoint;
		}

		public URI toUri() {
			return URI.create(hostname + endpoint);
		}

		@Override
		public String toString() {
			return "ServiceUri{" + "hostname=" + hostname + ", endpoint=" + endpoint + "}";
		}
	}
}
