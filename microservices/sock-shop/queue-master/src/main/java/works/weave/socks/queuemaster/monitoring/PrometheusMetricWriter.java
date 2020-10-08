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

package works.weave.socks.queuemaster.monitoring;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PrometheusMetricWriter implements MetricWriter {

	private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Gauge> gauges = new ConcurrentHashMap<>();
	private CollectorRegistry registry;

	@Autowired
	public PrometheusMetricWriter(CollectorRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void increment(Delta<?> delta) {
		counter(delta.getName()).inc(delta.getValue().doubleValue());
	}

	@Override
	public void reset(String metricName) {
		counter(metricName).clear();
	}

	@Override
	public void set(Metric<?> value) {
		gauge(value.getName()).set(value.getValue().doubleValue());
	}

	private Counter counter(String name) {
		String key = sanitizeName(name);
		return counters.computeIfAbsent(key, k -> Counter.build().name(k).help(k).register(registry));
	}

	private Gauge gauge(String name) {
		String key = sanitizeName(name);
		return gauges.computeIfAbsent(key, k -> Gauge.build().name(k).help(k).register(registry));
	}

	private String sanitizeName(String name) {
		return name.replaceAll("[^a-zA-Z0-9_]", "_");
	}

}
