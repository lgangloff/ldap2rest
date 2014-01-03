package org.ldap.utils;

import java.util.List;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ldap.NoSuchAttributeException;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Classe utilitaire faciltant les modifications sur un attribut LDAP
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 *
 */
public class LdapModificationUtils {


	private static Logger log = Logger.getLogger(LdapModificationUtils.class);
	
	/**
	 * Ajoute une valeur <code>value<code> à un attribut <code>attributeName</code> sur l'entrée LDAP <code>dn</code>
	 * 
	 * @param ldapTemplate
	 * @param dn
	 * @param attributeName
	 * @param value
	 */
	public static void addAttributeValue(LdapTemplate ldapTemplate, String dn, String attributeName, String value) {
		ModificationItem item = new ModificationItem(
				DirContext.ADD_ATTRIBUTE, 
				new BasicAttribute(attributeName, value));				
		
		log.info("DN["+ dn + "] MODIF[" + item+"]");
		ldapTemplate.modifyAttributes(dn, new ModificationItem[]{item});
	}

	public static void removeAttributeValue(LdapTemplate ldapTemplate, String dn, String attributeName, String value) {
		ModificationItem item = new ModificationItem(
				DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute(attributeName, value));		
		
		log.info("DN["+ dn + "] MODIF[" + item+"]");
		try {
			ldapTemplate.modifyAttributes(dn, new ModificationItem[]{item});
		} catch (NoSuchAttributeException e) {
			log.warn(e.getMessage());
		}
	}
	

	/**
	 * Ajoute un ModificationItem à la liste <code>modificationItems</code>
	 * Si la valeur <code>value</code> est null
	 * 	alors aucune modification n'est appliquée
	 * Si la valeur est vide
	 * 	alors une suppression est appliqué
	 * Sinon
	 * 	une modification est appliqué
	 * 
	 * @param modificationItems
	 * @param attributeName
	 * @param value
	 */
	public static void addMoficicationItemRemoveIfBlankElseReplace(List<ModificationItem> modificationItems, String attributeName, String value, String previousValue){
		if (value != null){
			if (StringUtils.isBlank(value)){
				if (StringUtils.isNotBlank(previousValue)){
					modificationItems.add(new ModificationItem(
								DirContext.REMOVE_ATTRIBUTE, 
								new BasicAttribute(attributeName)));
				}
				//Sinon, on ne fait rien: on veut remplacer du vide pas du vide...
				
			}
			else{
				modificationItems.add(new ModificationItem(
						DirContext.REPLACE_ATTRIBUTE, 
						new BasicAttribute(attributeName, value)));
			}
		}
	}

	/**
	 * Idem que {@link addMoficicationItemRemoveIfBlankElseReplace}
	 * 
	 * Au lieu de se base sur une chaine vide, on se base sur la taille du tableau
	 * 
	 * @param modificationItems
	 * @param attributeName
	 * @param value
	 */
	public static void addMoficicationItemRemoveIfBlankElseReplace(List<ModificationItem> modificationItems, String attributeName, byte[] value) {
		if (value != null){
			if (value.length == 0){
				modificationItems.add(new ModificationItem(
							DirContext.REMOVE_ATTRIBUTE, 
							new BasicAttribute(attributeName)));
				
			}
			else{
				modificationItems.add(new ModificationItem(
						DirContext.REPLACE_ATTRIBUTE, 
						new BasicAttribute(attributeName, value)));
			}
		}
	}
}
