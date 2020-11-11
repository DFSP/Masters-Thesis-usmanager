package pt.unl.fct.miei.usmanagement.manager.management.monitoring;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerConstants;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.HostsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@org.springframework.stereotype.Service
public class ContainersRecoveryService {

	private static final int STOP_CONTAINER_RECOVERY_ON_FAILURES = 3;
	private static final long STOP_CONTAINER_RECOVERY_TIME_FRAME = TimeUnit.MINUTES.toMillis(10);
	private static final int FIND_AVAILABLE_HOST_RETRIES = 5;

	private final ServicesService servicesService;
	private final HostsService hostsService;
	private final ContainersService containersService;
	private final SshService sshService;

	public ContainersRecoveryService(ServicesService servicesService, HostsService hostsService,
									 ContainersService containersService, SshService sshService) {
		this.servicesService = servicesService;
		this.hostsService = hostsService;
		this.containersService = containersService;
		this.sshService = sshService;
	}

	void restoreCrashedContainers(List<Container> monitoringContainers, List<Container> synchronizedContainers) {
		monitoringContainers.parallelStream()
			.filter(container -> {
				try {
					return servicesService.getService(container.getServiceName()).getMinimumReplicas() > 0;
				} catch (EntityNotFoundException ignored) {
					return false;
				}
			})
			.filter(container -> synchronizedContainers.stream().noneMatch(c -> Objects.equals(c.getContainerId(), container.getContainerId())))
			.distinct()
			.forEach(this::restartContainerCloseTo);
	}

	List<ContainerRecovery> getContainerRecoveries(Container container) {
		String containerId = container.getContainerId();
		log.info("Recovering crashed container {}={}", container.getServiceName(), containerId);
		List<ContainerRecovery> recoveries = new ArrayList<>();
		String previousRecoveries = container.getLabels().get(ContainerConstants.Label.RECOVERY);
		if (previousRecoveries != null) {
			long currentTimestamp = System.currentTimeMillis();
			for (ContainerRecovery recovery : new Gson().fromJson(previousRecoveries, ContainerRecovery[].class)) {
				if (recovery.getTimestamp() + STOP_CONTAINER_RECOVERY_TIME_FRAME > currentTimestamp) {
					log.info("Adding previous recovery: {}", recovery);
					recoveries.add(recovery);
				} else {
					log.info("Ignoring previous recovery {} because it has expired", recovery);
				}
			}
		} else {
			log.info("This is the first known recovery on container {}", containerId);
		}
		return recoveries;
	}

	boolean shouldStopContainerRecovering(List<ContainerRecovery> recoveries) {
		int count = 0;
		long currentTimestamp = System.currentTimeMillis();
		for (ContainerRecovery recovery : recoveries) {
			if (recovery.getTimestamp() + STOP_CONTAINER_RECOVERY_TIME_FRAME < currentTimestamp) {
				count++;
			}
		}
		return count >= STOP_CONTAINER_RECOVERY_ON_FAILURES;
	}

	// Restarts the container on a host close to where it used to be running
	void restartContainerCloseTo(Container container) {
		String containerId = container.getContainerId();
		List<ContainerRecovery> recoveries = getContainerRecoveries(container);
		if (shouldStopContainerRecovering(recoveries)) {
			log.info("Stopping recovery of crashed container {} {}... crashed too many times in a short period of time",
				container.getServiceName(), containerId);
			return;
		}
		recoveries.add(new ContainerRecovery(containerId, System.currentTimeMillis()));
		Coordinates coordinates = container.getCoordinates();
		String serviceName = container.getServiceName();
		double expectedMemoryConsumption = servicesService.getExpectedMemoryConsumption(serviceName);
		List<HostAddress> excludedHosts = new LinkedList<>();
		for (int i = 0; i < FIND_AVAILABLE_HOST_RETRIES; i++) {
			HostAddress hostAddress = hostsService.getClosestCapableHostIgnoring(expectedMemoryConsumption, coordinates, excludedHosts);
			if (hostAddress.equals(container.getHostAddress())) {
				try {
					Map<String, String> labels = Map.of(
						ContainerConstants.Label.RECOVERY, new Gson().toJson(recoveries)
					);
					containersService.launchContainer(hostAddress, serviceName, Collections.emptyList(), labels);
				}
				catch (ManagerException e) {
					excludedHosts.add(hostAddress);
					log.error("Tried to recover container on unavailable host, retrying... ({}/{})", i, FIND_AVAILABLE_HOST_RETRIES);
				}
			}
		}
	}

	// Restarts the container on the same host
	void restartContainer(Container container) {
		String containerId = container.getContainerId();
		List<ContainerRecovery> recoveries = getContainerRecoveries(container);
		if (shouldStopContainerRecovering(recoveries)) {
			log.info("Stopping recovery of crashed container {} {}... crashed too many times in a short period of time",
				container.getServiceName(), containerId);
			return;
		}
		recoveries.add(new ContainerRecovery(containerId, System.currentTimeMillis()));
		HostAddress hostAddress = container.getHostAddress();
		String serviceName = container.getServiceName();
		containersService.launchContainer(hostAddress, serviceName);
	}

}
