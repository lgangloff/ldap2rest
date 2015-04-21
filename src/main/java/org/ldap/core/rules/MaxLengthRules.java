package org.ldap.core.rules;

import java.util.Objects;

import org.ldap.Rest2LdapContext;

public class MaxLengthRules implements AttributeRules {

	private final Integer maxLength;
	
	public MaxLengthRules(Integer maxLength) {
		this.maxLength = maxLength;
	}
	
	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isUpdatable(Rest2LdapContext context, Object value) {
		return Objects.isNull(value) || (value instanceof String && String.valueOf(value).length() <= maxLength);
	}

}
