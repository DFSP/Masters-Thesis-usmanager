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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import works.weave.socks.cart.cart.CartDAO;
import works.weave.socks.cart.entities.Item;
import works.weave.socks.cart.item.ItemDAO;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UnitItemsController {

	@Autowired
	private ItemsController itemsController;

	@Autowired
	private ItemDAO itemDAO;

	@Autowired
	private CartsController cartsController;

	@Test
	public void whenNewItemAdd() {
		Item item = new Item("id", "itemId", 1, 0F);
		String customerId = "customerIdAdd";
		itemsController.addToCart(customerId, item);
		assertThat(itemsController.getItems(customerId), is(hasSize(1)));
		assertThat(itemsController.getItems(customerId), is(org.hamcrest.CoreMatchers.hasItem(item)));
	}

	@Test
	public void whenExistIncrementQuantity() {
		Item item = new Item("id", "itemId", 1, 0F);
		String customerId = "customerIdIncrement";
		itemsController.addToCart(customerId, item);
		itemsController.addToCart(customerId, item);
		assertThat(itemsController.getItems(customerId), is(hasSize(1)));
		assertThat(itemsController.getItems(customerId), is(org.hamcrest.CoreMatchers.hasItem(item)));
		assertThat(itemDAO.findOne(item.id()).quantity(), is(equalTo(2)));
	}

	@Test
	public void shouldRemoveItemFromCart() {
		Item item = new Item("id", "itemId", 1, 0F);
		String customerId = "customerIdRemove";
		itemsController.addToCart(customerId, item);
		assertThat(itemsController.getItems(customerId), is(hasSize(1)));
		itemsController.removeItem(customerId, item.itemId());
		assertThat(itemsController.getItems(customerId), is(hasSize(0)));
	}

	@Test
	public void shouldSetQuantity() {
		Item item = new Item("id", "itemId", 1, 0F);
		String customerId = "customerIdQuantity";
		itemsController.addToCart(customerId, item);
		assertThat(itemsController.getItems(customerId).get(0).quantity(), is(equalTo(item.quantity())));
		Item anotherItem = new Item(item, 15);
		itemsController.updateItem(customerId, anotherItem);
		assertThat(itemDAO.findOne(item.id()).quantity(), is(equalTo(anotherItem.quantity())));
	}

	@Configuration
	static class ItemsControllerTestConfiguration {
		@Bean
		public ItemsController itemsController() {
			return new ItemsController();
		}

		@Bean
		public CartsController cartsController() {
			return new CartsController();
		}

		@Bean
		public ItemDAO itemDAO() {
			return new ItemDAO.Fake();
		}

		@Bean
		public CartDAO cartDAO() {
			return new CartDAO.Fake();
		}
	}
}
