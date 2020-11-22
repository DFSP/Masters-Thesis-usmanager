package pt.unl.fct.miei.usmanagement.manager.management.symmetricds;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.management.services.discovery.registration.RegistrationServerService;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.SymTriggerEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.SymTriggersRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SymService {

	private final SymTriggersRepository symTriggersRepository;

	public SymService(SymTriggersRepository symTriggersRepository) {
		this.symTriggersRepository = symTriggersRepository;
	}

	public void handleContainerTriggers(Container container) {
		String name = container.getName();
		if (!name.startsWith(RegistrationServerService.REGISTRATION_SERVER) && !name.startsWith(NginxLoadBalancerService.LOAD_BALANCER)) {
			return;
		}
		String containerId = container.getId();
		List.of("container_labels", "container_mounts", "container_ports").forEach(table -> {
			Optional<SymTriggerEntity> currentTrigger = symTriggersRepository.findById("master-" + table);
			if (currentTrigger.isPresent()) {
				SymTriggerEntity symTrigger = currentTrigger.get();
				String syncCondition = symTrigger.getSyncOnInsertCondition();
				if (syncCondition != null && syncCondition.endsWith(")")) {
					syncCondition = syncCondition.substring(0, syncCondition.length() - 1);
					syncCondition += " or NEW_CONTAINER_ID = '" + containerId + "')";
				}
				else {
					syncCondition = "(NEW_CONTAINER_ID = '" + containerId + "')";
				}
				log.info("updating condition " + syncCondition);
				symTrigger.setSyncOnInsertCondition(syncCondition);
				symTrigger.setSyncOnUpdateCondition(syncCondition);
				symTrigger.setSyncOnDeleteCondition(syncCondition);
				symTrigger.setLastUpdateTime(LocalDateTime.now());
				symTriggersRepository.save(symTrigger);
			}
			else {
				symTriggersRepository.save(SymTriggerEntity.builder()
					.triggerId(table)
					.sourceTableName(table)
					.channelId("default")
					.syncOnInsertCondition("(NEW_CONTAINER_ID = '" + containerId + "')")
					.syncOnUpdateCondition("(NEW_CONTAINER_ID = '" + containerId + "')")
					.syncOnDeleteCondition("(NEW_CONTAINER_ID = '" + containerId + "')")
					.lastUpdateTime(LocalDateTime.now())
					.createTime(LocalDateTime.now())
					.build());
			}
		});
	}

}
