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

package works.weave.socks.cart.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import works.weave.socks.cart.entities.Cart;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
public class ITCartRepository {
	@Autowired
	private CartRepository cartRepository;

	@Before
	public void removeAllData() {
		cartRepository.deleteAll();
	}

	@Test
	public void testCartSave() {
		Cart original = new Cart("customerId");
		Cart saved = cartRepository.save(original);

		assertEquals(1, cartRepository.count());
		assertEquals(original, saved);
	}

	@Test
	public void testCartGetDefault() {
		Cart original = new Cart("customerId");
		Cart saved = cartRepository.save(original);

		assertEquals(1, cartRepository.count());
		assertEquals(original, saved);
	}

	@Test
	public void testSearchCustomerById() {
		Cart original = new Cart("customerId");
		cartRepository.save(original);

		List<Cart> found = cartRepository.findByCustomerId(original.customerId);
		assertEquals(1, found.size());
		assertEquals(original, found.get(0));
	}
}
