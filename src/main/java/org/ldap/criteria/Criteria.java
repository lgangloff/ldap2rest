package org.ldap.criteria;

/**
 * Interface de base pour la définition de Critère de recherche.
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 * 
 */
public interface Criteria {
	/**
	 * Pour une critèria SQL, on retournera du SQL.<br>
	 * Pour du LDAP, on retournera un filre LDAP.<br>
	 * Etc...
	 * 
	 * @return Une chaine de caractère représentant les critères encodés
	 */
	String encode();
}
