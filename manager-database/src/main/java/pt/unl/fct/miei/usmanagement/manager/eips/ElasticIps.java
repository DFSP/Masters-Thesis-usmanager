package pt.unl.fct.miei.usmanagement.manager.eips;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;

import java.util.Optional;

public interface ElasticIps extends JpaRepository<ElasticIp, Long> {

	Optional<ElasticIp> getElasticIpByRegion(RegionEnum regionEnum);

	@Query("select case when count(e) > 0 then true else false end "
		+ "from ElasticIp e "
		+ "where e.region = :region")
	boolean hasElasticIp(RegionEnum region);

	Optional<ElasticIp> getElasticIpByAllocationId(String allocationId);
}

