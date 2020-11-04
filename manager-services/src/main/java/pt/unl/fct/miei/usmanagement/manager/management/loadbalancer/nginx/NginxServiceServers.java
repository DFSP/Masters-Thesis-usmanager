package pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public final class NginxServiceServers implements Serializable {

	private static final long serialVersionUID = -1737876658697014089L;

	private String service;
	private List<NginxServer> servers;

}
