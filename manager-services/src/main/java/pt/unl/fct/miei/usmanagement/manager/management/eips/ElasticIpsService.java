package pt.unl.fct.miei.usmanagement.manager.management.eips;

import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.ReleaseAddressResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIps;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.management.containers.ContainersService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.nodes.NodesService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws.AwsService;
import pt.unl.fct.miei.usmanagement.manager.management.regions.RegionsService;
import pt.unl.fct.miei.usmanagement.manager.nodes.Node;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticIpsService {

	private final RegionsService regionsService;
	private final AwsService awsService;
	private final CloudHostsService cloudHostsService;
	private final NodesService nodesService;
	private final ContainersService containersService;

	private final ElasticIps elasticIps;

	public ElasticIpsService(RegionsService regionsService, @Lazy AwsService awsService,
							 @Lazy CloudHostsService cloudHostsService, NodesService nodesService,
							 @Lazy ContainersService containersService, ElasticIps elasticIps) {
		this.regionsService = regionsService;
		this.awsService = awsService;
		this.cloudHostsService = cloudHostsService;
		this.nodesService = nodesService;
		this.containersService = containersService;
		this.elasticIps = elasticIps;
	}

	public List<ElasticIp> getElasticIps() {
		return elasticIps.findAll();
	}

	public ElasticIp getElasticIp(Long id) {
		return elasticIps.findById(id).orElseThrow(() ->
			new EntityNotFoundException(ElasticIp.class, "id", id.toString()));
	}

	public ElasticIp getElasticIp(RegionEnum regionEnum) {
		return elasticIps.getElasticIpByRegion(regionEnum).orElseThrow(() ->
			new EntityNotFoundException(ElasticIp.class, "region", regionEnum.name()));
	}

	public ElasticIp getElasticIp(String allocationId) {
		return elasticIps.getElasticIpByAllocationId(allocationId).orElseThrow(() ->
			new EntityNotFoundException(ElasticIp.class, "allocationId", allocationId));
	}

	public ElasticIp addElasticIp(ElasticIp elasticIp) {
		checkElasticIpDoesntExist(elasticIp);
		log.info("Saving elasticIp {}", ToStringBuilder.reflectionToString(elasticIp));
		return elasticIps.save(elasticIp);
	}

	public void clearElasticIps() {
		elasticIps.deleteAll();
	}

	private void checkElasticIpDoesntExist(ElasticIp elasticIp) {
		RegionEnum region = elasticIp.getRegion();
		if (elasticIps.hasElasticIp(region)) {
			throw new DataIntegrityViolationException("ElasticIp already exists on region " + region.name());
		}
	}

	@Async
	public CompletableFuture<String> allocateElasticIpAddress(RegionEnum region) {
		String allocationId = awsService.allocateElasticIpAddress(region);
		return CompletableFuture.completedFuture(allocationId);
	}

	public void allocateElasticIpAddresses() {
		Map<RegionEnum, List<Address>> addresses = getElasticIpAddresses();

		RegionEnum[] regions = RegionEnum.values();

		Map<RegionEnum, CompletableFuture<String>> futureElasticIpAddresses = new HashMap<>(regions.length);
		for (RegionEnum region : regions) {
			List<Address> regionAddresses = addresses.get(region);
			if (regionAddresses != null && regionAddresses.size() > 0 && regionAddresses.get(0).getAssociationId() == null) {
				Address address = regionAddresses.get(0);
				futureElasticIpAddresses.put(region, CompletableFuture.completedFuture(address.getAllocationId()));
			}
			else {
				futureElasticIpAddresses.put(region, allocateElasticIpAddress(region));
			}
		}

		CompletableFuture.allOf(futureElasticIpAddresses.values().toArray(new CompletableFuture[0])).join();

		for (Map.Entry<RegionEnum, CompletableFuture<String>> futureElasticIpAddress : futureElasticIpAddresses.entrySet()) {
			RegionEnum region = futureElasticIpAddress.getKey();
			try {
				futureElasticIpAddress.getValue().get();
			}
			catch (InterruptedException | ExecutionException e) {
				throw new ManagerException("Failed to allocate elastic ip address for region %s: %s", region, e.getMessage());
			}
		}

		Map<RegionEnum, Address> elasticIpAddresses = getElasticIpAddresses().entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

		elasticIpAddresses.forEach((region, address) -> {
			ElasticIp elasticIp = ElasticIp.builder().region(region)
				.allocationId(address.getAllocationId())
				.publicIp(address.getPublicIp())
				.build();
			addElasticIp(elasticIp);
		});
	}

	public CloudHost associateElasticIpAddress(RegionEnum region, String allocationId, CloudHost cloudHost) {
		ElasticIp elasticIp = getElasticIp(allocationId);
		String instanceId = cloudHost.getInstanceId();
		AssociateAddressResult associateResult = awsService.associateElasticIpAddress(region, allocationId, instanceId);
		String associationId = associateResult.getAssociationId();
		elasticIp.setAssociationId(associationId);
		elasticIp.setInstanceId(instanceId);
		elasticIps.save(elasticIp);
		log.info("Associated public ip address {} from elastic ip {} to cloud instance {} with association id {}",
			elasticIp.getPublicIp(), elasticIp.getAllocationId(), instanceId, associationId);
		HostAddress previousHostAddress = cloudHost.getAddress();
		AwsRegion awsRegion = regionsService.mapToAwsRegion(region);
		Instance instance = awsService.getInstance(instanceId, awsRegion);
		cloudHost = cloudHostsService.saveCloudHostFromInstance(cloudHost.getId(), instance);
		nodesService.updateAddress(previousHostAddress, elasticIp.getPublicIp());
		containersService.updateAddress(previousHostAddress, elasticIp.getPublicIp());
		return cloudHost;
	}

	private Map<RegionEnum, List<Address>> getElasticIpAddresses() {
		Map<RegionEnum, CompletableFuture<List<Address>>> futureElasticIpAddresses = new HashMap<>();
		for (RegionEnum region : RegionEnum.values()) {
			AwsRegion awsRegion = regionsService.mapToAwsRegion(region);
			futureElasticIpAddresses.put(region, awsService.getElasticIpAddresses(awsRegion));
		}

		CompletableFuture.allOf(futureElasticIpAddresses.values().toArray(new CompletableFuture[0])).join();

		Map<RegionEnum, List<Address>> elasticIpAddresses = new HashMap<>(futureElasticIpAddresses.size());
		for (Map.Entry<RegionEnum, CompletableFuture<List<Address>>> futureElasticIpAddress : futureElasticIpAddresses.entrySet()) {
			RegionEnum region = futureElasticIpAddress.getKey();
			try {
				List<Address> addresses = futureElasticIpAddress.getValue().get();
				elasticIpAddresses.put(region, addresses);
			}
			catch (InterruptedException | ExecutionException e) {
				log.error("Failed to get elastic ip addresses from region {}: {}", region, e.getMessage());
			}
		}

		return elasticIpAddresses;
	}

	@Async
	public CompletableFuture<ReleaseAddressResult> releaseElasticIpAddress(String allocationId, RegionEnum region) {
		ReleaseAddressResult result = awsService.releaseElasticIpAddress(allocationId, region);
		ElasticIp elasticIp = getElasticIp(allocationId);
		elasticIps.delete(elasticIp);
		log.info("Released elastic ip {}", allocationId);
		return CompletableFuture.completedFuture(result);
	}

	public void releaseElasticIpAddresses() {
		List<CompletableFuture<ReleaseAddressResult>> futureReleases = new LinkedList<>();

		for (Map.Entry<RegionEnum, List<Address>> addresses : getElasticIpAddresses().entrySet()) {
			RegionEnum region = addresses.getKey();
			List<CompletableFuture<ReleaseAddressResult>> regionFutureReleases = addresses.getValue().stream()
				.map(Address::getAllocationId)
				.map(allocationId -> releaseElasticIpAddress(allocationId, region))
				.collect(Collectors.toList());
			futureReleases.addAll(regionFutureReleases);
		}

		CompletableFuture.allOf(futureReleases.toArray(new CompletableFuture[0])).join();
	}

	public void reset() {
		elasticIps.deleteAll();
		log.info("Clearing all elastic ips");
	}

	public ElasticIp desassociate(CloudHost cloudHost) {
		RegionEnum region = cloudHost.getAwsRegion().getRegion();
		ElasticIp elasticIp = getElasticIp(region);
		elasticIp.setAssociationId(null);
		elasticIp.setInstanceId(null);
		return elasticIps.save(elasticIp);
	}
}
