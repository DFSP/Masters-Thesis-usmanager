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

// Adapted from https://github.com/brunocleite/spring-boot-exception-handling

package pt.unl.fct.miei.usmanagement.manager.worker.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;


public class EntityNotFoundException extends RuntimeException {

	public EntityNotFoundException(Class clazz, String... searchParamsMap) {
		super(generateMessage(clazz.getSimpleName(), toMap(String.class, String.class, searchParamsMap)));
	}

	private static String generateMessage(String entity, Map<String, String> searchParams) {
		return String.format("%s with %s was not found", StringUtils.capitalize(entity), searchParams);
	}

	private static <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType, String... entries) {
		if (entries.length % 2 == 1) {
			throw new IllegalArgumentException("Invalid entries");
		}
		return IntStream
			.range(0, entries.length / 2)
			.map(i -> i * 2)
			.collect(HashMap::new, (m, i) -> m.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])), Map::putAll);
	}

}
