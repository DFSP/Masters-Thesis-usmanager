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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import works.weave.socks.shipping.entities.HealthCheck;
import works.weave.socks.shipping.entities.Shipment;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ITShippingController {
	@MockBean
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ShippingController shippingController;

	@Test
	public void getShipment() throws Exception {
		String shipping = shippingController.getShipping();
		assertThat(shipping, is(notNullValue()));
	}

	@Test
	public void getShipmentById() throws Exception {
		String shipping = shippingController.getShippingById("id");
		assertThat(shipping, is(notNullValue()));
	}

	@Test
	public void newShipment() throws Exception {
		Shipment original = new Shipment("someName");
		Shipment saved = shippingController.postShipping(original);
		verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any(Shipment.class));
		assertThat(original, is(equalTo(saved)));
	}

	@Test
	public void getHealthCheck() throws Exception {
		Map<String, List<HealthCheck>> healthChecks = shippingController.getHealth();
		assertThat(healthChecks.get("health").size(), is(equalTo(2)));
	}

	@Test
	public void doNotCrashWhenNoQueue() throws Exception {
		doThrow(new AmqpException("test error")).when(rabbitTemplate).convertAndSend(anyString(), any(Shipment.class));
		Shipment original = new Shipment("someName");
		Shipment saved = shippingController.postShipping(original);
		verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any(Shipment.class));
		assertThat(original, is(equalTo(saved)));
	}
}
