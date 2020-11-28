package pt.unl.fct.miei.usmanagement.manager.registrationservers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.unl.fct.miei.usmanagement.manager.containers.Container;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.List;
import java.util.Optional;

public interface RegistrationServers extends JpaRepository<RegistrationServer, String> {

	@Query("select case when count(r) > 0 then true else false end "
		+ "from RegistrationServer r "
		+ "where r.id = :id")
	boolean hasRegistrationServer(@Param("id") String id);

	@Query("select case when count(r) > 0 then true else false end "
		+ "from RegistrationServer r "
		+ "where r.container.id = :id")
	boolean hasRegistrationServerByContainer(@Param("id") String id);

	Optional<RegistrationServer> getByContainer(Container container);

	List<RegistrationServer> getByRegion(RegionEnum region);
}
