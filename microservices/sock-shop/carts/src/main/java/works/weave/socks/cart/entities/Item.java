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
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document
public class Item {
	@Id
	private String id;

	@NotNull(message = "Item Id must not be null")
	private String itemId;
	private int quantity;
	private float unitPrice;

	public Item(String id, String itemId, int quantity, float unitPrice) {
		this.id = id;
		this.itemId = itemId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public Item() {
		this(null, "", 1, 0F);
	}

	public Item(String itemId) {
		this(null, itemId, 1, 0F);
	}

	public Item(Item item, String id) {
		this(id, item.itemId, item.quantity, item.unitPrice);
	}

	public Item(Item item, int quantity) {
		this(item.id(), item.itemId, quantity, item.unitPrice);
	}

	public String id() {
		return id;
	}

	public String itemId() {
		return itemId;
	}

	public int quantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return "Item{" +
			"id='" + id + '\'' +
			", itemId='" + itemId + '\'' +
			", quantity=" + quantity +
			", unitPrice=" + unitPrice +
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

		Item item = (Item) o;

		return itemId != null ? itemId.equals(item.itemId) : item.itemId == null;
	}

	// ****** Crappy getter/setters for Jackson JSON invoking ********

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public float getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
}
