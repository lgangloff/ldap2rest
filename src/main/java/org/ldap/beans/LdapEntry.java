package org.ldap.beans;

import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
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

	private static final Logger log = Logger.getLogger(LdapEntry.class);
	
	private DistinguishedName dn;

	private Map<String, List<Object>> attrsObjectAsMap;
	private Map<String, List<LdapEntry>> attrsLdapEntryAsMap;
	
	public LdapEntry(){	}
	
	public LdapEntry(Name name){
		this(name, null);
	}
	
	public LdapEntry(Name name, Attributes attributes) {
		super();
		this.dn = new DistinguishedName(name);
		this.attrsObjectAsMap = convertAttributesToMap(attributes);
		this.attrsLdapEntryAsMap = new HashMap<String, List<LdapEntry>>();
	}

	public DistinguishedName getDn() {
		return dn;
	}

	@Override
	public String toString() {
		return "LdapEntry [dn=" + dn + ", attributes=" + attrsObjectAsMap + "]";
	}
	
	@XmlElement(name="id")
	public String getId(){
		return getDn().toString();
	}
	
	@XmlElement(name="attributes")
	public Map<String, List<Object>> getAttributesAsMap(){
		return attrsObjectAsMap;
	}
	
	public List<LdapEntry> getAttributeValuesAsLdapEntry(String ldapName) {
		
		if (attrsLdapEntryAsMap.containsKey(ldapName))
			return attrsLdapEntryAsMap.get(ldapName);
		
		if (!attrsObjectAsMap.containsKey(ldapName)){
			attrsLdapEntryAsMap.put(ldapName, Collections.<LdapEntry>emptyList());
			return attrsLdapEntryAsMap.get(ldapName);
		}

		List<Object> originalList = attrsObjectAsMap.get(ldapName);
		List<LdapEntry> results = new ArrayList<LdapEntry>(originalList.size());
		for (Object dn : originalList) {
			results.add(new LdapEntry(new DistinguishedName(dn.toString())));
		}
		attrsLdapEntryAsMap.put(ldapName, results);
		return attrsLdapEntryAsMap.get(ldapName);
	}


	public List<Object> getAttributeValuesAsObject(String ldapName) {
		return attrsObjectAsMap.getOrDefault(ldapName, Collections.emptyList());
	}
	
	
	private Map<String, List<Object>> convertAttributesToMap(Attributes attributes){

		Map<String, List<Object>> attrsAsMap = new HashMap<String, List<Object>>();
		
		
		if (attributes == null){
			return attrsAsMap;
		}
		
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
				log.warn("Problem when reading attribute value", e);
			}
						
			attrsAsMap.put(attr.getID(), values);
		}
		return attrsAsMap;
	}

	public void putAttribute(String name, Object value) {
		if (value instanceof List) {
			attrsObjectAsMap.put(name, (List<Object>) value);
		} 
		else {
			attrsObjectAsMap.put(name, Arrays.asList(value));
		}
	}

	public void mergeAttributes(LdapEntry entry) {
		attrsObjectAsMap.putAll(entry.attrsObjectAsMap);
	}

}