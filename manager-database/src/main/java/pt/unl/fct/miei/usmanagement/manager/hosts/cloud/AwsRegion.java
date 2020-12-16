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

package pt.unl.fct.miei.usmanagement.manager.hosts.cloud;

import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Placement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
@Getter
@ToString
public enum AwsRegion {

	GOV_CLOUD_WEST_1("us-gov-west-1", "AWS GovCloud (US-West)", RegionEnum.NORTH_AMERICA, null,
		new Coordinates("AWS GovCloud (US-West)", 46.412832, -117.046891), false),
	GOV_CLOUD_EAST_1("us-gov-east-1", "AWS GovCloud (US-East)", RegionEnum.NORTH_AMERICA, null,
		new Coordinates("AWS GovCloud (US-East)", 40.058766, -83.175497), false),
	US_EAST_1("us-east-1", "US East (N. Virginia)", RegionEnum.NORTH_AMERICA,
		Map.of(InstanceType.T2Micro, "ami-0c674068fe23667c7", InstanceType.T2Medium, "ami-085d22a7b1463c487"),
		new Coordinates("US East (N. Virginia)", 38.946728, -77.443386)),
	US_EAST_2("us-east-2", "US East (Ohio)", RegionEnum.NORTH_AMERICA,
		Map.of(InstanceType.T2Micro, "ami-065559657711caa53", InstanceType.T2Medium, "ami-040738d74158686a0"),
		new Coordinates("US East (Ohio)", 39.958587, -82.997058)),
	US_WEST_1("us-west-1", "US West (N. California)", RegionEnum.NORTH_AMERICA,
		Map.of(InstanceType.T2Micro, "ami-0df861b1e282905a0", InstanceType.T2Medium, "ami-0135ee11046bcdad1"),
		new Coordinates("US West (N. California)", 37.758891, -122.443318)),
	US_WEST_2("us-west-2", "US West (Oregon)", RegionEnum.NORTH_AMERICA,
		Map.of(InstanceType.T2Micro, "ami-04934a32a6f126606", InstanceType.T2Medium, "ami-0ae9cfc1fb8a7e689"),
		new Coordinates("US West (Oregon)", 45.841904, -119.296774)),
	AF_SOUTH_1("af-south-1", "Africa (Cape Town)", RegionEnum.AFRICA,
		Map.of(InstanceType.T2Micro, "ami-0d3f351838a464138", InstanceType.T2Medium, "ami-06863f37a31e409b2"),
		new Coordinates("Africa (Cape Town)", -33.953923, 18.566379)),
	AP_EAST_1("ap-east-1", "Asia Pacific (Hong Kong)", RegionEnum.ASIA,
		Map.of(InstanceType.T2Micro, "ami-05a10535c9e6811bd", InstanceType.T2Medium, "ami-01ef7bc73f08ca6a9"),
		new Coordinates("Asia Pacific (Hong Kong)", 22.321326, 114.172109), false),
	AP_SOUTH_1("ap-south-1", "Asia Pacific (Mumbai)", RegionEnum.ASIA,
		Map.of(InstanceType.T2Micro, "ami-060b4856be6b88671", InstanceType.T2Medium, "ami-03d31b5b7f0830bdb"),
		new Coordinates("Asia Pacific (Mumbai)", 19.085863, 72.873766)),
	AP_NORTHEAST_2("ap-northeast-2", "Asia Pacific (Seoul)", RegionEnum.ASIA,
		Map.of(InstanceType.T2Micro, "ami-051233a90353e8845", InstanceType.T2Medium, "ami-0c4e410aa18c98ba1"),
		new Coordinates("Asia Pacific (Seoul)", 37.562049, 127.007511)),
	AP_SOUTHEAST_1("ap-southeast-1", "Asia Pacific (Singapore)", RegionEnum.ASIA,
		Map.of(InstanceType.T2Micro, "ami-0d5e118c0540b5489", InstanceType.T2Medium, "ami-0281216283d7401e3"),
		new Coordinates("Asia Pacific (Singapore)", 1.353010, 103.869377)),
	AP_SOUTHEAST_2("ap-southeast-2", "Asia Pacific (Sydney)", RegionEnum.OCEANIA,
		Map.of(InstanceType.T2Micro, "ami-0efb60e5cd1244681", InstanceType.T2Medium, "ami-0dc2f2303888f5b90"),
		new Coordinates("Asia Pacific (Sydney)", -33.831767, 151.007401)),
	AP_NORTHEAST_3("ap-northeast-3", "Asia Pacific (Osaka-local)", RegionEnum.ASIA, null,
		new Coordinates("Asia Pacific (Osaka)", 34.675407, 135.496091), false),
	AP_NORTHEAST_1("ap-northeast-1", "Asia Pacific (Tokyo)", RegionEnum.ASIA,
		Map.of(InstanceType.T2Micro, "ami-0f08d926ba9733990", InstanceType.T2Medium, "ami-054bb54b0f4472dca"),
		new Coordinates("Asia Pacific (Tokyo)", 35.688572, 139.618912)),
	CA_CENTRAL_1("ca-central-1", "Canada (Central)", RegionEnum.NORTH_AMERICA,
		Map.of(InstanceType.T2Micro, "ami-010263fc91bee3579", InstanceType.T2Medium, "ami-021b72b60bbce6af8"),
		new Coordinates("Canada (Montreal)", 45.508968, -73.616289)),
	EU_CENTRAL_1("eu-central-1", "Europe (Frankfurt)", RegionEnum.EUROPE,
		Map.of(InstanceType.T2Micro, "ami-05f8dc43e4ca2d1fd", InstanceType.T2Medium, "ami-094081c87e471ed95"),
		new Coordinates("Europe (Frankfurt)", 50.110991, 8.632203)),
	EU_WEST_1("eu-west-1", "Europe (Ireland)", RegionEnum.EUROPE,
		Map.of(InstanceType.T2Micro, "ami-050ed5697473a92aa", InstanceType.T2Medium, "ami-02b55688d323e6bbf"),
		new Coordinates("Europe (Ireland)", 53.346174, -6.272156)),
	EU_WEST_2("eu-west-2", "Europe (London)", RegionEnum.EUROPE,
		Map.of(InstanceType.T2Micro, "ami-0edd5eaf7f6e43f1a", InstanceType.T2Medium, "ami-06a74b5f47aaaa1f0"),
		new Coordinates("Europe (London)", 51.516689, -0.134100)),
	EU_SOUTH_1("eu-south-1", "Europe (Milan)", RegionEnum.EUROPE,
		Map.of(InstanceType.T2Micro, "ami-03b570cd52f568c7c", InstanceType.T2Medium, "ami-02dd61a78476fcf79"),
		new Coordinates("Europe (Milan)", 45.469902, 9.179905)),
	EU_WEST_3("eu-west-3", "Europe (Paris)", RegionEnum.EUROPE,
		Map.of(InstanceType.T2Micro, "ami-070848040dcf61cb6", InstanceType.T2Medium, "ami-022bb0ec308b249f3"),
		new Coordinates("Europe (Paris)", 48.879382, 2.341615)),
	EU_NORTH_1("eu-north-1", "Europe (Stockholm)", RegionEnum.EUROPE,
		Map.of(InstanceType.T2Micro, "ami-033eae7360852e3c3", InstanceType.T2Medium, "ami-086a62f9f1689c6da"),
		new Coordinates("Europe (Stockholm)", 59.329544, 18.066653)),
	ME_SOUTH_1("me-south-1", "Middle East (Bahrain)", RegionEnum.MIDDLE_EAST,
		Map.of(InstanceType.T2Micro, "ami-0a227f8b7f1926094", InstanceType.T2Medium, "ami-0b3fee1c8a0e2d249"),
		new Coordinates("Middle East (Bahrain)", 26.233356, 50.585524)),
	SA_EAST_1("sa-east-1", "South America (São Paulo)", RegionEnum.SOUTH_AMERICA,
		Map.of(InstanceType.T2Micro, "ami-0351ba0be52c674ef", InstanceType.T2Medium, "ami-0df402ffcf63464b3"),
		new Coordinates("South America (São Paulo)", -23.576129, -46.614103));

