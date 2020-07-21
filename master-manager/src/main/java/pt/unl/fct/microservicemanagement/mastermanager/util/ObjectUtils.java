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

package pt.unl.fct.microservicemanagement.mastermanager.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.UtilityClass;
import org.springframework.beans.BeanUtils;

@UtilityClass
public class ObjectUtils {

  public void copyValidProperties(Object source, Object target) {
    var ignoreProperties = new LinkedList<String>();
    ignoreProperties.add("id");
    // loop to include super classes except Object
    for (Class<?> current = target.getClass(); current.getSuperclass() != null; current = current.getSuperclass()) {
      Arrays.stream(current.getDeclaredFields()).forEach(field -> {
        field.setAccessible(true);
        if (hasInvalidAnnotation(field)) {
          ignoreProperties.add(field.getName());
        }
      });
    }
    BeanUtils.copyProperties(source, target, ignoreProperties.toArray(new String[0]));
  }

  private boolean hasInvalidAnnotation(Field field) {
    return field.getAnnotation(OneToMany.class) != null
        || field.getAnnotation(ManyToMany.class) != null
        /*|| field.getAnnotation(ManyToOne.class) != null*/
        || field.getAnnotation(JsonIgnore.class) != null;
  }

}
