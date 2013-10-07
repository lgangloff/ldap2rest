package org.ldap.core;

import org.ldap.beans.LdapEntry;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class DefaultContextMapper implements ContextMapper{

	@Override
	public Object mapFromContext(Object context) {
		DirContextAdapter ctx = (DirContextAdapter) context;
	
		LdapEntry entry = new LdapEntry(ctx.getDn(), ctx.getAttributes());
		return entry;		
	}
}
