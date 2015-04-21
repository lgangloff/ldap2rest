package org.ldap.core.rules;

import org.ldap.Rest2LdapContext;

public interface AttributeRules {

	public boolean isReadable();
	public boolean isUpdatable(Rest2LdapContext context, Object value);
}
