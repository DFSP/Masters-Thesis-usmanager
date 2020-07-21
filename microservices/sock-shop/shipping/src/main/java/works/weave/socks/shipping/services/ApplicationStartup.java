package works.weave.socks.shipping.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.swagger.client.ApiException;
import io.swagger.client.api.AppsApi;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * This event is executed as late as conceivably possible to indicate that the
     * application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        registerOnEureka();
    }

    private void registerOnEureka() {
        AppsApi apiInstance = new AppsApi();
        try {
            apiInstance.register();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

}