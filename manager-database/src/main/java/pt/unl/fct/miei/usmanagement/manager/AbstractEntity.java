package pt.unl.fct.miei.usmanagement.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Persistable;

import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

/*@MappedSuperclass*/
public abstract class AbstractEntity<ID> implements Persistable<ID> {

	@Transient
	private boolean isNew = true;

	@PrePersist
	@PostLoad
	void markNotNew() {
		this.isNew = false;
	}

	@JsonIgnore
	@Override
	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
}
