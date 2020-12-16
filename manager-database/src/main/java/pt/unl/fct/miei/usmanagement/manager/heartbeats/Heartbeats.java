package pt.unl.fct.miei.usmanagement.manager.heartbeats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Heartbeats extends JpaRepository<Heartbeat, String> {

}