	private final String zone;
	private final String name;
	private final RegionEnum region;
	@JsonIgnore
	private final Map<InstanceType, String> ami;
	private final Coordinates coordinates;
	private final boolean available;

	public static final AwsRegion DEFAULT_ZONE = US_WEST_2;

	@Getter
	private static final List<AwsRegion> awsRegions;

	static {
		awsRegions = new LinkedList<>();
		for (AwsRegion region : values()) {
			if (region.isAvailable()) {
				awsRegions.add(region);
			}
		}
	}

	AwsRegion(String zone, String name, RegionEnum region, Map<InstanceType, String> ami, Coordinates coordinates) {
		this.zone = zone;
		this.name = name;
		this.region = region;
		this.ami = ami;
		this.coordinates = coordinates;
		this.available = true;
	}

	public static int getAvailableRegionsCount() {
		return awsRegions.size();
	}

	@JsonCreator
	public static AwsRegion forValues(@JsonProperty("zone") String zone, @JsonProperty("name") String name,
									  @JsonProperty("region") RegionEnum region, @JsonProperty("ami") Map<InstanceType, String> ami,
									  @JsonProperty("coordinates") Coordinates coordinates) {
		for (AwsRegion awsRegion : AwsRegion.values()) {
			if (awsRegion.zone.equalsIgnoreCase(zone) && awsRegion.name.equalsIgnoreCase(name)
				&& awsRegion.region == region && awsRegion.ami.equals(ami)
				&& Objects.equals(awsRegion.coordinates, coordinates)) {
				return awsRegion;
			}
		}
		return null;
	}


	public static AwsRegion getDefaultZone() {
		return EU_WEST_3;
	}

	public static AwsRegion fromPlacement(Placement placement) {
		String availabilityZone = placement.getAvailabilityZone();
		while (!Character.isDigit(availabilityZone.charAt(availabilityZone.length() - 1))) {
			availabilityZone = availabilityZone.substring(0, availabilityZone.length() - 1);
		}
		return AwsRegion.valueOf(availabilityZone.toUpperCase().replace("-", "_"));
	}
}
