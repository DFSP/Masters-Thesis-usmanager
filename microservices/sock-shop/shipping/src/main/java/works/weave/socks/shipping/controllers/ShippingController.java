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

package works.weave.socks.shipping.controllers;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import works.weave.socks.shipping.entities.HealthCheck;
import works.weave.socks.shipping.entities.Shipment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ShippingController {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@RequestMapping(value = "/shipping", method = RequestMethod.GET)
	public String getShipping() {
		return "GET ALL Shipping Resource.";
	}

	@RequestMapping(value = "/shipping/{id}", method = RequestMethod.GET)
	public String getShippingById(@PathVariable String id) {
		return "GET Shipping Resource with id: " + id;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/shipping", method = RequestMethod.POST)
	public
	@ResponseBody
	Shipment postShipping(@RequestBody Shipment shipment) {
		System.out.println("Adding shipment to queue...");
		try {
			rabbitTemplate.convertAndSend("shipping-task", shipment);
		}
		catch (Exception e) {
			System.out.println("Unable to add to queue (the queue is probably down). Accepting anyway. Don't do this " +
				"for real!");
		}
		return shipment;
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, path = "/health")
	public
	@ResponseBody
	Map<String, List<HealthCheck>> getHealth() {
		Map<String, List<HealthCheck>> map = new HashMap<String, List<HealthCheck>>();
		List<HealthCheck> healthChecks = new ArrayList<HealthCheck>();
		Date dateNow = Calendar.getInstance().getTime();

		HealthCheck rabbitmq = new HealthCheck("shipping-rabbitmq", "OK", dateNow);
		HealthCheck app = new HealthCheck("shipping", "OK", dateNow);

		try {
			this.rabbitTemplate.execute(new ChannelCallback<String>() {
				@Override
				public String doInRabbit(Channel channel) throws Exception {
					Map<String, Object> serverProperties = channel.getConnection().getServerProperties();
					return serverProperties.get("version").toString();
				}
			});
		}
		catch (AmqpException e) {
			rabbitmq.setStatus("err");
		}

		healthChecks.add(rabbitmq);
		healthChecks.add(app);

		map.put("health", healthChecks);
		return map;
	}
}
