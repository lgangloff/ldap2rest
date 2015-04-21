package org.ldap.core.dao;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ldap.beans.LdapEntry;
import org.ldap.core.ILdapDAO;
import org.ldap.criteria.LdapCriteria;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

public class LdapDAOImpl implements ILdapDAO {

	private static final Logger log = Logger.getLogger(LdapDAOImpl.class);
	
	private LdapTemplate ldapTemplate;
	private ContextMapper mapper;
	
	
	@Override
	public LdapEntry getLdapEntry(DistinguishedName dn) {

		log.debug("Looking up for "+dn);
		Object lookup = ldapTemplate.lookup(dn, mapper);
		log.debug("Returning ["+lookup+"]");
		
		return (LdapEntry) lookup;
	}

	@Override
	public LdapEntry updateLdapEntry(LdapEntry ldapEntry) {

		DirContextOperations context = ldapTemplate.lookupContext(ldapEntry.getDn());
		for (Entry<String, List<Object>> entry : ldapEntry.getAttributesAsMap().entrySet()) {
			
			context.setAttributeValues(entry.getKey(), entry.getValue().toArray());
			
		}
		
		log.debug("Updating LdapEntry "+ldapEntry);
		
		ldapTemplate.modifyAttributes(context);
		
		return getLdapEntry(ldapEntry.getDn());
	}



	@Override
	public List<LdapEntry> findAllLdapEntry(DistinguishedName dn) {
		return findAllLdapEntry(dn, "");
	}

	@Override
	public List<LdapEntry> findAllLdapEntry(DistinguishedName dn, LdapCriteria criteria) {
		return findAllLdapEntry(dn, criteria.encode());
	}


	@SuppressWarnings("unchecked")
	private List<LdapEntry> findAllLdapEntry(DistinguishedName dn, String filter) {
		
		List<LdapEntry> result;

		log.debug("Searching all "+dn+" with filter "+ filter);
		
		if (StringUtils.isBlank(filter)){
			result = ldapTemplate.listBindings(dn, mapper);
		}
		else{
			result = ldapTemplate.search(dn, filter, mapper);
		}
		
		log.debug("Returning ["+result.size()+"]");
		return result;
	}



	public void setLdapTemplate(LdapTemplate ldapTemplate){
		this.ldapTemplate = ldapTemplate;
	}


	public void setMapper(ContextMapper mapper) {
		this.mapper = mapper;
	}
}
