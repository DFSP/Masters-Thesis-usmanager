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

package pt.unl.fct.miei.usmanagement.manager.monitoring.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
@Getter
@ToString
public enum PrometheusQuery {

	AVAILABLE_MEMORY("node_memory_MemAvailable_bytes"),
	TOTAL_MEMORY("node_memory_MemTotal_bytes"),
	MEMORY_USAGE("node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes"),
	AVAILABLE_MEMORY_RATE("node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes"),
	MEMORY_USAGE_PERCENTAGE("100 * (1 - ((avg_over_time(node_memory_MemFree_bytes[5m]) + avg_over_time(node_memory_Cached_bytes[5m]) + avg_over_time(node_memory_Buffers_bytes[5m])) / avg_over_time(node_memory_MemTotal_bytes[5m])))"),
	CPU_USAGE_PERCENTAGE("100-(avg by (instance) (irate(node_cpu_seconds_total{job=\"node_exporter\",mode=\"idle\"}[5m]))*100)"),
	FILESYSTEM_AVAILABLE_SPACE("node_filesystem_avail_bytes{mountpoint='/'}"),
	NETWORK_BYTES_RECEIVED("rate(node_network_receive_bytes_total[5m])");

	private final String query;

	@JsonCreator
	public static PrometheusQuery forValues(@JsonProperty("query") String query) {
		for (PrometheusQuery prometheusQuery : PrometheusQuery.values()) {
			if (prometheusQuery.query.equalsIgnoreCase(query)) {
				return prometheusQuery;
			}
		}
		return null;
	}

}

