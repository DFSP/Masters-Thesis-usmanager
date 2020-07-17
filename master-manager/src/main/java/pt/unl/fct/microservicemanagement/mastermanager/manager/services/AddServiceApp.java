package pt.unl.fct.microservicemanagement.mastermanager.manager.services;

import lombok.Data;

@Data
public class AddServiceApp {

  private final String name;
  private final int launchOrder;

}
