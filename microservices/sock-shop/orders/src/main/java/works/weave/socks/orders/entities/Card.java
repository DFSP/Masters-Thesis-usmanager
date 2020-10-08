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

package works.weave.socks.orders.entities;

import org.springframework.data.annotation.Id;

public class Card {

	@Id
	private String id;

	private String longNum;
	private String expires;
	private String ccv;

	public Card() {
	}

	public Card(String id, String longNum, String expires, String ccv) {
		this.id = id;
		this.longNum = longNum;
		this.expires = expires;
		this.ccv = ccv;
	}

	public Card(String longNum, String expires, String ccv) {
		this(null, longNum, expires, ccv);
	}

	@Override
	public String toString() {
		return "Card{" +
			"id=" + id +
			", longNum='" + longNum + '\'' +
			", expires='" + expires + '\'' +
			", ccv='" + ccv + '\'' +
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

		Card card = (Card) o;

		return getId().equals(card.getId());

	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLongNum() {
		return longNum;
	}

	public void setLongNum(String longNum) {
		this.longNum = longNum;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public String getCcv() {
		return ccv;
	}

	public void setCcv(String ccv) {
		this.ccv = ccv;
	}
}
