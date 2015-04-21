package org.ldap.utils;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;

/**
 * Classe permettant de convertir l'identifiant d'une ressource REST vers un chemin LDAP et inversement
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 */
public class PathConverter {

	private static final String URI_PATH_SEPRATOR = "/";

	private String resourceName;
	private String base;
	private String ldapKey;

	
	public PathConverter(String resourceName, String base, String ldapKey) {
		this.resourceName = resourceName;
		this.base = base;
		this.ldapKey = ldapKey;
	}


	public String getUriFromDn(String dn) {
		DistinguishedName distinguishedName = new DistinguishedName(dn);
		return getUriFromDn(distinguishedName);
	}
	public String getUriFromDn(DistinguishedName dn) {
		DistinguishedName distinguishedName = new DistinguishedName(dn);
		distinguishedName.removeFirst(new DistinguishedName(base));

		@SuppressWarnings("unchecked")
		List<LdapRdn> names = distinguishedName.getNames();

		StringBuilder sb = new StringBuilder();
		for (LdapRdn ldapRdn : names) {
			sb.append(URI_PATH_SEPRATOR).append(ldapRdn.getValue());
		}
		if (sb.length() > 0)
			sb.insert(0, resourceName);
		
		return sb.toString();
	}

	public DistinguishedName getDnFromUri(String uri) {
		DistinguishedName distinguishedName = new DistinguishedName(base);

		if (StringUtils.isNotBlank(uri)){
			String uriCleaned = StringUtils.remove(uri, resourceName+URI_PATH_SEPRATOR);
			if(StringUtils.startsWith(uriCleaned, URI_PATH_SEPRATOR)){
				uriCleaned = StringUtils.removeStart(uriCleaned, URI_PATH_SEPRATOR);
			}
			
			String[] paths = uriCleaned.split(URI_PATH_SEPRATOR);
			
			for (int i = 0; i < paths.length; i++) {
				String path = paths[i];
				distinguishedName.add(ldapKey, path);
			}

		}
		return distinguishedName;
	}
}
