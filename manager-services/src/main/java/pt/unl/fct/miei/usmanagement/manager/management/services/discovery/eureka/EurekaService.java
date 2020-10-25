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

package pt.unl.fct.miei.usmanagement.manager.management.services.discovery.eureka;

import com.google.gson.Gson;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EurekaService {

	public static final String EUREKA_SERVER = "eureka-server";

	private final HostsService hostsService;
	private final ServicesService serviceService;
	private final ContainersService containersService;
	private final int port;

	public EurekaService(HostsService hostsService, ServicesService serviceService,
						 @Lazy ContainersService containersService, EurekaProperties eurekaProperties) {
		this.hostsService = hostsService;
		this.serviceService = serviceService;
		this.containersService = containersService;
		this.port = eurekaProperties.getPort();
	}

	public Optional<String> getEurekaServerAddress(Region region) {
		return containersService.getContainersWithLabels(Set.of(
			Pair.of(ContainerConstants.Label.SERVICE_NAME, EUREKA_SERVER),
			Pair.of(ContainerConstants.Label.SERVICE_REGION, new Gson().toJson(region))))
			.stream()
			.map(container -> container.getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS))
			.findFirst();
	}

	public ContainerEntity launchEurekaServer(Region region) {
		return launchEurekaServers(List.of(region)).get(0);
	}

	public List<ContainerEntity> launchEurekaServers(List<Region> regions) {
		double expectedMemoryConsumption = serviceService.getService(EUREKA_SERVER).getExpectedMemoryConsumption();
		List<HostAddress> availableHosts = regions.stream()
			.map(region -> hostsService.getClosestCapableHost(expectedMemoryConsumption, region))
			.collect(Collectors.toList());
		List<String> customEnvs = Collections.emptyList();
		Map<String, String> customLabels = Collections.emptyMap();
		String eurekaServers = availableHosts.stream()
			.map(hostAddress -> String.format("http://%s:%s/eureka/", hostAddress.getPublicIpAddress(), port))
			.collect(Collectors.joining(","));
		Map<String, String> dynamicLaunchParams = Map.of("${zone}", eurekaServers);
		return availableHosts.stream()
			.map(hostAddress ->
				containersService.launchContainer(hostAddress, EUREKA_SERVER, customEnvs, customLabels, dynamicLaunchParams))
			.collect(Collectors.toList());
	}

}
