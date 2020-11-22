/*
 * MIT License
 *  
 * Copyright (c) 2020 manager
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.symmetricds.trigger.router;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
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
@IdClass(SymTriggerRouterId.class)
@Table(name = "sym_trigger_router")
public class SymTriggerRouterEntity {

	@Id
	private String triggerId;

	@Id
	private String routerId;

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Short enabled = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Integer initialLoadOrder = 1;

	private String initialLoadSelect;

	private String initialLoadDeleteStmt;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short pingBackEnabled = 0;

	@NotNull
	private LocalDateTime createTime;

	private String lastUpdateBy;

	@NotNull
	private LocalDateTime lastUpdateTime;

	private String description;

	@Override
	public int hashCode() {
		return Objects.hash(getTriggerId(), getRouterId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SymTriggerRouterEntity)) {
			return false;
		}
		SymTriggerRouterEntity other = (SymTriggerRouterEntity) o;
		return triggerId != null && triggerId.equals(other.getTriggerId())
			&& routerId != null && routerId.equals(other.getRouterId());
	}

}
