/*
 * MIT License
 *
 * Copyright (c) 2020 usmanager
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

package works.weave.socks.orders.resources;

import java.net.URI;

import org.hibernate.validator.constraints.URL;

public class NewOrderResource {

  @URL
  private URI customer;

  @URL
  private URI address;

  @URL
  private URI card;

  @URL
  private URI items;

  public NewOrderResource() {
  }

  public NewOrderResource(URI customer, URI address, URI card, URI items) {
    this.customer = customer;
    this.address = address;
    this.card = card;
    this.items = items;
  }

  @Override
  public String toString() {
    return "NewOrderResource{"
        + "customer=" + customer
        + ", address=" + address
        + ", card=" + card
        + ", items=" + items
        + '}';
  }

  public URI getCustomer() {
    return customer;
  }

  public void setCustomer(URI customer) {
    this.customer = customer;
  }

  public URI getAddress() {
    return address;
  }

  public void setAddress(URI address) {
    this.address = address;
  }

  public URI getCard() {
    return card;
  }

  public void setCard(URI card) {
    this.card = card;
  }

  public URI getItems() {
    return items;
  }

  public void setItems(URI items) {
    this.items = items;
  }

}
