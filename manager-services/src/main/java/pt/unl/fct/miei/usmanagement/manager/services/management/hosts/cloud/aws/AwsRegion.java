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

package pt.unl.fct.miei.usmanagement.manager.services.management.hosts.cloud.aws;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.Coordinates;

@AllArgsConstructor
@Getter
@ToString
public enum AwsRegion {

	GOV_CLOUD_WEST_1("us-gov-west-1", "AWS GovCloud (US-West)",  new Coordinates("AWS GovCloud (US-West)", 46.412832, -117.046891)),
	GOV_CLOUD_EAST_1("us-gov-east-1", "AWS GovCloud (US-East)", new Coordinates("AWS GovCloud (US-East)", 40.058766, -83.175497)),
	US_EAST_1("us-east-1", "US East (N. Virginia)", new Coordinates("US East (N. Virginia)", 38.946728, -77.443386)),
	US_EAST_2("us-east-2", "US East (Ohio)", new Coordinates("US East (Ohio)", 39.958587, -82.997058)),
	US_WEST_1("us-west-1", "US West (N. California)", new Coordinates("US West (N. California)", 37.758891, -122.443318)),
	US_WEST_2("us-west-2", "US West (Oregon)", new Coordinates("US West (Oregon)",45.841904, -119.296774)),
	EU_CENTRAL_1("eu-central-1", "Europe (Frankfurt)", new Coordinates("Europe (Frankfurt)", 50.110991, 8.632203)),
	EU_WEST_1("eu-west-1", "Europe (Ireland)", new Coordinates("Europe (Ireland)", 53.346174, -6.272156)),
	EU_WEST_2("eu-west-2", "Europe (London)", new Coordinates("Europe (London)", 51.516689, -0.134100)),
	EU_SOUTH_1("eu-south-1", "Europe (Milan)", new Coordinates("Europe (Milan)", 45.469902, 9.179905)),
	EU_WEST_3("eu-west-3", "Europe (Paris)", new Coordinates("Europe (Paris)", 48.879382, 2.341615)),
	EU_NORTH_1("eu-north-1", "Europe (Stockholm)", new Coordinates("Europe (Stockholm)", 59.329544, 18.066653)),
	AF_SOUTH_1("af-south-1", "Africa (Cape Town)", new Coordinates("Africa (Cape Town)", -33.953923, 18.566379)),
	AP_EAST_1("ap-east-1", "Asia Pacific (Hong Kong)", new Coordinates("Asia Pacific (Hong Kong)", 22.321326, 114.172109)),
	AP_SOUTH_1("ap-south-1", "Asia Pacific (Mumbai)", new Coordinates("Asia Pacific (Mumbai)", 19.085863, 72.873766)),
	AP_NORTHEAST_1("ap-northeast-1", "Asia Pacific (Tokyo)", new Coordinates("Asia Pacific (Tokyo)", 35.688572, 139.618912)),
	AP_NORTHEAST_2("ap-northeast-2", "Asia Pacific (Seoul)", new Coordinates("Asia Pacific (Seoul)", 37.562049, 127.007511)),
	AP_NORTHEAST_3("ap-northeast-3", "Asia Pacific (Osaka)", new Coordinates("Asia Pacific (Osaka)", 34.675407, 135.496091)),
	AP_SOUTHEAST_1("ap-southeast-1", "Asia Pacific (Singapore)", new Coordinates("Asia Pacific (Singapore)", 1.353010, 103.869377)),
	AP_SOUTHEAST_2("ap-southeast-2", "Asia Pacific (Sydney)", new Coordinates("Asia Pacific (Sydney)", -33.831767, 151.007401)),
	CA_CENTRAL_1("ca-central-1", "Canada (Central)", new Coordinates("Canada (Central)", 45.508968, -73.616289)),
	ME_SOUTH_1("me-south-1", "Middle East (Bahrain)", new Coordinates("Middle East (Bahrain)", 26.233356, 50.585524)),
	SA_EAST_1("sa-east-1", "South America (São Paulo)", new Coordinates("South America (São Paulo)", -23.576129, -46.614103));

	private final String zone;
	private final String name;
	private final Coordinates coordinates;

	public static final AwsRegion DEFAULT_ZONE = US_WEST_2;
}
