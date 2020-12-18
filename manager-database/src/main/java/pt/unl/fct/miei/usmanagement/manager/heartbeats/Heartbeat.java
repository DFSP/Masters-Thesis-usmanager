package pt.unl.fct.miei.usmanagement.manager.heartbeats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "heartbeats")
public class Heartbeat {

	@Id
	@NotNull
	private String id;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(pattern = "yyyy-dd-MM HH:mm:ss")
	private LocalDateTime timestamp;

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@PrePersist
	@PreUpdate
	public void setTimestamp() {
		this.timestamp = LocalDateTime.now();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Heartbeat)) {
			return false;
		}
		Heartbeat other = (Heartbeat) o;
		return id != null && id.equals(other.getId());
	}
}
