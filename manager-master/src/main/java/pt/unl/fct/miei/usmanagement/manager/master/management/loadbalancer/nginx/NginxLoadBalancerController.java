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

package pt.unl.fct.miei.usmanagement.manager.master.management.loadbalancer.nginx;

import net.minidev.json.JSONArray;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.master.util.Json;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/load-balancer")
public class NginxLoadBalancerController {

	private final NginxLoadBalancerService nginxLoadBalancerService;

	public NginxLoadBalancerController(NginxLoadBalancerService nginxLoadBalancerService) {
		this.nginxLoadBalancerService = nginxLoadBalancerService;
	}

	@PostMapping
	public List<ContainerEntity> launchLoadBalancer(@Json String service, @Json JSONArray regions) {
		return nginxLoadBalancerService.launchLoadBalancers(service, Arrays.asList(regions.toArray(new String[0])));
	}

}
