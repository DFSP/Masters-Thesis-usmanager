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

package pt.unl.fct.miei.usmanagement.manager.services.services.discovery.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.registrationservers.RegistrationServer;
import pt.unl.fct.miei.usmanagement.manager.registrationservers.RegistrationServers;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.services.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.services.eips.ElasticIpsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistrationServerService {

	private final ContainersService containersService;
	private final ElasticIpsService elasticIpsService;

	private final RegistrationServers registrationServers;

	private final int port;

	public RegistrationServerService(@Lazy ContainersService containersService, ElasticIpsService elasticIpsService,
									 RegistrationServers registrationServers, RegistrationProperties registrationProperties) {
		this.containersService = containersService;
		this.elasticIpsService = elasticIpsService;
		this.registrationServers = registrationServers;
		this.port = registrationProperties.getPort();
	}

	public RegistrationServer launchRegistrationServer(RegionEnum region) {
		return launchRegistrationServers(List.of(region)).get(0);
	}

	@Async
	public CompletableFuture<RegistrationServer> launchRegistrationServer(HostAddress hostAddress) {
		String registrationServerAddresses = getRegistrationServerAddresses();
		Map<String, String> dynamicLaunchParams = Map.of("${zone}", registrationServerAddresses);
		Container container = containersService.launchContainer(hostAddress, ServiceConstants.Name.REGISTRATION_SERVER,
			Collections.emptyList(), Collections.emptyMap(), dynamicLaunchParams);
		return CompletableFuture.completedFuture(saveRegistrationServer(container));
	}

	public List<RegistrationServer> launchRegistrationServers(List<RegionEnum> regions) {
		log.info("Launching registration servers at regions {}", regions);

		List<CompletableFuture<RegistrationServer>> futureRegistrationServers = regions.stream().map(region -> {
			List<RegistrationServer> regionRegistrationServers = getRegistrationServer(region);
			if (regionRegistrationServers.size() > 0) {
				return CompletableFuture.completedFuture(regionRegistrationServers.get(0));
			}
			else {
				HostAddress hostAddress = elasticIpsService.getHost(region);
				return launchRegistrationServer(hostAddress);
			}
		}).collect(Collectors.toList());

		CompletableFuture.allOf(futureRegistrationServers.toArray(new CompletableFuture[0])).join();

		List<RegistrationServer> registrationServers = new ArrayList<>();
		for (CompletableFuture<RegistrationServer> futureRegistrationServer : futureRegistrationServers) {
			RegistrationServer registrationServer = futureRegistrationServer.join();
			registrationServers.add(registrationServer);
		}

		return registrationServers;
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

	public RegistrationServer getRegistrationServerByContainer(Container container) {
		return registrationServers.getByContainer(container).orElseThrow(() ->
			new EntityNotFoundException(RegistrationServer.class, "containerEntity", container.getId()));
	}

	private String getRegistrationServerAddresses() {
		return elasticIpsService.getElasticIps().stream()
			.map(address -> String.format("http://%s:%s/eureka", address.getPublicIp(), port))
			.collect(Collectors.joining(","));
	}

	public Optional<String> getRegistrationServerAddress(RegionEnum region) {
		return getRegistrationServer(region)
			.stream()
			.map(registrationServer -> registrationServer.getContainer().getAddress())
			.findFirst();
	}

	public void stopRegistrationServer(String id) {
		RegistrationServer registrationServer = getRegistrationServer(id);
		String containerId = registrationServer.getContainer().getId();
		registrationServers.delete(registrationServer);
		containersService.stopContainer(containerId);
	}

	public void reset() {
		registrationServers.deleteAll();
	}

	public void deleteRegistrationServerByContainer(Container container) {
		RegistrationServer registrationServer = getRegistrationServerByContainer(container);
		registrationServers.delete(registrationServer);
	}

	public RegistrationServer saveRegistrationServer(Container container) {
		return registrationServers.getByContainer(container).orElseGet(() ->
			registrationServers.save(RegistrationServer.builder().container(container).region(container.getRegion()).build()));
	}

	public boolean hasRegistrationServer(Container container) {
		return registrationServers.hasRegistrationServerByContainer(container.getId());
	}
}
