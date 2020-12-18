package pt.unl.fct.miei.usmanagement.manager.heartbeats;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Heartbeats extends JpaRepository<Heartbeat, String> {

}
