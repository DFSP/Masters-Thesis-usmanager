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
 *//*


package pt.unl.fct.miei.usmanagement.manager.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pt.unl.fct.miei.usmanagement.manager.worker.kafka.Producer;
import pt.unl.fct.miei.usmanagement.manager.worker.kafka.User;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	private final Producer producer;

	public Application(Producer producer) {
		this.producer = producer;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args).close();
	}

	@Override
	public void run(String... args) throws Exception {
		*/
/*Thread.sleep(2000);
 *//*
 */
/*Customer customer = new Customer("daniel", "pimenta");
    producer.sendCustomer(customer);*//*
 */
/*
    Thread.sleep(2000);
    User user = new User("daniel");
    producer.sendUser(user);
   *//*
 */
/* Thread.sleep(2000);
    producer.sendMessage("fail");*//*
 */
/*
    Thread.sleep(1000000000);*//*

	}

}
*/
