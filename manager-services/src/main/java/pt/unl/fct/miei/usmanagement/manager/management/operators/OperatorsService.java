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

package pt.unl.fct.miei.usmanagement.manager.management.operators;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.operators.Operators;
import pt.unl.fct.miei.usmanagement.manager.util.ObjectUtils;

import java.util.List;

@Slf4j
@Service
public class OperatorsService {

	private final Operators operators;

	public OperatorsService(Operators operators) {
		this.operators = operators;
	}

	public List<Operator> getOperators() {
		return operators.findAll();
	}

	public Operator getOperator(Long id) {
		return operators.findById(id).orElseThrow(() ->
			new EntityNotFoundException(Operator.class, "id", id.toString()));
	}

	public Operator getOperator(String operatorName) {
		OperatorEnum operator = OperatorEnum.valueOf(operatorName.toUpperCase());
		return getOperator(operator);
	}

	public Operator getOperator(OperatorEnum operator) {
		return operators.findByOperator(operator).orElseThrow(() ->
			new EntityNotFoundException(Operator.class, "name", operator.name()));
	}

	public Operator addOperator(Operator operator) {
		checkOperatorDoesntExist(operator);
		log.info("Saving operator {}", ToStringBuilder.reflectionToString(operator));
		return operators.save(operator);
	}

	public Operator updateOperator(String operatorName, Operator newOperator) {
		Operator operator = getOperator(operatorName);
		log.info("Updating operator {} with {}", ToStringBuilder.reflectionToString(operator), ToStringBuilder.reflectionToString(newOperator));
		ObjectUtils.copyValidProperties(newOperator, operator);
		operator = operators.save(operator);
		return operator;
	}

	public void deleteOperator(String operatorName) {
		Operator operator = getOperator(operatorName);
		operators.delete(operator);
	}

	private void checkOperatorDoesntExist(Operator operator) {
		OperatorEnum op = operator.getOperator();
		if (operators.hasOperator(op)) {
			throw new DataIntegrityViolationException("Operator '" + op.getSymbol() + "' already exists");
		}
	}

}
