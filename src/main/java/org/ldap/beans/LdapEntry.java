package org.ldap.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.eclipse.jetty.util.log.Log;
import org.springframework.ldap.core.DistinguishedName;


/**
 * 
 * Entrée d'un annuaire LDAP
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 * 
 */
@XmlRootElement()
@JsonSerialize(include = Inclusion.NON_NULL)
@XmlAccessorType(XmlAccessType.NONE)
public class LdapEntry {

	private DistinguishedName dn;

	private Map<String, List<Object>> attrsAsMap;
	
	public LdapEntry(){	}
	
	public LdapEntry(Name name){
		this(name, null);
	}
	
	public LdapEntry(Name name, Attributes attributes) {
		super();
		this.dn = new DistinguishedName(name);
		this.attrsAsMap = convertAttributesToMap(attributes);
	}

	public DistinguishedName getDn() {
		return dn;
	}

	public void setDn(DistinguishedName dn) {
		this.dn = dn;
	}


	@Override
	public String toString() {
		return "LdapEntry [dn=" + dn + ", attributes=" + attrsAsMap + "]";
	}
	
	@XmlElement(name="id")
	public String getId(){
		return getDn().toString();
	}
	
	@XmlElement(name="attributes")
	public Map<String, List<Object>> getAttributesAsMap(){
		return attrsAsMap;
	}

	public List<Object> getAttributeValues(String ldapName) {
		return attrsAsMap.containsKey(ldapName) ? attrsAsMap.get(ldapName) : Collections.EMPTY_LIST;
	}
	

	public String getAttributeValueAsString(String ldapName) {
		List<Object> values = getAttributeValues(ldapName);
		return values.size() > 0 && values.get(0) != null ?  values.get(0).toString() : "";
	}
	
	
	private Map<String, List<Object>> convertAttributesToMap(Attributes attributes){

		Map<String, List<Object>> attrsAsMap = new HashMap<String, List<Object>>();
		NamingEnumeration<? extends Attribute> attrs = attributes.getAll();
		
		while (attrs.hasMoreElements()) {
			Attribute attr = (Attribute) attrs.nextElement();
			
			List<Object> values = new ArrayList<Object>();
			
			try {
				NamingEnumeration<?> attrValues = attr.getAll();
				
				while (attrValues.hasMoreElements()) {
					Object attrValue = (Object) attrValues.nextElement();
					values.add(attrValue);
				}
			} catch (NamingException e) {
				Log.warn("Problem when reading attribute value", e);
			}
						
			attrsAsMap.put(attr.getID(), values);
		}
		return attrsAsMap;
	}

}