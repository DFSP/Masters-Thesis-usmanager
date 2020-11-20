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

package pt.unl.fct.miei.usmanagement.manager.management.services.discovery.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.config.ParallelismProperties;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.eips.ElasticIpsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.registrationservers.RegistrationServer;
import pt.unl.fct.miei.usmanagement.manager.registrationservers.RegistrationServers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistrationServerService {

	public static final String REGISTRATION_SERVER = "registration-server";

	private final HostsService hostsService;
	private final ServicesService servicesService;
	private final ContainersService containersService;
	private final ElasticIpsService elasticIpsService;
	private final CloudHostsService cloudHostsService;

	private final RegistrationServers registrationServers;

	private final int port;
	private final int threads;

	public RegistrationServerService(HostsService hostsService, ServicesService servicesService,
									 @Lazy ContainersService containersService, ElasticIpsService elasticIpsService,
									 CloudHostsService cloudHostsService,
									 RegistrationServers registrationServers, RegistrationProperties registrationProperties, ParallelismProperties parallelismProperties) {
		this.hostsService = hostsService;
		this.servicesService = servicesService;
		this.containersService = containersService;
		this.elasticIpsService = elasticIpsService;
		this.cloudHostsService = cloudHostsService;
		this.registrationServers = registrationServers;
		this.port = registrationProperties.getPort();
		this.threads = parallelismProperties.getThreads();
	}

	public RegistrationServer launchRegistrationServer(RegionEnum region) {
		return launchRegistrationServers(List.of(region)).get(0);
	}

	public RegistrationServer launchRegistrationServer(HostAddress hostAddress) {
		String registrationServerAddresses = getRegistrationServerAddresses();
		Map<String, String> dynamicLaunchParams = Map.of("${zone}", registrationServerAddresses);

		Container container = containersService.launchContainer(hostAddress, REGISTRATION_SERVER,
			Collections.emptyList(), Collections.emptyMap(), dynamicLaunchParams);

		RegionEnum region = container.getHostAddress().getRegion();
		String instanceId = cloudHostsService.getCloudHostByAddress(hostAddress).getInstanceId();
		String allocationId = elasticIpsService.getElasticIp(region).getAllocationId();
		try {
			elasticIpsService.associateElasticIpAddress(region, allocationId, instanceId).get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new ManagerException("Failed to associate elastic ip address to the registration server: %s", e.getMessage());
		}

		RegistrationServer registrationServer = RegistrationServer.builder().container(container).region(region).build();
		return registrationServers.save(registrationServer);
	}

	public List<RegistrationServer> launchRegistrationServers(List<RegionEnum> regions) {
		log.info("Launching registration servers at regions {}", regions);

		double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(REGISTRATION_SERVER);

		List<String> customEnvs = Collections.emptyList();

		Map<String, String> customLabels = Collections.emptyMap();

		String registrationServerAddresses = getRegistrationServerAddresses();
		Map<String, String> dynamicLaunchParams = Map.of("${zone}", registrationServerAddresses);

		try {
			return new ForkJoinPool(threads).submit(() ->
				regions.parallelStream().map(region -> {
					List<RegistrationServer> regionRegistrationServers = getRegistrationServer(region);
					if (regionRegistrationServers.size() > 0) {
						return regionRegistrationServers.get(0);
					}
					else {
						HostAddress hostAddress = hostsService.getCapableHost(expectedMemoryConsumption, region);
						Container container = containersService.launchContainer(hostAddress, REGISTRATION_SERVER, customEnvs, customLabels, dynamicLaunchParams);
						RegistrationServer registrationServer = RegistrationServer.builder().container(container).region(region).build();
						return registrationServers.save(registrationServer);
					}
				}).collect(Collectors.toList())).get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new ManagerException("Unable to launch registration servers at regions %s: %s", regions, e.getMessage());
		}
	}

	public List<RegistrationServer> getRegistrationServers() {
		return registrationServers.findAll();
	}

	private List<RegistrationServer> getRegistrationServer(RegionEnum region) {
		return registrationServers.getByRegion(region);
	}

	public RegistrationServer getRegistrationServer(String id) {
		return registrationServers.findById(id).orElseThrow(() ->
			new EntityNotFoundException(RegistrationServer.class, "id", id));
	}

	private String getRegistrationServerAddresses() {
		return elasticIpsService.getElasticIps().stream()
			.map(address -> String.format("http://%s:%s/eureka/", address.getPublicIp(), port))
			.collect(Collectors.joining(","));
	}

	public Optional<String> getRegistrationServerAddress(RegionEnum region) {
		return getRegistrationServer(region)
			.stream()
			.map(registrationServer -> registrationServer.getContainer().getLabels().get(ContainerConstants.Label.SERVICE_ADDRESS))
			.findFirst();
	}

	public void stopRegistrationServer(String id) {
		RegistrationServer registrationServer = getRegistrationServer(id);
		Container container = registrationServer.getContainer();
		registrationServers.delete(registrationServer);
		containersService.stopContainer(container.getId());
	}

	public void reset() {
		registrationServers.deleteAll();
	}
}
