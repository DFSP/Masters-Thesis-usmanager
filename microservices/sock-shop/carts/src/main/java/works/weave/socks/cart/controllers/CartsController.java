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

package works.weave.socks.cart.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import works.weave.socks.cart.cart.CartDAO;
import works.weave.socks.cart.cart.CartResource;
import works.weave.socks.cart.entities.Cart;


@RestController
@RequestMapping(path = "/carts")
public class CartsController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CartDAO cartDAO;

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public Cart get(@PathVariable String customerId) {
		return new CartResource(cartDAO, customerId).value().get();
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(value = "/{customerId}", method = RequestMethod.DELETE)
	public void delete(@PathVariable String customerId) {
		new CartResource(cartDAO, customerId).destroy().run();
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(value = "/{customerId}/merge", method = RequestMethod.GET)
	public void mergeCarts(@PathVariable String customerId, @RequestParam(value = "sessionId") String sessionId) {
		logger.debug("Merge carts request received for ids: " + customerId + " and " + sessionId);
		CartResource sessionCart = new CartResource(cartDAO, sessionId);
		CartResource customerCart = new CartResource(cartDAO, customerId);
		customerCart.merge(sessionCart.value().get()).run();
		delete(sessionId);
	}
}
