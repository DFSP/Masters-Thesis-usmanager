package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceKey;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class AppServiceDTO {

	private AppServiceKey id;
	private int launchOrder;
	private AppDTO app;
	private ServiceDTO service;

}
