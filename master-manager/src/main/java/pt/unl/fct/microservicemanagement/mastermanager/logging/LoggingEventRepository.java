package pt.unl.fct.microservicemanagement.mastermanager.logging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggingEventRepository extends JpaRepository<LoggingEventEntity, Long> {

}
