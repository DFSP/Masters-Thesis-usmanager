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

package pt.unl.fct.miei.usmanagement.manager.database.workermanagers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import pt.unl.fct.miei.usmanagement.manager.database.containers.ContainerEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.cloud.CloudHostEntity;
import pt.unl.fct.miei.usmanagement.manager.database.hosts.edge.EdgeHostEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "worker_managers")
public class WorkerManagerEntity {

	@Id
	private String id;

	@NotNull
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss.SSS")
	private LocalDateTime startedAt;

	@NotNull
	@OneToOne(cascade = CascadeType.REMOVE)
	private ContainerEntity container;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "managedByWorker", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CloudHostEntity> assignedCloudHosts;

	@Singular
	@JsonIgnore
	@OneToMany(mappedBy = "managedByWorker", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<EdgeHostEntity> assignedEdgeHosts;

	@PrePersist
	private void ensure() {
		this.setId(UUID.randomUUID().toString());
		if (this.getStartedAt() == null) {
			this.setStartedAt(LocalDateTime.now());
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof WorkerManagerEntity)) {
			return false;
		}
		WorkerManagerEntity other = (WorkerManagerEntity) o;
		return id != null && id.equals(other.getId());
	}

}
