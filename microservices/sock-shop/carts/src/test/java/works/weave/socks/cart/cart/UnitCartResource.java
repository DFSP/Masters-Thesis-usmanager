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

import org.junit.Test;
import works.weave.socks.cart.entities.Cart;
import works.weave.socks.cart.entities.Item;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class UnitCartResource {

	private final String customerId = "testId";
	private final CartDAO.Fake fake = new CartDAO.Fake();

	@Test
	public void whenCartExistsUseThat() {
		Cart cart = new Cart(customerId);
		fake.save(cart);
		CartResource cartResource = new CartResource(fake, customerId);
		assertThat(cartResource.value().get(), equalTo(cart));
	}

	@Test
	public void whenCartDoesntExistCreateNew() {
		CartResource cartResource = new CartResource(fake, customerId);
		assertThat(cartResource.value().get(), is(notNullValue()));
		assertThat(cartResource.value().get().customerId, is(equalTo(customerId)));
	}

	@Test
	public void whenDestroyRemoveItem() {
		Cart cart = new Cart(customerId);
		fake.save(cart);
		CartResource cartResource = new CartResource(fake, customerId);
		cartResource.destroy().run();
		assertThat(fake.findByCustomerId(customerId), is(empty()));
	}

	@Test
	public void whenDestroyOnEmptyStillEmpty() {
		CartResource cartResource = new CartResource(fake, customerId);
		cartResource.destroy().run();
		assertThat(fake.findByCustomerId(customerId), is(empty()));
	}

	@Test
	public void whenCreateDoCreate() {
		CartResource cartResource = new CartResource(fake, customerId);
		cartResource.create().get();
		assertThat(fake.findByCustomerId(customerId), is(not(empty())));
	}

	@Test
	public void contentsShouldBeEmptyWhenNew() {
		CartResource cartResource = new CartResource(fake, customerId);
		cartResource.create().get();
		assertThat(cartResource.contents().get().contents().get(), is(empty()));
	}

	@Test
	public void mergedItemsShouldBeInCart() {
		String person1 = "person1";
		String person2 = "person2";
		Item person1Item = new Item("item1");
		Item person2Item = new Item("item2");
		CartResource cartResource = new CartResource(fake, person1);
		cartResource.contents().get().add(() -> person1Item).run();
		CartResource cartResourceToMerge = new CartResource(fake, person2);
		cartResourceToMerge.contents().get().add(() -> person2Item).run();
		cartResource.merge(cartResourceToMerge.value().get()).run();
		assertThat(cartResource.contents().get().contents().get(), hasSize(2));
		assertThat(cartResource.contents().get().contents().get().get(0), anyOf(equalTo(person1Item), equalTo
			(person2Item)));
		assertThat(cartResource.contents().get().contents().get().get(1), anyOf(equalTo(person1Item), equalTo
			(person2Item)));
		assertThat(cartResourceToMerge.contents().get().contents().get(), hasSize(1));
	}
}
