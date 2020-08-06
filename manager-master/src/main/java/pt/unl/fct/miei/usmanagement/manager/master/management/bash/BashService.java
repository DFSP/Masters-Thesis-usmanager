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

package pt.unl.fct.miei.usmanagement.manager.master.management.bash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.master.exceptions.MasterManagerException;

@Slf4j
@Service
public class BashService {

  public String getUsername() {
    BashCommandResult usernameResult = executeCommand("whoami");
    if (!usernameResult.isSuccessful()) {
      throw new MasterManagerException("Unable to get username of this machine: %s", usernameResult.getError());
    }
    return usernameResult.getOutput().get(0);
  }

  public String getPublicIp() {
    BashCommandResult publicIpResult = executeCommand("curl https://ipinfo.io/ip");
    if (!publicIpResult.isSuccessful()) {
      throw new MasterManagerException("Unable to get public ip: %s", publicIpResult.getError());
    }
    return publicIpResult.getOutput().get(0);
  }

  public String getPrivateIp() {
    BashCommandResult privateIpResult = executeCommand("hostname -I | awk '{print $1}'");
    if (!privateIpResult.isSuccessful()) {
      throw new MasterManagerException("Unable to get private ip: %s", privateIpResult.getError());
    }
    return privateIpResult.getOutput().get(0);
  }

  public BashCommandResult executeCommand(String command) {
    Runtime r = Runtime.getRuntime();
    String[] commands = {"bash", "-c", command};
    try {
      Process p = r.exec(commands);
      p.waitFor();
      int exitStatus = p.exitValue();
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      List<String> output = new LinkedList<>();
      String s;
      while ((s = stdInput.readLine()) != null) {
        output.add(s);
      }
      stdInput.close();
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      List<String> error = new LinkedList<>();
      while ((s = stdError.readLine()) != null) {
        error.add(s);
      }
      stdError.close();
      log.info("Command: {}\nResult: {}\nError: {}", command, String.join("\n", output), String.join("\n", error));
      return new BashCommandResult(command, exitStatus, output, error);
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
      return new BashCommandResult(command, -1, null, List.of(e.getMessage()));
    }
  }

}
