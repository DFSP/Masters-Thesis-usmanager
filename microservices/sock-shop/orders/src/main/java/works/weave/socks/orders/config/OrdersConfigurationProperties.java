package works.weave.socks.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.swagger.client.ApiException;
import io.swagger.client.api.AppsApi;
import io.swagger.client.model.App;

import java.net.URI;

@ConfigurationProperties
public class OrdersConfigurationProperties {
	private AppsApi apiInstance = new AppsApi();    
    
	private String getAppEndpoint(String appName) {	   
		try {
			App app = apiInstance.getAppsByName(appName);
			return app.getEndpoint();
		} catch (ApiException e) {           
			e.printStackTrace();
			return "";
		}	   
	}
    
    public URI getPaymentUri_V2() {    	
    	return new ServiceUri(getAppEndpoint("payment"), "/paymentAuth").toUri();
    }
    
    public URI getShippingUri_V2() {        
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
