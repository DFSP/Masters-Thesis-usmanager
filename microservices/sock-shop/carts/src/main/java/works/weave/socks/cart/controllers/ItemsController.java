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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import works.weave.socks.cart.cart.CartDAO;
import works.weave.socks.cart.cart.CartResource;
import works.weave.socks.cart.entities.Item;
import works.weave.socks.cart.item.FoundItem;
import works.weave.socks.cart.item.ItemDAO;
import works.weave.socks.cart.item.ItemResource;

import java.util.List;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping(value = "/carts/{customerId:.*}/items")
public class ItemsController {
	private final Logger LOG = getLogger(getClass());

	@Autowired
	private ItemDAO itemDAO;
	@Autowired
	private CartsController cartsController;
	@Autowired
	private CartDAO cartDAO;

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/{itemId:.*}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public Item get(@PathVariable String customerId, @PathVariable String itemId) {
		return new FoundItem(() -> getItems(customerId), () -> new Item(itemId)).get();
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public List<Item> getItems(@PathVariable String customerId) {
		return cartsController.get(customerId).contents();
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	public Item addToCart(@PathVariable String customerId, @RequestBody Item item) {
		// If the item does not exist in the cart, create new one in the repository.
		FoundItem foundItem = new FoundItem(() -> cartsController.get(customerId).contents(), () -> item);
		if (!foundItem.hasItem()) {
			Supplier<Item> newItem = new ItemResource(itemDAO, () -> item).create();
			LOG.debug("Did not find item. Creating item for user: " + customerId + ", " + newItem.get());
			new CartResource(cartDAO, customerId).contents().get().add(newItem).run();
			return item;
		}
		else {
			Item newItem = new Item(foundItem.get(), foundItem.get().quantity() + 1);
			LOG.debug("Found item in cart. Incrementing for user: " + customerId + ", " + newItem);
			updateItem(customerId, newItem);
			return newItem;
		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(value = "/{itemId:.*}", method = RequestMethod.DELETE)
	public void removeItem(@PathVariable String customerId, @PathVariable String itemId) {
		FoundItem foundItem = new FoundItem(() -> getItems(customerId), () -> new Item(itemId));
		Item item = foundItem.get();

		LOG.debug("Removing item from cart: " + item);
		new CartResource(cartDAO, customerId).contents().get().delete(() -> item).run();

		LOG.debug("Removing item from repository: " + item);
		new ItemResource(itemDAO, () -> item).destroy().run();
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PATCH)
	public void updateItem(@PathVariable String customerId, @RequestBody Item item) {
		// Merge old and new items
		ItemResource itemResource = new ItemResource(itemDAO, () -> get(customerId, item.itemId()));
		LOG.debug("Merging item in cart for user: " + customerId + ", " + item);
		itemResource.merge(item).run();
	}
}
