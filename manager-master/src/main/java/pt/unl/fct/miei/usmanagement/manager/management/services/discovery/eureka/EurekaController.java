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

package pt.unl.fct.miei.usmanagement.manager.management.services.discovery.eureka;

import net.minidev.json.JSONArray;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;
import pt.unl.fct.miei.usmanagement.manager.util.Json;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/eureka-server")
public class EurekaController {

	private final EurekaService eurekaService;

	public EurekaController(EurekaService eurekaService) {
		this.eurekaService = eurekaService;
	}

	@PostMapping
	public List<ContainerEntity> launchEureka(@Json JSONArray regions) {
		List<Region> regionsList = Arrays.stream(regions.toArray(new String[0]))
			.map(region -> Region.valueOf(region.toUpperCase().replace(" ", "_")))
			.collect(Collectors.toList());
		return eurekaService.launchEurekaServers(regionsList);
	}

}
