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

package works.weave.socks.cart.cart;

import org.slf4j.Logger;
import works.weave.socks.cart.entities.Cart;
import works.weave.socks.cart.entities.Item;

import java.util.List;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public class CartContentsResource implements Contents<Item> {
	private final Logger LOG = getLogger(getClass());

	private final CartDAO cartRepository;
	private final Supplier<Resource<Cart>> parent;

	public CartContentsResource(CartDAO cartRepository, Supplier<Resource<Cart>> parent) {
		this.cartRepository = cartRepository;
		this.parent = parent;
	}

	@Override
	public Supplier<List<Item>> contents() {
		return () -> parentCart().contents();
	}

	@Override
	public Runnable add(Supplier<Item> item) {
		return () -> {
			LOG.debug("Adding for user: " + parent.get().value().get().toString() + ", " + item.get());
			cartRepository.save(parentCart().add(item.get()));
		};
	}

	@Override
	public Runnable delete(Supplier<Item> item) {
		return () -> {
			LOG.debug("Deleting for user: " + parent.get().value().get().toString() + ", " + item.get());
			cartRepository.save(parentCart().remove(item.get()));
		};
	}

	private Cart parentCart() {
		return parent.get().value().get();
	}
}
