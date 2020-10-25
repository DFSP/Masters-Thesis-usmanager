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

package pt.unl.fct.miei.usmanagement.manager.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.ManagerWorkerProperties;

@Slf4j
@Service
public class Consumer {

	private final String ownTopic;
	private final ObjectMapper objectMapper;

	public Consumer(ManagerWorkerProperties managerWorkerProperties) {
		this.ownTopic = String.format("worker-manager-%s", managerWorkerProperties.getId());
		this.objectMapper = new ObjectMapper();
	}

  /*public String getOwnTopic() {
    return ownTopic;
  }

  @KafkaListener(topics = "#{__listener.ownTopic}")
  public void customer(Customer customer) {
    log.info("Received: " + customer);
  }

  @KafkaListener(topics = "#{__listener.ownTopic}.DLT")
  public void customerDLT(String in) {
    log.info("Received from DLT: " + in);
  }*/

  /*@KafkaListener(topics = "users")
  public void user(User u) {
    log.info("Received: " + u);
  }*/

  /*@KafkaListener(topics = "users")
  public void test(String user) {
    //log.info(user);
    User u = parseJson(user, User.class);
    if (u != null) {
      log.info(u.toString());
    } else {
      log.info("user is null");
    }
  }*/


  /*private <T> T parseJson(String json, Class<?> jsonClass) {
    try {
      return (T)objectMapper.readValue(json, jsonClass);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }*/
}
