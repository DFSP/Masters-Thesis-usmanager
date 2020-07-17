/*
package pt.unl.fct.microserviceManagement.managerMaster.entities.host;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public final class EdgeHostDetails extends HostDetails {

    private final String country;
    private final String city;

    public EdgeHostDetails(String hostname, String continent, String region,
                          final String country, String city) {
        super(hostname, continent, region);
        this.country = country;
        this.city = city;
    }

    @Override
    public String toString() {
        return "[EdgeHostDetails]: " +
                "hostname = " + super.getHostname() +
                ", continent = " + super.getContinent() +
                ", region = " + super.getRegion() +
                ", country = " + country +
                ", city = " + city;
    }

}
*/
