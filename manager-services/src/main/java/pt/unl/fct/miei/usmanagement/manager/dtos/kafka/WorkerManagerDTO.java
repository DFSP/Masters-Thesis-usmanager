package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.workermanagers.WorkerManager;

import java.util.HashSet;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class WorkerManagerDTO {

	private String id;
	private ContainerDTO container;
	private RegionEnum region;

	public WorkerManagerDTO(WorkerManager workerManager) {
		this.id = workerManager.getId();
		this.container = new ContainerDTO(workerManager.getContainer());
		this.region = workerManager.getRegion();
		/*this.isNew = edgeHost.isNew();*/
	}

	@JsonIgnore
	public WorkerManager toEntity() {
		WorkerManager workerManager = WorkerManager.builder()
			.id(id)
			.region(region)
			.container(container.toEntity())
			.build();
		/*workerManager.setNew(isNew);*/
		return workerManager;
	}

}
