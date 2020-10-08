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

package works.weave.socks.cart.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Document
public class Cart {
	@NotNull
	public String customerId; // Public instead of getters/setters.
	@Id
	private String id;
	@DBRef
	private List<Item> items = new ArrayList<>();

	public Cart(String customerId) {
		this.customerId = customerId;
	}

	public Cart() {
		this(null);
	}

	public List<Item> contents() {
		return items;
	}

	public Cart add(Item item) {
		items.add(item);
		return this;
	}

	public Cart remove(Item item) {
		items.remove(item);
		return this;
	}

	@Override
	public String toString() {
		return "Cart{" +
			"id='" + id + '\'' +
			", customerId='" + customerId + '\'' +
			", items=" + items +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Cart cart = (Cart) o;

		if (customerId != null ? !customerId.equals(cart.customerId) : cart.customerId != null) {
			return false;
		}
		if (id != null ? !id.equals(cart.id) : cart.id != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = customerId != null ? customerId.hashCode() : 0;
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}
}
