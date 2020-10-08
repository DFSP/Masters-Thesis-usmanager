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

import works.weave.socks.cart.action.FirstResultOrDefault;
import works.weave.socks.cart.entities.Cart;

import java.util.function.Supplier;

public class CartResource implements Resource<Cart>, HasContents<CartContentsResource> {
	private final CartDAO cartRepository;
	private final String customerId;

	public CartResource(CartDAO cartRepository, String customerId) {
		this.cartRepository = cartRepository;
		this.customerId = customerId;
	}

	@Override
	public Runnable destroy() {
		return () -> cartRepository.delete(value().get());
	}

	@Override
	public Supplier<Cart> create() {
		return () -> cartRepository.save(new Cart(customerId));
	}

	@Override
	public Supplier<Cart> value() {
		return new FirstResultOrDefault<>(
			cartRepository.findByCustomerId(customerId),
			() -> {
				create().get();
				return value().get();
			});
	}

	@Override
	public Runnable merge(Cart toMerge) {
		return () -> toMerge.contents().forEach(item -> contents().get().add(() -> item).run());
	}

	@Override
	public Supplier<CartContentsResource> contents() {
		return () -> new CartContentsResource(cartRepository, () -> this);
	}
}
