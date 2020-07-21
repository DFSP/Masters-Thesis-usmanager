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

package pt.unl.fct.microservicemanagement.mastermanager.manager.monitoring;

import java.sql.Timestamp;
import java.util.Objects;

import javax.persistence.*;

import lombok.*;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "host_monitoring")
public class HostMonitoringEntity {

  @Id
  @GeneratedValue
  private Long id;

  private String hostname;

  private String field;

  private double minValue;

  private double maxValue;

  private double sumValue;

  private double lastValue;

  private long count;

  @Basic
  private Timestamp lastUpdate;

  void logValue(double value, Timestamp updateTime) {
    setMinValue(Math.min(value, getMinValue()));
    setMaxValue(Math.max(value, getMaxValue()));
    setLastValue(value);
    setSumValue(getSumValue() + value);
    setCount(getCount() + 1);
    setLastUpdate(updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof HostMonitoringEntity)) {
      return false;
    }
    HostMonitoringEntity other = (HostMonitoringEntity) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

}
