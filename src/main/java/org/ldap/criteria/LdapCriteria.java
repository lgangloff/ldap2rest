package org.ldap.criteria;

import org.springframework.ldap.filter.Filter;

/**
 * Une criteria pour une recherche sur un LDAP<br>
 * 
 * Le requêtage sur une ldap ne fait que sur les entités de type T
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 * 
 */
public interface LdapCriteria extends Criteria {
	/**
	 * 
	 * @return un filter LDAP à appliquer à la recherche
	 */
	public Filter filter();
}
