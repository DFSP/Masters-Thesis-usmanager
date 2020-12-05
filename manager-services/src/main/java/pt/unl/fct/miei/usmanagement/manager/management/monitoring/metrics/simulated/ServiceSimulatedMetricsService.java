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

package pt.unl.fct.miei.usmanagement.manager.management.monitoring.metrics.simulated;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.management.communication.kafka.KafkaService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ContainerSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.ServiceSimulatedMetrics;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;
import java.util.Random;

@Slf4j
@org.springframework.stereotype.Service
public class ServiceSimulatedMetricsService {

	private final ServicesService servicesService;
	private final KafkaService kafkaService;

	private final ServiceSimulatedMetrics serviceSimulatedMetrics;

	public ServiceSimulatedMetricsService(@Lazy ServicesService servicesService,
										  KafkaService kafkaService, ServiceSimulatedMetrics serviceSimulatedMetrics) {
		this.servicesService = servicesService;
		this.kafkaService = kafkaService;
		this.serviceSimulatedMetrics = serviceSimulatedMetrics;
	}

	public List<ServiceSimulatedMetric> getServiceSimulatedMetrics() {
		return serviceSimulatedMetrics.findAll();
	}

	public List<ServiceSimulatedMetric> getServiceSimulatedMetricByService(String serviceName) {
		return serviceSimulatedMetrics.findByService(serviceName);
	}

	public ServiceSimulatedMetric getServiceSimulatedMetric(Long id) {
		return serviceSimulatedMetrics.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ServiceSimulatedMetric.class, "id", id.toString()));
	}

	public ServiceSimulatedMetric getServiceSimulatedMetric(String simulatedMetricName) {
		return serviceSimulatedMetrics.findByNameIgnoreCase(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ServiceSimulatedMetric.class, "simulatedMetricName", simulatedMetricName));
	}

	public ServiceSimulatedMetric addServiceSimulatedMetric(ServiceSimulatedMetric serviceSimulatedMetric) {
		checkServiceSimulatedMetricDoesntExist(serviceSimulatedMetric);
		log.info("Saving simulated service metric {}", ToStringBuilder.reflectionToString(serviceSimulatedMetric));
		serviceSimulatedMetric = saveServiceSimulatedMetric(serviceSimulatedMetric);
		kafkaService.sendServiceSimulatedMetric(serviceSimulatedMetric);
		return serviceSimulatedMetric;
	}

	public ServiceSimulatedMetric updateServiceSimulatedMetric(String simulatedMetricName,
															   ServiceSimulatedMetric newServiceSimulatedMetric) {
		log.info("Updating simulated service metric {} with {}", simulatedMetricName,
			ToStringBuilder.reflectionToString(newServiceSimulatedMetric));
		ServiceSimulatedMetric serviceSimulatedMetric = getServiceSimulatedMetric(simulatedMetricName);
		ObjectUtils.copyValidProperties(newServiceSimulatedMetric, serviceSimulatedMetric);
		serviceSimulatedMetric = saveServiceSimulatedMetric(serviceSimulatedMetric);
		kafkaService.sendServiceSimulatedMetric(serviceSimulatedMetric);
		return serviceSimulatedMetric;
	}

	public ServiceSimulatedMetric saveServiceSimulatedMetric(ServiceSimulatedMetric serviceSimulatedMetric) {
		return serviceSimulatedMetrics.save(serviceSimulatedMetric);
	}

	public void deleteServiceSimulatedMetric(String simulatedMetricName) {
		log.info("Deleting simulated service metric {}", simulatedMetricName);
		ServiceSimulatedMetric serviceSimulatedMetric = getServiceSimulatedMetric(simulatedMetricName);
		serviceSimulatedMetric.removeAssociations();
		serviceSimulatedMetrics.delete(serviceSimulatedMetric);
	}

	public List<ServiceSimulatedMetric> getGenericServiceSimulatedMetrics() {
		return serviceSimulatedMetrics.findGenericServiceSimulatedMetrics();
	}

	public ServiceSimulatedMetric getGenericServiceSimulatedMetric(String simulatedMetricName) {
		return serviceSimulatedMetrics.findGenericServiceSimulatedMetric(simulatedMetricName).orElseThrow(() ->
			new EntityNotFoundException(ServiceSimulatedMetric.class, "simulatedMetricName", simulatedMetricName));
	}

	public List<Service> getServices(String simulatedMetricName) {
		checkServiceSimulatedMetricExists(simulatedMetricName);
		return serviceSimulatedMetrics.getServices(simulatedMetricName);
	}

	public Service getService(String simulatedMetricName, String serviceName) {
		checkServiceSimulatedMetricExists(simulatedMetricName);
		return serviceSimulatedMetrics.getService(simulatedMetricName, serviceName).orElseThrow(() ->
			new EntityNotFoundException(Service.class, "serviceName", serviceName));
	}

	public void addService(String simulatedMetricName, String serviceName) {
		addServices(simulatedMetricName, List.of(serviceName));
	}

	public void addServices(String simulatedMetricName, List<String> serviceNames) {
		log.info("Adding services {} to simulated metric {}", serviceNames, simulatedMetricName);
		ServiceSimulatedMetric serviceMetric = getServiceSimulatedMetric(simulatedMetricName);
		serviceNames.forEach(serviceName -> {
			Service service = servicesService.getService(serviceName);
			service.addServiceSimulatedMetric(serviceMetric);
		});
		ServiceSimulatedMetric serviceSimulatedMetric = saveServiceSimulatedMetric(serviceMetric);
		kafkaService.sendServiceSimulatedMetric(serviceSimulatedMetric);
	}

	public void removeService(String simulatedMetricName, String serviceName) {
		removeServices(simulatedMetricName, List.of(serviceName));
	}

	public void removeServices(String simulatedMetricName, List<String> serviceNames) {
		log.info("Removing services {} from simulated metric {}", serviceNames, simulatedMetricName);
		ServiceSimulatedMetric serviceMetric = getServiceSimulatedMetric(simulatedMetricName);
		serviceNames.forEach(serviceName ->
			servicesService.getService(serviceName).removeServiceSimulatedMetric(serviceMetric));
		ServiceSimulatedMetric serviceSimulatedMetric = saveServiceSimulatedMetric(serviceMetric);
		kafkaService.sendServiceSimulatedMetric(serviceSimulatedMetric);
	}

	public Double randomizeFieldValue(ServiceSimulatedMetric serviceSimulatedMetric) {
		Random random = new Random();
		double minValue = serviceSimulatedMetric.getMinimumValue();
		double maxValue = serviceSimulatedMetric.getMaximumValue();
		return minValue + (maxValue - minValue) * random.nextDouble();
	}

	private void checkServiceSimulatedMetricExists(String simulatedMetricName) {
		if (!serviceSimulatedMetrics.hasServiceSimulatedMetric(simulatedMetricName)) {
			throw new EntityNotFoundException(ServiceSimulatedMetric.class, "simulatedMetricName", simulatedMetricName);
		}
	}

	private void checkServiceSimulatedMetricDoesntExist(ServiceSimulatedMetric serviceSimulatedMetric) {
		String name = serviceSimulatedMetric.getName();
		if (serviceSimulatedMetrics.hasServiceSimulatedMetric(name)) {
			throw new DataIntegrityViolationException("Simulated service metric '" + name + "' already exists");
		}
	}
}
