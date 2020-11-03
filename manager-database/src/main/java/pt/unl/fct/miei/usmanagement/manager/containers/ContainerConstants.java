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

package pt.unl.fct.miei.usmanagement.manager.containers;

public final class ContainerConstants {

	private ContainerConstants() {
	}

	public static final class Environment {
		public static final String SERVICE_REGION = "SERVICE_REGION";
		public static final String BASIC_AUTH_USERNAME = "BASIC_AUTH_USERNAME";
		public static final String BASIC_AUTH_PASSWORD = "BASIC_AUTH_PASSWORD";
		public static final String PROXY_PASS = "PROXY_PASS";
		public static final String ID = "ID";
		public static final String MASTER = "MASTER";

		public static final class LoadBalancer {
			public static final String SERVER_NAME = "SERVER_NAME";
			public static final String SERVER = "SERVER";
		}

	}

	public static final class Label {
		public static final String US_MANAGER = "us-manager";
		public static final String SERVICE_NAME = "serviceName";
		public static final String SERVICE_TYPE = "serviceType";
		public static final String SERVICE_ADDRESS = "serviceAddress";
		public static final String SERVICE_PUBLIC_IP_ADDRESS = "servicePublicIpAddress";
		public static final String SERVICE_PRIVATE_IP_ADDRESS = "servicePrivateIpAddress";
		public static final String COORDINATES = "coordinates";
		public static final String REGION = "region";
		public static final String IS_REPLICABLE = "isReplicable";
		public static final String IS_STOPPABLE = "isStoppable";
	}

}
