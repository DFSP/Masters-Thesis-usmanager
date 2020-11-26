package pt.unl.fct.miei.usmanagement.manager.kafka;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.List;
import java.util.Optional;

public interface KafkaBrokers extends JpaRepository<KafkaBroker, String> {

	@Query("select case when count(r) > 0 then true else false end "
		+ "from Zookeeper r "
		+ "where r.id = :id")
	boolean hasKafkaBroker(@Param("id") String id);

	Optional<KafkaBroker> getByContainer(Container container);

	List<KafkaBroker> getByRegion(RegionEnum region);
}
