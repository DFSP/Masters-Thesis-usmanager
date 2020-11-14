package pt.unl.fct.miei.usmanagement.manager.symmetricds;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.db.sql.ISqlTransaction;
import org.jumpmind.symmetric.load.IReloadListener;
import org.jumpmind.symmetric.model.Node;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SymReloadMonitor implements IReloadListener {

	@Override
	public void beforeReload(ISqlTransaction iSqlTransaction, Node node, long l) {

	}

	@Override
	public void afterReload(ISqlTransaction iSqlTransaction, Node node, long l) {
		log.error(iSqlTransaction.toString());
	}
}
