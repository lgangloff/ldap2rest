package org.ldap.core.rules;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.ldap.Rest2LdapConfig.AttributeMappingType;
import org.ldap.Rest2LdapContext;

public class TypeRules implements AttributeRules {

	private final AttributeMappingType type;
	private final List<String> possibleValues;
	private final String contentType;
		
	public TypeRules(AttributeMappingType type, String values, String contentType) {
		super();
		this.type = type;
		this.possibleValues = Arrays.asList((values == null) ? new String[]{} : values.split(","));
		this.contentType = contentType;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isUpdatable(Rest2LdapContext context, Object value) {
		if (Objects.isNull(value))
			return true;
		
		return type == AttributeMappingType.STRING && value instanceof String ||
				type == AttributeMappingType.INTEGER && value instanceof Integer ||
				type == AttributeMappingType.ENUM && possibleValues.contains(value) ||
				type == AttributeMappingType.FILE && checkContentType(value);
	}

	private boolean checkContentType(Object value) {
		try {
			return value instanceof byte[] && 
					contentType.equals(
							URLConnection.guessContentTypeFromStream(new ByteArrayInputStream((byte[]) value)));
		} catch (IOException e) {
			return false; // Si on trouve pas le mime-type, on dit que la rules a echoue
		}
	}

}
