package pt.unl.fct.miei.usmanagement.manager.loadbalancers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.List;
import java.util.Optional;

public interface LoadBalancers extends JpaRepository<LoadBalancer, String> {

	@Query("select case when count(l) > 0 then true else false end "
		+ "from LoadBalancer l "
		+ "where l.id = :id")
	boolean hasLoadBalancer(@Param("id") String id);

	Optional<LoadBalancer> getByContainer(Container container);

	List<LoadBalancer> getByRegion(RegionEnum region);
}
