package pt.unl.fct.miei.usmanagement.manager.configurations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface Configurations extends JpaRepository<Configuration, String> {

}
