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

package pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.amazonaws.services.ec2.model.ReleaseAddressResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.management.regions.RegionsService;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AwsService {

	private static final int BOOT_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(3);

	private final SshService sshService;
	private final Map<AwsRegion, AmazonEC2> ec2Clients;
	private final AWSStaticCredentialsProvider awsStaticCredentialsProvider;
	private final String awsInstanceSecurityGroup;
	private final String awsInstanceKeyPair;
	private final String awsInstanceType;
	private final String awsInstanceTag;
	private final int awsMaxRetries;
	private final int awsConnectionTimeout;
	private final String awsUsername;
	private final RegionsService regionService;

	@Getter
	private final Map<AwsRegion, Address> allocatedElasticIps;

	public AwsService(SshService sshService, AwsProperties awsProperties, RegionsService regionService) {
		this.sshService = sshService;
		this.regionService = regionService;
		this.ec2Clients = new HashMap<>(AwsRegion.getAvailableRegionsCount());
		String awsAccessKey = awsProperties.getAccess().getKey();
		String awsSecretAccessKey = awsProperties.getAccess().getSecretKey();
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretAccessKey);
		this.awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
		this.awsInstanceSecurityGroup = awsProperties.getInstance().getSecurityGroup();
		this.awsInstanceKeyPair = awsProperties.getInstance().getKeyPair();
		this.awsInstanceType = awsProperties.getInstance().getType();
		this.awsInstanceTag = awsProperties.getInstance().getTag();
		this.awsMaxRetries = awsProperties.getMaxRetries();
		this.awsConnectionTimeout = awsProperties.getConnectionTimeout();
		this.awsUsername = awsProperties.getAccess().getUsername();
		this.allocatedElasticIps = new HashMap<>();
	}

	private AmazonEC2 getEC2Client() {
		return getEC2Client(AwsRegion.getDefaultZone());
	}

	private AmazonEC2 getEC2Client(AwsRegion region) {
		AmazonEC2 amazonEC2 = ec2Clients.get(region);
		/*ClientConfiguration configuration = new ClientConfiguration()
			.withConnectionTimeout((int) TimeUnit.SECONDS.toMillis(5))
			.withRequestTimeout((int) TimeUnit.SECONDS.toMillis(5))
			.withClientExecutionTimeout((int) TimeUnit.SECONDS.toMillis(10));*/
		if (amazonEC2 == null) {
			amazonEC2 = AmazonEC2ClientBuilder
				.standard()
				.withRegion(region.getZone())
				.withCredentials(awsStaticCredentialsProvider)
				/*.withClientConfiguration(configuration)*/
				.build();
			ec2Clients.put(region, amazonEC2);
		}
		return amazonEC2;
	}

	public List<Instance> getInstances() {
		List<CompletableFuture<List<Instance>>> futureRegionsInstances = AwsRegion.getAwsRegions().stream()
			.map(this::getInstances).collect(Collectors.toList());

		CompletableFuture.allOf(futureRegionsInstances.toArray(new CompletableFuture[0])).join();

		List<Instance> instances = new LinkedList<>();
		futureRegionsInstances.forEach(futureInstances -> {
			try {
				instances.addAll(futureInstances.get());
			}
			catch (InterruptedException | ExecutionException e) {
				log.error("Unable to get instances from every region");
			}
		});

		return instances;
	}

	@Async
	public CompletableFuture<List<Instance>> getInstances(AwsRegion region) {
		List<Instance> instances = new ArrayList<>();
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		DescribeInstancesResult result;
		try {
			do {
				result = getEC2Client(region).describeInstances(request);
				result.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream)
					.filter(this::isUsManagerInstance).forEach(instances::add);
				request.setNextToken(result.getNextToken());
			} while (result.getNextToken() != null);
		}
		catch (SdkClientException e) {
			log.error("Unable to get instances from region {}: {}", region.getName(), e.getMessage());
		}
		return CompletableFuture.completedFuture(instances);
	}

	public Instance getInstance(String id, AwsRegion region) {
		for (int i = 0; i < awsMaxRetries; i++) {
			try {
				List<Instance> instances = getInstances(region).get();
				return instances.stream().filter(instance -> instance.getInstanceId().equals(id))
					.findFirst().orElseThrow(() -> new EntityNotFoundException(Instance.class, "id", id, "region", region.getName()));
			}
			catch (Exception e) {
				log.error("Unable to get instance {} on region {}: {}. Retrying... ({}/{})", id, region, e.getMessage(), i, awsMaxRetries);
				Timing.sleep(i + 1, TimeUnit.SECONDS);
			}
		}
		throw new ManagerException("Unable to get instance %s on region %s", id, region.getRegion());
	}

	public List<AwsSimpleInstance> getSimpleInstances() {
		return getInstances().stream().map(AwsSimpleInstance::new).collect(Collectors.toList());
	}

	public Instance createInstance(AwsRegion region) {
		log.info("Launching new instance at region {} {}", region.getZone(), region.getName());
		String instanceId;
		try {
			instanceId = launchInstance(region).get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new ManagerException("Unable to launch instance on region: %s", region.getName(), e.getMessage());
		}
		Instance instance = waitInstanceState(instanceId, AwsInstanceState.RUNNING, region);
		String publicIpAddress = instance.getPublicIpAddress();
		log.info("New aws instance created: instanceId = {}, publicIpAddress = {}", instanceId, publicIpAddress);
		try {
			waitToBoot(instance);
		}
		catch (TimeoutException e) {
			throw new ManagerException("Timeout while waiting for instance %s to boot: ", instance, e.getMessage());
		}
		return instance;
	}

	@Async
	public CompletableFuture<String> launchInstance(AwsRegion region) {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
			.withImageId(region.getAmi())
			.withInstanceType(awsInstanceType)
			.withMinCount(1)
			.withMaxCount(1)
			.withSecurityGroups(awsInstanceSecurityGroup)
			.withKeyName(awsInstanceKeyPair);
		AmazonEC2 amazonEC2 = getEC2Client(region);
		RunInstancesResult result = amazonEC2.runInstances(runInstancesRequest);
		Instance instance = result.getReservation().getInstances().get(0);
		String instanceId = instance.getInstanceId();
		String instanceName = String.format("ubuntu-%d", System.currentTimeMillis());
		CreateTagsRequest createTagsRequest = new CreateTagsRequest().withResources(instanceId)
			.withTags(new Tag("Name", instanceName), new Tag(awsInstanceTag, "true"));
		amazonEC2.createTags(createTagsRequest);
		return CompletableFuture.completedFuture(instanceId);
	}

	public Instance startInstance(String instanceId, AwsRegion region, boolean wait) {
		log.info("Starting instance {}", instanceId);
		Instance instance = setInstanceState(instanceId, AwsInstanceState.RUNNING, region, wait);
		try {
			waitToBoot(instance);
		}
		catch (TimeoutException e) {
			log.error("Unable wait for instance {} to boot: {}", instanceId, e.getMessage());
		}
		return instance;
	}

	private void startInstanceById(String instanceId, AwsRegion region) {
		DryRunSupportedRequest<StartInstancesRequest> dryRequest = () ->
			new StartInstancesRequest().withInstanceIds(instanceId).getDryRunRequest();
		AmazonEC2 amazonEC2 = getEC2Client(region);
		DryRunResult<StartInstancesRequest> dryResponse = amazonEC2.dryRun(dryRequest);
		if (!dryResponse.isSuccessful()) {
			throw new ManagerException(dryResponse.getDryRunResponse().getErrorMessage());
		}
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instanceId);
		amazonEC2.startInstances(request);
	}

	public Instance stopInstance(String instanceId, AwsRegion region, boolean wait) {
		log.info("Stopping instance {}", instanceId);
		return setInstanceState(instanceId, AwsInstanceState.STOPPED, region, wait);
	}

	private void stopInstanceById(String instanceId, AwsRegion region) {
		DryRunSupportedRequest<StopInstancesRequest> dryRequest = () ->
			new StopInstancesRequest().withInstanceIds(instanceId).getDryRunRequest();
		AmazonEC2 amazonEC2 = getEC2Client(region);
		DryRunResult<StopInstancesRequest> dryResponse = amazonEC2.dryRun(dryRequest);
		if (!dryResponse.isSuccessful()) {
			throw new ManagerException(dryResponse.getDryRunResponse().getErrorMessage());
		}
		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instanceId);
		amazonEC2.stopInstances(request);
	}

	public void terminateInstance(String instanceId, AwsRegion region, boolean wait) {
		log.info("Terminating instance {}", instanceId);
		setInstanceState(instanceId, AwsInstanceState.TERMINATED, region, wait);
	}

	private void terminateInstanceById(String instanceId, AwsRegion region) {
		DryRunSupportedRequest<TerminateInstancesRequest> dryRequest = () ->
			new TerminateInstancesRequest().withInstanceIds(instanceId).getDryRunRequest();
		AmazonEC2 amazonEC2 = getEC2Client(region);
		DryRunResult<TerminateInstancesRequest> dryResponse = amazonEC2.dryRun(dryRequest);
		if (!dryResponse.isSuccessful()) {
			throw new ManagerException(dryResponse.getDryRunResponse().getErrorMessage());
		}
		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceId);
		amazonEC2.terminateInstances(request);
	}

	private Instance setInstanceState(String id, AwsInstanceState state, AwsRegion region, boolean wait) {
		Instance instance = getInstance(id, region);
		int instanceState = instance.getState().getCode();
		if (instanceState == state.getCode()) {
			log.info("Instance {} is already on state {}", id, state.getState());
			return instance;
		}
		try {
			switch (state) {
				case RUNNING:
					startInstanceById(id, region);
					break;
				case STOPPED:
					stopInstanceById(id, region);
					break;
				case TERMINATED:
					terminateInstanceById(id, region);
					break;
				default:
					throw new UnsupportedOperationException();
			}
			if (wait) {
				instance = waitInstanceState(id, state, region);
			}
			log.info("Setting instance {} to {} state", id, state.getState());
			return instance;
		}
		catch (ManagerException e) {
			log.error("Failed to set instance {} to {} state: {}", id, state.getState(), e.getMessage());
		}

		throw new ManagerException("Unable to set instance state %d within %d tries",
			state.getState(), awsMaxRetries);
	}

	private Instance waitInstanceState(String instanceId, AwsInstanceState state, AwsRegion region) {
		Instance[] instance = new Instance[1];
		try {
			Timing.wait(() -> {
				instance[0] = getInstance(instanceId, region);
				return instance[0].getState().getCode() == state.getCode();
			}, awsConnectionTimeout);
		}
		catch (TimeoutException e) {
			throw new ManagerException("Unknown status of instance %s %s operation: Timed out", instanceId, state.getState());
		}
		return instance[0];
	}

	public void waitToBoot(Instance instance) throws TimeoutException {
		HostAddress hostAddress = new HostAddress(awsUsername, instance.getPublicIpAddress(), instance.getPrivateIpAddress());
		try {
			SSHClient client = sshService.waitAvailability(hostAddress, BOOT_TIMEOUT);
			sshService.executeCommand("whoami", hostAddress, client, TimeUnit.SECONDS.toMillis(30));
		}
		catch (IOException e) {
			throw new TimeoutException("Timeout while waiting for new instance to boot");
		}
	}

	private boolean isUsManagerInstance(Instance instance) {
		return instance.getTags().stream().anyMatch(tag ->
			Objects.equals(tag.getKey(), awsInstanceTag) && Objects.equals(tag.getValue(), "true"));
	}

	@Async
	public CompletableFuture<String> allocateElasticIpAddress(AwsRegion region) {
		final AmazonEC2 ec2 = getEC2Client(region);

		AllocateAddressRequest allocateRequest = new AllocateAddressRequest().withDomain(DomainType.Vpc);
		AllocateAddressResult allocateResponse = ec2.allocateAddress(allocateRequest);
		String allocationId = allocateResponse.getAllocationId();

		return CompletableFuture.completedFuture(allocationId);
	}

	public void allocateElasticIpAddresses() {
		Map<AwsRegion, List<Address>> addresses = getElasticIpAddresses();

		RegionEnum[] regions = RegionEnum.values();

		Map<AwsRegion, CompletableFuture<String>> futureElasticIpAddresses = new HashMap<>(regions.length);
		for (RegionEnum region : regions) {
			AwsRegion awsRegion = regionService.getAwsRegion(region);
			List<Address> regionAddresses = addresses.get(awsRegion);
			if (regionAddresses != null && regionAddresses.size() > 0 && regionAddresses.get(0).getAssociationId() == null) {
				Address address = regionAddresses.get(0);
				futureElasticIpAddresses.put(awsRegion, CompletableFuture.completedFuture(address.getAllocationId()));
			}
			else {
				futureElasticIpAddresses.put(awsRegion, allocateElasticIpAddress(awsRegion));
			}
		}

		CompletableFuture.allOf(futureElasticIpAddresses.values().toArray(new CompletableFuture[0])).join();

		for (Map.Entry<AwsRegion, CompletableFuture<String>> futureElasticIpAddress : futureElasticIpAddresses.entrySet()) {
			AwsRegion awsRegion = futureElasticIpAddress.getKey();
			try {
				futureElasticIpAddress.getValue().get();
			}
			catch (InterruptedException | ExecutionException e) {
				throw new ManagerException("Failed to allocate elastic ip address for region %s: %s", awsRegion, e.getMessage());
			}
		}

		Map<AwsRegion, Address> elasticIpAddresses = getElasticIpAddresses().entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
		allocatedElasticIps.putAll(elasticIpAddresses);
	}

	@Async
	public CompletableFuture<AssociateAddressResult> associateElasticIpAddress(String allocationId, String instanceId) {
		final AmazonEC2 ec2 = getEC2Client();

		AssociateAddressRequest associateRequest = new AssociateAddressRequest()
			.withInstanceId(instanceId)
			.withAllocationId(allocationId);

		AssociateAddressResult associateResult = ec2.associateAddress(associateRequest);

		return CompletableFuture.completedFuture(associateResult);
	}

	@Async
	public CompletableFuture<List<Address>> getElasticIpAddresses(AwsRegion region) {
		final AmazonEC2 ec2 = getEC2Client(region);
		DescribeAddressesResult result = ec2.describeAddresses();
		return CompletableFuture.completedFuture(result.getAddresses());
	}

	public Map<AwsRegion, List<Address>> getElasticIpAddresses() {
		Map<AwsRegion, CompletableFuture<List<Address>>> futureElasticIpAddresses = new HashMap<>();
		for (RegionEnum region : RegionEnum.values()) {
			AwsRegion awsRegion = regionService.getAwsRegion(region);
			futureElasticIpAddresses.put(awsRegion, getElasticIpAddresses(awsRegion));
		}

		CompletableFuture.allOf(futureElasticIpAddresses.values().toArray(new CompletableFuture[0])).join();

		Map<AwsRegion, List<Address>> elasticIpAddresses = new HashMap<>(futureElasticIpAddresses.size());
		for (Map.Entry<AwsRegion, CompletableFuture<List<Address>>> futureElasticIpAddress : futureElasticIpAddresses.entrySet()) {
			AwsRegion region = futureElasticIpAddress.getKey();
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
	public CompletableFuture<ReleaseAddressResult> releaseElasticIpAddress(String allocationId, AwsRegion region) {
		final AmazonEC2 ec2 = getEC2Client(region);
		ReleaseAddressRequest request = new ReleaseAddressRequest().withAllocationId(allocationId);
		ReleaseAddressResult result = ec2.releaseAddress(request);
		log.info("Released elastic ip {}", allocationId);
		return CompletableFuture.completedFuture(result);
	}

	public void releaseElasticIpAddresses() {
		List<CompletableFuture<ReleaseAddressResult>> futureReleases = new LinkedList<>();

		for (Map.Entry<AwsRegion, List<Address>> addresses : getElasticIpAddresses().entrySet()) {
			AwsRegion region = addresses.getKey();
			List<CompletableFuture<ReleaseAddressResult>> regionFutureReleases = addresses.getValue().stream()
				.map(Address::getAllocationId)
				.map(allocationId -> releaseElasticIpAddress(allocationId, region))
				.collect(Collectors.toList());
			futureReleases.addAll(regionFutureReleases);
		}

		CompletableFuture.allOf(futureReleases.toArray(new CompletableFuture[0])).join();
	}
}
