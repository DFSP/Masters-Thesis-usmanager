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

package pt.unl.fct.miei.usmanagement.manager.workermanagers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.hosts.cloud.CloudHost;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.List;
import java.util.Optional;

public interface WorkerManagers extends JpaRepository<WorkerManager, String> {

	@Query("select case when count(w) > 0 then true else false end "
		+ "from WorkerManager w "
		+ "where w.id = :id")
	boolean hasWorkerManager(@Param("id") String id);

	Optional<WorkerManager> getByContainer(@Param("container") Container container);

	@Query("select c "
		+ "from WorkerManager w join w.assignedCloudHosts c "
		+ "where w.id = :id")
	List<CloudHost> getCloudHosts(@Param("id") String id);

	@Query("select e "
		+ "from WorkerManager w join w.assignedEdgeHosts e "
		+ "where w.id = :id")
	List<EdgeHost> getEdgeHosts(@Param("id") String id);

	List<WorkerManager> getByRegion(RegionEnum region);

}
