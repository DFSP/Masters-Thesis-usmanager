package pt.unl.fct.miei.usmanagement.manager.configurations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface Configurations extends JpaRepository<Configuration, String> {

	@Query("select case when count(c) > 0 then true else false end "
		+ "from Configuration c "
		+ "where c.id = :id")
	boolean isConfiguring(String id);

}
