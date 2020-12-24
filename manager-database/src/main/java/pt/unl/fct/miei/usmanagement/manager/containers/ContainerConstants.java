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

		public static final class LoadBalancer {
			public static final String SERVER = "SERVER";
		}

		public static final class Manager {
			public static final String ID = "ID";
			public static final String HOST_ADDRESS = "HOST_ADDRESS";
			public static final String KAFKA_BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";
			public static final String CONTAINER = "CONTAINER";
		}

		public static final class Kafka {
			public static final String KAFKA_BROKER_ID = "KAFKA_BROKER_ID";
			public static final String KAFKA_LISTENERS = "KAFKA_LISTENERS";
			public static final String KAFKA_ADVERTISED_LISTENERS = "KAFKA_ADVERTISED_LISTENERS";
			public static final String KAFKA_ZOOKEEPER_CONNECT = "KAFKA_ZOOKEEPER_CONNECT";
			public static final String KAFKA_CREATE_TOPICS = "KAFKA_CREATE_TOPICS";
		}
	}

	public static final class Label {
		public static final String US_MANAGER = "usManager";
		public static final String CONTAINER_TYPE = "containerType";
		public static final String SERVICE_NAME = "serviceName";
		public static final String SERVICE_TYPE = "serviceType";
		//public static final String SERVICE_ADDRESS = "serviceAddress";
		//public static final String SERVICE_PUBLIC_IP_ADDRESS = "servicePublicIpAddress";
		//public static final String SERVICE_PRIVATE_IP_ADDRESS = "servicePrivateIpAddress";
		public static final String SERVICE_PORT = "servicePort";
		public static final String COORDINATES = "coordinates";
		public static final String REGION = "region";
		public static final String RECOVERY = "recovery";
		public static final String MASTER_MANAGER = "masterManager";
		public static final String KAFKA_BROKER_ID = "kafkaBrokerId";
		public static final String MANAGER_ID = "managerId";
	}

}
