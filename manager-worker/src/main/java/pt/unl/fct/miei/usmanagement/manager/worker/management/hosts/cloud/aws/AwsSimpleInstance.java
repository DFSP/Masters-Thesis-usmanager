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

package pt.unl.fct.miei.usmanagement.manager.worker.management.hosts.cloud.aws;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Placement;
import lombok.Data;

@Data
public final class AwsSimpleInstance {

	private final String instanceId;
	private final String imageId;
	private final String instanceType;
	private final InstanceState state;
	private final String publicDnsName;
	private final String publicIpAddress;
	private final String privateIpAddress;
	private final Placement placement;

	public AwsSimpleInstance(Instance instance) {
		this.instanceId = instance.getInstanceId();
		this.imageId = instance.getImageId();
		this.instanceType = instance.getInstanceType();
		this.state = instance.getState();
		this.publicDnsName = instance.getPublicDnsName();
		this.publicIpAddress = instance.getPublicIpAddress();
		this.privateIpAddress = instance.getPrivateIpAddress();
		this.placement = instance.getPlacement();
	}

}