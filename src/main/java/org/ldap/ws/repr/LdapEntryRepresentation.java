package org.ldap.ws.repr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private List<LinkRepresentation> links = new ArrayList<LinkRepresentation>();
	private List<String> warnings = new ArrayList<String>();
	
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

	public LdapEntry parse(){
		LdapEntry ldapEntry = new LdapEntry(resource.getPathConverter().getDnFromUri(this.id));
		
		
		for (AttributeMappingConfiguration attr : representation.getAttributes()) {
			
			if (attributes.containsKey(attr.getName())){

				if (attr.isResource()){
					
					PathConverter pathConverter = attr.getResourceReference().getPathConverter();
					
					if (attr.isMultiple()){
						List<Object> attributeValues = (List<Object>) attributes.get(attr.getLdapName());
						List<String> attributeDnValue = new ArrayList<String>(attributeValues.size());
						
						for (Object uri : attributeValues) {
							attributeDnValue.add(pathConverter.getDnFromUri(uri.toString()).toString());
						}
						
						ldapEntry.putAttribute(attr.getLdapName(), attributeDnValue);
					}
					else{
						String uri = (String) attributes.get(attr.getLdapName());
						ldapEntry.putAttribute(attr.getLdapName(), pathConverter.getDnFromUri(uri).toString());
					}
					
				}
				else if (attr.isFile()){
					//TODO ?
				}
				else{
					ldapEntry.putAttribute(attr.getLdapName(), attributes.get(attr.getName()));
				}
				
			}
			
		}	
		
		
		return ldapEntry;
	}
	public void format(LdapEntry entry) {

		this.id = resource.getPathConverter().getUriFromDn(entry.getDn());
		this.links = new ArrayList<LinkRepresentation>();
		
		this.links.add(new LinkRepresentation("this", this.id, MediaType.APPLICATION_JSON));
		for (RepresentationConfiguration r : resource.getRepresentations()) {
			this.links.add(new LinkRepresentation("this/"+r.getName(), this.id + "?view=" + r.getName(), MediaType.APPLICATION_JSON));
		}
		
		if (representation == null){
			return;
		}
		
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
					
				if (attr.isResource()){
										
					List<LdapEntry> attributeValues = entry.getAttributeValuesAsLdapEntry(attr.getLdapName());
					List<LdapEntryRepresentation> attributeValue = new ArrayList<LdapEntryRepresentation>(attributeValues.size());
					
					for (int i = 0; i < attributeValues.size(); i++) {
						LdapEntry referenceEntry = attributeValues.get(i);

						LdapEntryRepresentation referenceRepresentation = 
								new LdapEntryRepresentation(config, attr.getResourceReference(), attr.getRepresentationReference());
						referenceRepresentation.format(referenceEntry);
						
						attributeValue.add(referenceRepresentation);
						
						
						
						if (!attr.isMultiple())
							break;
					}
					
					if (attr.isMultiple())
						attributes.put(attr.getName(), attributeValue);
					else{
						attributes.put(attr.getName(), attributeValue.isEmpty() ? null : attributeValue.get(0) );
					}
					
				}
				else if (attr.isFile()){
					this.links.add(new LinkRepresentation("this/" + attr.getName(), this.id + "/" + attr.getName(), attr.getContentType()));
				}
				else{
					List<Object> attributeValues = entry.getAttributeValuesAsObject(attr.getLdapName());

					if (attr.isMultiple())
						attributes.put(attr.getName(), attributeValues);
					else{
						attributes.put(attr.getName(), attributeValues.isEmpty() ? null : attributeValues.get(0) );
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

	@XmlElement
	public List<LinkRepresentation> getLinks() {
		return links;
	}

	public void setLinks(List<LinkRepresentation> links) {
		this.links = links;
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

	@XmlElement
	public List<String> getWarnings() {
		return warnings == null || warnings.isEmpty() ? null : warnings;
	}

	public void addWarningMessage(String message) {
		this.warnings.add(message);
	}

	@Override
	public String toString() {
		return "LdapEntryRepresentation [id=" + id + ", attributes="
				+ attributes + "]";
	}	
}
