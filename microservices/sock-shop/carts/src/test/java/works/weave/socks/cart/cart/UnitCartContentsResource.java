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

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;
import works.weave.socks.cart.entities.Cart;
import works.weave.socks.cart.entities.Item;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class UnitCartContentsResource {
	private final String customerId = "testId";
	private final CartDAO.Fake fakeDAO = new CartDAO.Fake();
	private final Resource<Cart> fakeCartResource = new Resource.CartFake(customerId);

	@Test
	public void shouldAddAndReturnContents() {
		CartContentsResource contentsResource = new CartContentsResource(fakeDAO, () -> fakeCartResource);
		Item item = new Item("testId");
		contentsResource.add(() -> item).run();
		assertThat(contentsResource.contents().get(), IsCollectionWithSize.hasSize(1));
		assertThat(contentsResource.contents().get(), containsInAnyOrder(item));
	}

	@Test
	public void shouldStartEmpty() {
		CartContentsResource contentsResource = new CartContentsResource(fakeDAO, () -> fakeCartResource);
		assertThat(contentsResource.contents().get(), IsCollectionWithSize.hasSize(0));
	}

	@Test
	public void shouldDeleteItemFromCart() {
		CartContentsResource contentsResource = new CartContentsResource(fakeDAO, () -> fakeCartResource);
		Item item = new Item("testId");
		contentsResource.add(() -> item).run();
		assertThat(contentsResource.contents().get(), IsCollectionWithSize.hasSize(1));
		assertThat(contentsResource.contents().get(), containsInAnyOrder(item));
		Item item2 = new Item(item.itemId());
		contentsResource.delete(() -> item2).run();
		assertThat(contentsResource.contents().get(), IsCollectionWithSize.hasSize(0));
	}
}
