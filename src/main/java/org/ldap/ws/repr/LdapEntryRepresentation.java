package org.ldap.ws.repr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.ldap.Rest2LdapConfig;
import org.ldap.Rest2LdapConfig.AttributeMappingConfiguration;
import org.ldap.Rest2LdapConfig.RepresentationConfiguration;
import org.ldap.Rest2LdapConfig.ResourceConfiguration;
import org.ldap.beans.LdapEntry;
import org.ldap.utils.PathConverter;


/**
 * 
 * Représentation d'une entrée du LDAP
 * 
 * @author Loic Gangloff: loic.gangloff@atos.net
 */
@XmlRootElement(name = "ldap-entry")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class LdapEntryRepresentation {

	private String id;
	private Map<String, Object> attributes;
	
	private Rest2LdapConfig config;
	private ResourceConfiguration resource;
	private RepresentationConfiguration representation;

	public LdapEntryRepresentation(){
		this(null,null,null);
	}
	
	public LdapEntryRepresentation(Rest2LdapConfig config,
			ResourceConfiguration resource, RepresentationConfiguration representation) {
		this.config = config;
		this.resource = resource;
		this.representation = representation;
	}

	public void buildRepresentation(LdapEntry entry) {

		this.id = resource.getPathConverter().getUriFromDn(entry.getDn());
		
		if (representation.isIncludeAllAttributes()){

			Map<String, List<Object>> attributesAsMap = entry.getAttributesAsMap();
			this.attributes = new HashMap<String, Object>(attributesAsMap.size());
			
			for (Entry<String, List<Object>> e : attributesAsMap.entrySet()) {
				this.attributes.put(e.getKey(), e.getValue());
			}
			
		}
		else{
			this.attributes = new HashMap<String, Object>(representation.getAttributes().size());
			for (AttributeMappingConfiguration attr : representation.getAttributes()) {
					
				if (StringUtils.equals(attr.getType(), "resource")){
					
					ResourceConfiguration res = config.getResourceConfig(attr.getResourceName());
					PathConverter pathConverter = res.getPathConverter();
					
					if (attr.isMultiple()){
						List<Object> attributeValues = entry.getAttributeValues(attr.getLdapName());
						List<String> attributeUriValue = new ArrayList<String>(attributeValues.size());
						
						for (Object dn : attributeValues) {
							attributeUriValue.add(pathConverter.getUriFromDn(dn.toString()));
						}
						
						attributes.put(attr.getName(), attributeUriValue);
					}
					else{
						String dn = entry.getAttributeValueAsString(attr.getLdapName());
						attributes.put(attr.getName(), pathConverter.getUriFromDn(dn) );
					}
					
				}
				else{
					if (attr.isMultiple()){
						attributes.put(attr.getName(), entry.getAttributeValues(attr.getLdapName()));
					}
					else{
						attributes.put(attr.getName(), entry.getAttributeValueAsString(attr.getLdapName()));
					}
				}
			}		
		}
	}

	@XmlElement
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public void setResource(ResourceConfiguration resource) {
		this.resource = resource;
	}

	public void setRepresentation(RepresentationConfiguration representation) {
		this.representation = representation;
	}

	public void setConfig(Rest2LdapConfig config) {
		this.config = config;
	}
	
}
