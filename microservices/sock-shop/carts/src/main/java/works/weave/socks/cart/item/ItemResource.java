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

package works.weave.socks.cart.item;

import works.weave.socks.cart.cart.Resource;
import works.weave.socks.cart.entities.Item;

import java.util.function.Supplier;

public class ItemResource implements Resource<Item> {
	private final ItemDAO itemRepository;
	private final Supplier<Item> item;

	public ItemResource(ItemDAO itemRepository, Supplier<Item> item) {
		this.itemRepository = itemRepository;
		this.item = item;
	}

	@Override
	public Runnable destroy() {
		return () -> itemRepository.destroy(value().get());
	}

	@Override
	public Supplier<Item> create() {
		return () -> itemRepository.save(item.get());
	}

	@Override
	public Supplier<Item> value() {
		return item;    // Basically a null method. Gets the item from the supplier.
	}

	@Override
	public Runnable merge(Item toMerge) {
		return () -> itemRepository.save(new Item(value().get(), toMerge.quantity()));
	}
}
