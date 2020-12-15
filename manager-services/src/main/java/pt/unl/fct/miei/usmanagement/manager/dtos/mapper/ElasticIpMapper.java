package pt.unl.fct.miei.usmanagement.manager.dtos.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pt.unl.fct.miei.usmanagement.manager.dtos.kafka.ElasticIpDTO;
import pt.unl.fct.miei.usmanagement.manager.eips.ElasticIp;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ElasticIpMapper {

	ElasticIpMapper MAPPER = Mappers.getMapper(ElasticIpMapper.class);

	@IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
	@Mapping(source = "id", target = "id")
	ElasticIp toElasticIp(ElasticIpDTO elasticIpDTO, @Context CycleAvoidingMappingContext context);

	@InheritInverseConfiguration
	ElasticIpDTO fromElasticIp(ElasticIp elasticIp, @Context CycleAvoidingMappingContext context);

}
