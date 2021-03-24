package pt.unl.fct.miei.usmanagement.manager.exceptions;

import org.springframework.util.StringUtils;

public final class BadGatewayException extends RuntimeException {

	public BadGatewayException(Class<?> clazz, String reason) {
		super(String.format("Error connecting with %s: %s", StringUtils.capitalize(clazz.getSimpleName()), reason));
	}

}
