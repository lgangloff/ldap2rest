package org.ldap.core;

import java.util.List;

import org.ldap.beans.LdapEntry;
import org.ldap.criteria.LdapCriteria;
import org.springframework.ldap.core.DistinguishedName;

public interface ILdapDAO {
	public LdapEntry getLdapEntry(DistinguishedName dn);

	public List<LdapEntry> findAllLdapEntry(DistinguishedName dn);

	public List<LdapEntry> findAllLdapEntry(DistinguishedName dn, LdapCriteria criteria);
}
