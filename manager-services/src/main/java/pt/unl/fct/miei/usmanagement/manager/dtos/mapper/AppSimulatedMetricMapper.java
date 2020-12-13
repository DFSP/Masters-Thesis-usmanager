package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppRuleDTO;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.AppSimulatedMetricDTO;
import pt.unl.fct.miei.usmanagement.manager.metrics.simulated.AppSimulatedMetric;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.AppRule;

@Mapper(builder = @Builder(disableBuilder = true))
public interface AppSimulatedMetricMapper {

	AppSimulatedMetricMapper MAPPER = Mappers.getMapper(AppSimulatedMetricMapper.class);

	@Mapping(source = "id", target = "id")
	AppSimulatedMetric toAppSimulatedMetric(AppSimulatedMetricDTO appSimulatedMetricDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	AppSimulatedMetricDTO fromAppSimulatedMetric(AppSimulatedMetric appSimulatedMetric, @Context CycleAvoidingMappingContext context);

}
