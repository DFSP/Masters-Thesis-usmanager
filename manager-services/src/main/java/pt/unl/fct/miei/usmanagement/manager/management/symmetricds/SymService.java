package pt.unl.fct.miei.usmanagement.manager.management.symmetricds;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.management.services.discovery.registration.RegistrationServerService;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.SymTriggerEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.SymTriggersRepository;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.router.SymTriggerRouterEntity;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.router.SymTriggerRouterId;
import pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.router.SymTriggerRoutersRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SymService {

	private final SymTriggersRepository symTriggersRepository;
	private final SymTriggerRoutersRepository symTriggerRoutersRepository;

	public SymService(SymTriggersRepository symTriggersRepository, SymTriggerRoutersRepository symTriggerRoutersRepository) {
		this.symTriggersRepository = symTriggersRepository;
		this.symTriggerRoutersRepository = symTriggerRoutersRepository;
	}

	public void handleContainerTriggers(Container container) {
		/*String name = container.getName();
		if (!name.startsWith(RegistrationServerService.REGISTRATION_SERVER) && !name.startsWith(NginxLoadBalancerService.LOAD_BALANCER)) {
			return;
		}
		String containerId = container.getId();
		List.of("container_labels", "container_mounts", "container_ports", "container_rule", "container_simulated_metric").forEach(table -> {
			updateSymTrigger(containerId, table);
			updateSymTriggerRouter(containerId, table);
		});*/
	}

	private void updateSymTriggerRouter(String containerId, String table) {
		SymTriggerRouterId symTriggerRouterId = new SymTriggerRouterId("master-" + table, "master-to-worker");
		Optional<SymTriggerRouterEntity> currentTriggerRouter = symTriggerRoutersRepository.findById(symTriggerRouterId);
		if (currentTriggerRouter.isPresent()) {
			SymTriggerRouterEntity symTriggerRouter = currentTriggerRouter.get();
			String initialLoadSelect = symTriggerRouter.getInitialLoadSelect();
			if (initialLoadSelect != null && initialLoadSelect.endsWith(")")) {
				initialLoadSelect = initialLoadSelect.substring(0, initialLoadSelect.length() - 1);
				initialLoadSelect += " or CONTAINER_ID = '" + containerId + "')";
			}
			else {
				initialLoadSelect = "(CONTAINER_ID = '" + containerId + "')";
			}
			log.info("updating condition " + initialLoadSelect);
			symTriggerRouter.setInitialLoadSelect(initialLoadSelect);
			symTriggerRouter.setLastUpdateTime(LocalDateTime.now());
			symTriggerRoutersRepository.save(symTriggerRouter);
		}
		else {
			symTriggerRoutersRepository.save(SymTriggerRouterEntity.builder()
				.triggerId("master-" + table)
				.routerId("master-to-worker")
				.initialLoadOrder(1)
				.initialLoadSelect("(CONTAINER_ID = '" + containerId + "')")
				.createTime(LocalDateTime.now())
				.lastUpdateTime(LocalDateTime.now())
				.build());
		}
	}

	private void updateSymTrigger(String containerId, String table) {
		Optional<SymTriggerEntity> currentTrigger = symTriggersRepository.findById("master-" + table);
		if (currentTrigger.isPresent()) {
			SymTriggerEntity symTrigger = currentTrigger.get();
			String syncCondition = symTrigger.getSyncOnInsertCondition();
			if (syncCondition != null && syncCondition.endsWith(")")) {
				syncCondition = syncCondition.substring(0, syncCondition.length() - 1);
				syncCondition += " or OLD_CONTAINER_ID = '" + containerId + "' or NEW_CONTAINER_ID = '" + containerId + "')";
			}
			else {
				syncCondition = "(OLD_CONTAINER_ID = '" + containerId + "' or NEW_CONTAINER_ID = '" + containerId + "')";
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
				.syncOnInsertCondition("(OLD_CONTAINER_ID = '" + containerId + "' or NEW_CONTAINER_ID = '" + containerId + "')")
				.syncOnUpdateCondition("(OLD_CONTAINER_ID = '" + containerId + "' or NEW_CONTAINER_ID = '" + containerId + "')")
				.syncOnDeleteCondition("(OLD_CONTAINER_ID = '" + containerId + "' or NEW_CONTAINER_ID = '" + containerId + "')")
				.lastUpdateTime(LocalDateTime.now())
				.createTime(LocalDateTime.now())
				.build());
		}
	}

}
