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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("aws")
public class AwsProperties {

	private final Access access;
	private final Instance instance;
	private int initialMaxInstances;
	private int maxRetries;
	private int delayBetweenRetries;
	private int connectionTimeout;

	public AwsProperties() {
		this.access = new Access();
		this.instance = new Instance();
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	@Setter
	public static final class Access {

		private String keyFilePath;
		private String username;
		private String key;
		private String secretKey;

	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	@Setter
	public static final class Instance {

		private String ami;
		private String securityGroup;
		private String keyPair;
		private String type;
		private String tag;

	}

}
