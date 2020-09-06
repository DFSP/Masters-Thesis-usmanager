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

package pt.unl.fct.miei.usmanagement.manager.master.symmetricds.trigger;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "sym_trigger")
public class SymTriggerEntity {

	@Id
	private String triggerId;

	private String sourceCatalogName;

	private String sourceSchemaName;

	@NotNull
	private String sourceTableName;

	@NotNull
	private String channelId;

	@Builder.Default
	@Column(columnDefinition = "varchar(128) default 'reload'")
	private String reloadChannelId = "reload";

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Short syncOnUpdate = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Short syncOnInsert = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Short syncOnDelete = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short syncOnIncomingBatch = 0;

	private String nameForUpdateTrigger;

	private String nameForInsertTrigger;

	private String nameForDeleteTrigger;

	private String syncOnUpdateCondition;

	private String syncOnInsertCondition;

	private String syncOnDeleteCondition;

	private String customBeforeUpdateText;

	private String customBeforeInsertText;

	private String customBeforeDeleteText;

	private String customOnUpdateText;

	private String customOnInsertText;

	private String customOnDeleteText;

	private String externalSelect;

	private String txIdExpression;

	private String channelExpression;

	private String excludedColumnNames;

	private String includedColumnNames;

	private String syncKeyNames;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short useStreamLobs = 0;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short useCaptureLobs = 0;

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Short useCaptureOldData = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 1")
	private Short useHandleKeyUpdates = 1;

	@Builder.Default
	@Column(columnDefinition = "integer default 0")
	private Short streamRow = 1;

	@NotNull
	private LocalDateTime createTime;

	private String lastUpdateBy;

	@NotNull
	private LocalDateTime lastUpdateTime;

	private String description;

	@Override
	public int hashCode() {
		return Objects.hashCode(getTriggerId());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SymTriggerEntity)) {
			return false;
		}
		SymTriggerEntity other = (SymTriggerEntity) o;
		return triggerId != null && triggerId.equals(other.getTriggerId());
	}

}
