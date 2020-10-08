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

package works.weave.socks.queuemaster.controllers;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import works.weave.socks.queuemaster.entities.HealthCheck;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HealthCheckController {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, path = "/health")
	public
	@ResponseBody
	Map<String, List<HealthCheck>> getHealth() {
		Map<String, List<HealthCheck>> map = new HashMap<String, List<HealthCheck>>();
		List<HealthCheck> healthChecks = new ArrayList<HealthCheck>();
		Date dateNow = Calendar.getInstance().getTime();

		HealthCheck app = new HealthCheck("queue-master", "OK", dateNow);
		HealthCheck rabbitmq = new HealthCheck("queue-master-rabbitmq", "OK", dateNow);

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

		healthChecks.add(app);
		healthChecks.add(rabbitmq);

		map.put("health", healthChecks);
		return map;
	}
}
