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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.CpuStats;
import com.spotify.docker.client.messages.MemoryStats;
import com.spotify.docker.client.messages.NetworkStats;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

	private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
	private static final long READ_TIMEOUT = TimeUnit.SECONDS.toMillis(120);
	private static final int PORT = 2375;

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Usage: java container-monitor cpuCores hostname containerId");
		}
		final int cpuCores = Integer.parseInt(args[0]);
		final String hostname = args[1];
		final String containerId = args[2];
		System.out.println("Monitoring container " + containerId + " on hostname " + hostname + "...");

		final long startTime = System.currentTimeMillis();

		final FileWriter writer = getWriter(hostname, containerId);

		final byte[] auth = String.format("%s:%s", "username", "password").getBytes();
		final String dockerAuthorization = String.format("Basic %s", new String(Base64.getEncoder().encode(auth)));
		final String uri = String.format("http://%s:%d", hostname, PORT);
		final DockerClient dockerClient = DefaultDockerClient.builder().uri(uri)
			.header("Authorization", dockerAuthorization)
			.connectTimeoutMillis(CONNECTION_TIMEOUT).readTimeoutMillis(READ_TIMEOUT).build();
		new Timer("container-monitor", false).schedule(new TimerTask() {
			private long previousTime = System.currentTimeMillis();
			double previousRxBytes = Double.MAX_VALUE;
			double previousTxBytes = Double.MAX_VALUE;
			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				int interval = (int) (currentTime - previousTime);
				previousTime = currentTime;
				try {
					Map<String, Double> stats = getContainerStats(dockerClient, containerId, cpuCores, interval);
					double currentRxBytes = stats.get("rx-bytes");
					double rxBytesPerSec = Math.max(0, (currentRxBytes - previousRxBytes) / TimeUnit.MILLISECONDS.toSeconds(interval));
					stats.put("rx-bytes-per-sec", rxBytesPerSec);
					double currentTxBytes = stats.get("tx-bytes");
					double txBytesPerSec = Math.max(0, (currentTxBytes - previousTxBytes) / TimeUnit.MILLISECONDS.toSeconds(interval));
					stats.put("tx-bytes-per-sec", txBytesPerSec);
					String line = String.format(Locale.US, "%s,%.0f,%.3f,%.0f,%.0f,%.0f,%.0f", getTimestamp(startTime),
						stats.get("ram"), stats.get("cpu-%"), currentRxBytes, rxBytesPerSec, currentTxBytes, txBytesPerSec);
					previousRxBytes = currentRxBytes;
					previousTxBytes = currentTxBytes;
					writer.write( line + "\n");
					writer.flush();
				}
				catch (Exception e) {
					System.err.println(getTimestamp(startTime) + ": " + "Failed to get container stats: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}, 0, 1000);
	}

	private static FileWriter getWriter(String hostname, String containerId) throws IOException {
		Path source = Paths.get(Main.class.getResource("/").getPath());
		Path outFile = Paths.get(source.toAbsolutePath() + "/" + hostname + "_" + containerId + ".cvs");
		if (!Files.exists(outFile)) {
			Files.createFile(outFile);
		}
		FileWriter writer = new FileWriter(outFile.toFile());
		writer.write("timestamp,ram,cpu-%,rx-bytes,rx-bytes-per-sec,tx-bytes,tx-bytes-per-sec\n");
		writer.flush();
		return writer;
	}

	private static String getTimestamp(long startTime) {
		Date date = new Date(System.currentTimeMillis() - startTime);
		SimpleDateFormat formatter = new SimpleDateFormat("mm:ss.SSS");
		return formatter.format(date);
	}

	private static Map<String, Double> getContainerStats(DockerClient dockerClient, String containerId, int cpuCores, long interval)
		throws DockerException, InterruptedException {
		Map<String, Double> stats = new HashMap<>();
		ContainerStats containerStats = dockerClient.stats(containerId);

		CpuStats cpuStats = containerStats.cpuStats();
		CpuStats preCpuStats = containerStats.precpuStats();
		double cpu = cpuStats.cpuUsage().totalUsage().doubleValue();
		stats.put("cpu", cpu);
		double cpuPercent = getContainerCpuPercent(preCpuStats, cpuStats) / cpuCores;
		stats.put("cpu-%", cpuPercent);
		MemoryStats memoryStats = containerStats.memoryStats();
		double ram = memoryStats.usage().doubleValue();
		stats.put("ram", ram);
		double ramPercent = getContainerRamPercent(memoryStats);
		stats.put("ram-%", ramPercent);
		double rxBytes = 0;
		double txBytes = 0;
		for (NetworkStats networkStats : containerStats.networks().values()) {
			rxBytes += networkStats.rxBytes().doubleValue();
			txBytes += networkStats.txBytes().doubleValue();
		}
		stats.put("rx-bytes", rxBytes);
		stats.put("tx-bytes", txBytes);

		return stats;
	}

	private static double getContainerCpuPercent(CpuStats preCpuStats, CpuStats cpuStats) {
		double systemDelta = cpuStats.systemCpuUsage().doubleValue() - preCpuStats.systemCpuUsage().doubleValue();
		double cpuDelta = cpuStats.cpuUsage().totalUsage().doubleValue() - preCpuStats.cpuUsage().totalUsage().doubleValue();
		double cpuPercent = 0.0;
		if (systemDelta > 0.0 && cpuDelta > 0.0) {
			double onlineCpus = cpuStats.cpuUsage().percpuUsage().stream().filter(cpuUsage -> cpuUsage >= 1).count();
			cpuPercent = (cpuDelta / systemDelta) * onlineCpus * 100.0;
		}
		return cpuPercent;
	}

	private static double getContainerRamPercent(MemoryStats memStats) {
		return memStats.limit() < 1 ? 0.0 : (memStats.usage().doubleValue() / memStats.limit().doubleValue()) * 100.0;
	}
}
