package org.ldap.core.rules;

import java.util.Objects;
import java.util.regex.Pattern;

import org.ldap.Rest2LdapContext;

public class FormatRules implements AttributeRules {

	private final Pattern pattern;
	
	public FormatRules(String format) {
	    this.pattern = Pattern.compile(format);
	}
	
	@Override
	public boolean isReadable() {		
		return true;
	}

	@Override
	public boolean isUpdatable(Rest2LdapContext context, Object value) {
		return Objects.isNull(value) || 
				(value instanceof String && pattern.matcher(String.valueOf(value)).matches());
	}

}
