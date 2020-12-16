package pt.unl.fct.miei.usmanagement.manager.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, scope = FieldDTO.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HeartbeatDTO {

	private String id;
	@JsonFormat(pattern = "yyyy-dd-MM HH:mm:ss")
	private LocalDateTime time;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HeartbeatDTO)) {
			return false;
		}
		HeartbeatDTO other = (HeartbeatDTO) o;
		return id != null && id.equals(other.getId());
	}

}
