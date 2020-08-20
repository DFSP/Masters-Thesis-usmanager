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

package pt.unl.fct.miei.usmanagement.manager.worker.management.location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Singular;

@Data
public class LocationMonitoring {

  @Singular
  private final Map<String, Map<String, Integer>> serviceLocationsCount; // Service name -> (location key, count)
  @Singular
  private final Map<String, Integer> serviceTotalRequestsCount; // Service name -> requests count

  public LocationMonitoring() {
    this.serviceLocationsCount = new HashMap<>();
    this.serviceTotalRequestsCount = new HashMap<>();
  }

  public void addCount(String service, int count, List<String> locationKeys) {
    Map<String, Integer> locationsCount;
    if (serviceLocationsCount.containsKey(service)) {
      locationsCount = serviceLocationsCount.get(service);
      for (String locationKey : locationKeys) {
        locationsCount.computeIfPresent(locationKey, (key, previousCount) -> previousCount + count);
        locationsCount.putIfAbsent(locationKey, count);
      }
      serviceTotalRequestsCount.put(service, serviceTotalRequestsCount.get(service) + count);
    } else {
      locationsCount = new HashMap<>();
      for (String locationKey : locationKeys) {
        locationsCount.put(locationKey, count);
      }
      serviceTotalRequestsCount.put(service, count);
    }
    serviceLocationsCount.put(service, locationsCount);
  }

  public Map<String, Integer> getServiceLocationsCount(String service) {
    return serviceLocationsCount.get(service);
  }

  public boolean containsServiceLocationsCount(String service) {
    return serviceLocationsCount.containsKey(service);
  }

  public int getServiceTotalRequestsCount(String service) {
    return serviceTotalRequestsCount.get(service);
  }

  public boolean containsTotalRequestsCount(String service) {
    return serviceTotalRequestsCount.containsKey(service);
  }

}
