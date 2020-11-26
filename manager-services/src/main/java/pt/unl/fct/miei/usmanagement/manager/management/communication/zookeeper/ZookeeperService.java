package pt.unl.fct.miei.usmanagement.manager.management.communication.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceConstants;
import pt.unl.fct.miei.usmanagement.manager.zookeeper.Zookeeper;
import pt.unl.fct.miei.usmanagement.manager.zookeeper.Zookeepers;

import java.util.List;

@Slf4j
@Service
public class ZookeeperService {

	private final ContainersService containersService;

	private final Zookeepers zookeepers;

	public ZookeeperService(ContainersService containersService, Zookeepers zookeepers) {
		this.containersService = containersService;
		this.zookeepers = zookeepers;
	}

	public Zookeeper launchZookeeper(HostAddress hostAddress) {
		RegionEnum region = hostAddress.getRegion();
		Container container = containersService.launchContainer(hostAddress, ServiceConstants.Name.ZOOKEEPER);
		Zookeeper zookeeper = Zookeeper.builder().container(container).region(region).build();
		return zookeepers.save(zookeeper);
	}

	public List<Zookeeper> getZookeepers() {
		return zookeepers.findAll();
	}

	public List<Zookeeper> getZookeepers(RegionEnum region) {
		return zookeepers.getByRegion(region);
	}

	public Zookeeper getZookeeper(String id) {
		return zookeepers.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Zookeeper.class, "id", id));
	}

	public Zookeeper getZookeeperByContainer(Container container) {
		return zookeepers.getByContainer(container).orElseThrow(() ->
			new EntityNotFoundException(Zookeeper.class, "containerEntity", container.getId()));
	}

	public void stopZookeeper(String id) {
		Zookeeper zookeeper = getZookeeper(id);
		String containerId = zookeeper.getContainer().getId();
		zookeepers.delete(zookeeper);
		containersService.stopContainer(containerId);
	}

}
