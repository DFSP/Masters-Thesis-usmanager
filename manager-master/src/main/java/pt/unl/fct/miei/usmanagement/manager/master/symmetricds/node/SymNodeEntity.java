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

package pt.unl.fct.miei.usmanagement.manager.master.symmetricds.node;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Setter
@Getter
@Table(name = "sym_node")
public class SymNodeEntity {

	@Id
	private String nodeId;

	@NotNull
	private String nodeGroupId;

	@NotNull
	private String externalId;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short syncEnabled = 0;

	private String syncUrl;

	private String schemaVersion;

	private String symmetricVersion;

	private String configVersion;

	private String databaseType;

	private String databaseVersion;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Integer batchToSendCount = 0;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Integer batchInErrorCount = 0;

	private String createdAtNodeId;

	private String deploymentType;

	private String deploymentSubType;

	@Override
	public int hashCode() {
		return Objects.hashCode(getNodeId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SymNodeEntity)) {
			return false;
		}
		SymNodeEntity other = (SymNodeEntity) o;
		return nodeId != null && nodeId.equals(other.getNodeId());
	}

}
