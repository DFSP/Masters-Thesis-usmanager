package pt.unl.fct.miei.usmanagement.manager.dtos.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class FieldDTO {

	private Long id;
	private String name;
	private PrometheusQueryEnum prometheusQuery;

}
