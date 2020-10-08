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

package works.weave.socks.orders.config;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@ConditionalOnClass(SpringBootMetricsCollector.class)
class PrometheusAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean(SpringBootMetricsCollector.class)
	SpringBootMetricsCollector springBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
		SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector
			(publicMetrics);
		springBootMetricsCollector.register();
		return springBootMetricsCollector;
	}

	@Bean
	@ConditionalOnMissingBean(name = "prometheusMetricsServletRegistrationBean")
	ServletRegistrationBean prometheusMetricsServletRegistrationBean(@Value("${prometheus.metrics" +
		".path:/metrics}") String metricsPath) {
		DefaultExports.initialize();
		return new ServletRegistrationBean(new MetricsServlet(), metricsPath);
	}
}
