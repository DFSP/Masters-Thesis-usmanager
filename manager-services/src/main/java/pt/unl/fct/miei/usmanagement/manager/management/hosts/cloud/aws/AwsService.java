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
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.exceptions.ManagerException;
import pt.unl.fct.miei.usmanagement.manager.hosts.HostAddress;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.AwsRegion;
import pt.unl.fct.miei.usmanagement.manager.management.remote.ssh.SshService;
import pt.unl.fct.miei.usmanagement.manager.util.Timing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AwsService {

	private final SshService sshService;
	private final Map<AwsRegion, AmazonEC2> ec2Clients;
	private final AWSStaticCredentialsProvider awsStaticCredentialsProvider;
	private final String awsInstanceSecurityGroup;
	private final String awsInstanceKeyPair;
	private final String awsInstanceType;
	private final String awsInstanceTag;
	private final int awsMaxRetries;
	private final int awsDelayBetweenRetries;
	private final int awsConnectionTimeout;
	private final String awsUsername;

	public AwsService(SshService sshService, AwsProperties awsProperties) {
		this.sshService = sshService;
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
		this.awsDelayBetweenRetries = awsProperties.getDelayBetweenRetries();
		this.awsConnectionTimeout = awsProperties.getConnectionTimeout();
		this.awsUsername = awsProperties.getAccess().getUsername();
	}

	private AmazonEC2 getEC2Client(AwsRegion region) {
		AmazonEC2 amazonEC2 = ec2Clients.get(region);
		if (amazonEC2 == null) {
			amazonEC2 = AmazonEC2ClientBuilder
				.standard()
				.withRegion(region.getZone())
				.withCredentials(awsStaticCredentialsProvider)
				.build();
			ec2Clients.put(region, amazonEC2);
		}
		return amazonEC2;
	}

	public List<Instance> getInstances() {
		List<Instance> instances = new ArrayList<>();
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		AwsRegion.getAwsRegions().parallelStream().forEach(region -> {
			try {
				DescribeInstancesResult result;
				do {
					result = getEC2Client(region).describeInstances(request);
					result.getReservations().stream().map(Reservation::getInstances).flatMap(List::stream)
						.filter(this::isUsManagerInstance).forEach(instances::add);
					request.setNextToken(result.getNextToken());
				} while (result.getNextToken() != null);
			} catch (SdkClientException e) {
				log.error(e.getMessage());
			}
		});
		return instances;
	}

	public Instance getInstance(String id, AwsRegion region) {
		DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(id);
		DescribeInstancesResult result;
		do {
			result = getEC2Client(region).describeInstances(request);
			Optional<Instance> instance = result.getReservations().stream().map(Reservation::getInstances)
				.flatMap(List::stream).filter(this::isUsManagerInstance).findFirst();
			if (instance.isPresent()) {
				return instance.get();
			}
			request.setNextToken(result.getNextToken());
		} while (result.getNextToken() != null);

		throw new ManagerException("Instance with id %s not found", id);
	}

	public List<AwsSimpleInstance> getSimpleInstances() {
		return getInstances().stream().map(AwsSimpleInstance::new).collect(Collectors.toList());
	}

	public Instance createInstance(AwsRegion region) {
		log.info("Launching new instance at region {} {}", region.getZone(), region.getName());
		String instanceId = createEC2Instance(region);
		Instance instance = waitInstanceState(instanceId, AwsInstanceState.RUNNING, region);
		String publicIpAddress = instance.getPublicIpAddress();
		log.info("New aws instance created: instanceId = {}, publicIpAddress = {}", instanceId, publicIpAddress);
		try {
			waitToBoot(instance);
		}
		catch (TimeoutException e) {
			e.printStackTrace();
		}
		return instance;
	}

	private String createEC2Instance(AwsRegion region) {
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
		return instanceId;
	}

	public Instance startInstance(String instanceId, AwsRegion region, boolean wait) {
		log.info("Starting instance {}", instanceId);
		Instance instance = setInstanceState(instanceId, AwsInstanceState.RUNNING, region, wait);
		try {
			waitToBoot(instance);
		}
		catch (TimeoutException e) {
			e.printStackTrace();
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

	public Instance terminateInstance(String instanceId, AwsRegion region, boolean wait) {
		log.info("Terminating instance {}", instanceId);
		return setInstanceState(instanceId, AwsInstanceState.TERMINATED, region, wait);
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
		for (int tries = 0; tries < awsMaxRetries; tries++) {
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
				log.info("Failed to set instance {} to {} state: {}", id, state.getState(), e.getMessage());
			}
			Timing.sleep(awsDelayBetweenRetries, TimeUnit.MILLISECONDS);
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
			log.info("Unknown status of instance {} {} operation: Timed out", instanceId, state.getState());
			throw new ManagerException(e.getMessage());
		}
		return instance[0];
	}

	private void waitToBoot(Instance instance) throws TimeoutException {
		HostAddress hostAddress = new HostAddress(awsUsername, instance.getPublicIpAddress(), instance.getPrivateIpAddress());
		log.info("Waiting for instance {} to boot", hostAddress.getPublicIpAddress());
		Timing.wait(() -> sshService.hasConnection(hostAddress), 1000, awsConnectionTimeout);
	}

	private boolean isUsManagerInstance(Instance instance) {
		return instance.getTags().stream().anyMatch(tag ->
			Objects.equals(tag.getKey(), awsInstanceTag) && Objects.equals(tag.getValue(), "true"));
	}

}
