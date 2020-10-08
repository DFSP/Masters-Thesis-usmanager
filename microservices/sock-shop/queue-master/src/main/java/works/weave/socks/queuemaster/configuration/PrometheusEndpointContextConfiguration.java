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

package works.weave.socks.queuemaster.configuration;

import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import works.weave.socks.queuemaster.controllers.PrometheusEndpoint;
import works.weave.socks.queuemaster.controllers.PrometheusMvcEndpoint;
import works.weave.socks.queuemaster.monitoring.PrometheusMetricWriter;

@ManagementContextConfiguration
public class PrometheusEndpointContextConfiguration {

	@Bean
	public PrometheusEndpoint prometheusEndpoint(CollectorRegistry registry) {
		return new PrometheusEndpoint(registry);
	}

	@Bean
	@ConditionalOnBean(PrometheusEndpoint.class)
	@ConditionalOnEnabledEndpoint("prometheus")
	PrometheusMvcEndpoint prometheusMvcEndpoint(PrometheusEndpoint prometheusEndpoint) {
		return new PrometheusMvcEndpoint(prometheusEndpoint);
	}

	@Bean
	CollectorRegistry collectorRegistry() {
		return new CollectorRegistry();
	}

	@Bean
	@ExportMetricWriter
	MetricWriter prometheusMetricWriter(CollectorRegistry registry) {
		return new PrometheusMetricWriter(registry);
	}

}
