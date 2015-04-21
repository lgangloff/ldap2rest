package org.ldap.core.rules;

import java.util.List;
import java.util.Objects;

import org.ldap.Rest2LdapContext;

public class MultipleRules implements AttributeRules {

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isUpdatable(Rest2LdapContext context, Object value) {
		return Objects.nonNull(value) && value instanceof List;
	}

}
