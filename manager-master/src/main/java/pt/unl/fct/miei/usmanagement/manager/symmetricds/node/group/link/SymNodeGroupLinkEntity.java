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

package pt.unl.fct.miei.usmanagement.manager.symmetricds.node.group.link;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@IdClass(SymNodeGroupLinkId.class)
@Table(name = "sym_node_group_link")
public class SymNodeGroupLinkEntity {

	@Id
	private String sourceNodeGroupId;

	@Id
	private String targetNodeGroupId;

	@Builder.Default
	@Column(columnDefinition = "char(1) default 'W'")
	private String dataEventAction = "W";

	@Builder.Default
	@ColumnDefault("1")
	private Short syncConfigEnabled = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short isReversible = 0;

	private LocalDateTime createTime;

	private String lastUpdateBy;

	private LocalDateTime lastUpdateTime;

	@Override
	public int hashCode() {
		return Objects.hash(getSourceNodeGroupId(), getTargetNodeGroupId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SymNodeGroupLinkEntity)) {
			return false;
		}
		SymNodeGroupLinkEntity other = (SymNodeGroupLinkEntity) o;
		return sourceNodeGroupId != null && sourceNodeGroupId.equals(other.getSourceNodeGroupId())
			&& targetNodeGroupId != null && targetNodeGroupId.equals(other.getTargetNodeGroupId());
	}

}
