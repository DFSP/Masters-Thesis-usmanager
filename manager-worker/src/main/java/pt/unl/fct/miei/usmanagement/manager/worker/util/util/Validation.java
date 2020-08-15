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

package pt.unl.fct.miei.usmanagement.manager.worker.util.util;

import lombok.experimental.UtilityClass;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.BadRequestException;

@UtilityClass
public class Validation {

  public void validatePostRequest(String requestBodyId) {
    if (requestBodyId != null && !requestBodyId.isEmpty()) {
      throw new BadRequestException("Expected empty request body id, instead got %d", requestBodyId);
    }
  }

  public void validatePostRequest(Long requestBodyId) {
    if (requestBodyId != null && requestBodyId > 0) {
      throw new BadRequestException("Expected non positive request body id, instead got %d", requestBodyId);
    }
  }

  public void validatePutRequest(String requestBodyId) {
    if (requestBodyId == null || requestBodyId.isEmpty()) {
      throw new BadRequestException("Expected non empty request body id");
    }
  }

  public void validatePutRequest(Long requestBodyId) {
    if (requestBodyId == null || requestBodyId < 1) {
      throw new BadRequestException("Expected positive request body id, instead got %d", requestBodyId);
    }
  }

}
