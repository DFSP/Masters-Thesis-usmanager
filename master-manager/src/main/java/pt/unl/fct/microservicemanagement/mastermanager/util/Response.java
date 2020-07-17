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

package pt.unl.fct.microservicemanagement.mastermanager.util;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.DuplicateResponseEntryException;
import pt.unl.fct.microservicemanagement.mastermanager.exceptions.ResponseEntryNotFoundException;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class Response {

  private final Set<Map.Entry<Object, Object>> response;
  private final boolean successful;

  public Response add(Object key, Object value) throws DuplicateResponseEntryException {
    final Map.Entry<Object, Object> entry = new AbstractMap.SimpleImmutableEntry<>(key, value);
    if (!response.add(entry)) {
      throw new DuplicateResponseEntryException();
    }
    return this;
  }

  public Response remove(Map.Entry<Object, Object> entry) throws ResponseEntryNotFoundException {
    if (!response.remove(entry)) {
      throw new ResponseEntryNotFoundException();
    }
    return this;
  }

}
