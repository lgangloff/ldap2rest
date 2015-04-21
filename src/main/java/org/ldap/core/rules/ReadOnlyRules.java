package org.ldap.core.rules;

import org.ldap.Rest2LdapContext;

public class ReadOnlyRules implements AttributeRules {

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isUpdatable(Rest2LdapContext context, Object value) {
		return false;
	}

	
}
