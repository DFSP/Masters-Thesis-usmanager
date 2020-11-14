package pt.unl.fct.miei.usmanagement.manager.nodes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface Nodes extends JpaRepository<Node, Long> {

	Optional<Node> findNodeByNodeId(String id);

	List<Node> findByPublicIpAddress(String publicIpAddress);

	List<Node> findByAvailability(NodeAvailability nodeAvailability);

	List<Node> findByState(String state);

	List<Node> findByStateAndManagerStatusIsNotNull(String ready);

	List<Node> findByStateAndManagerStatusIsNull(String ready);

	@Query("select case when count(n) > 0 then true else false end "
		+ "from Node n "
		+ "where n.nodeId = :nodeId")
	boolean hasNode(String nodeId);
}
