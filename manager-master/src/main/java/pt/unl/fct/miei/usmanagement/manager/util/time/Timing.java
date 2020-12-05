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

package pt.unl.fct.miei.usmanagement.manager.util.time;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

@Slf4j
@UtilityClass
public class Timing {

	public void sleep(long time, TimeUnit timeUnit) {
		try {
			timeUnit.sleep(time);
		}
		catch (InterruptedException e) {
			log.error("Unable to sleep {} {}: {}", time, timeUnit.name(), e.getMessage());
		}
	}

	public void wait(BooleanSupplier condition, long timeout) throws TimeoutException {
		wait(condition, 50, timeout);
	}

	public void wait(BooleanSupplier condition, long sleep, long timeout) throws TimeoutException {
		long start = System.currentTimeMillis();
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() - start > timeout) {
				throw new TimeoutException(String.format("Condition not meet within %s ms", timeout));
			}
			Timing.sleep(sleep, TimeUnit.MILLISECONDS);
		}
	}

}
