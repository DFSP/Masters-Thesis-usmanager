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

package pt.unl.fct.miei.usmanagement.manager.database.rulesystem.decision;

import java.util.Map;

import lombok.Data;
import pt.unl.fct.miei.usmanagement.manager.database.rulesystem.rules.RuleDecision;

@Data
public class DecisionResult implements Comparable<DecisionResult> {

  private final String hostname;
  private final RuleDecision decision;
  private final long ruleId;
  private final Map<String, Double> fields;
  private final int priority;
  private final double sumFields;

  @Override
  public int compareTo(DecisionResult o) {
    if (this.getDecision() == o.getDecision()) {
      if (this.getPriority() == o.getPriority()) {
        return this.getSumFields() < o.getSumFields() ? -1 : 1;
      } else {
        return this.getPriority() < o.getPriority() ? -1 : 1;
      }
    } else {
      return this.getDecision() == RuleDecision.START
          || (this.getDecision() == RuleDecision.STOP
          && o.getDecision() == RuleDecision.NONE) ? -1 : 1;
    }
  }

}
