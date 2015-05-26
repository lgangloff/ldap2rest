package org.ldap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ldap.core.rules.AttributeRules;
import org.ldap.core.rules.FormatRules;
import org.ldap.core.rules.MaxLengthRules;
import org.ldap.core.rules.MultipleRules;
import org.ldap.core.rules.ReadOnlyRules;
import org.ldap.core.rules.RequiredRules;
import org.ldap.core.rules.TypeRules;
import org.ldap.criteria.LdapCriteria;
import org.ldap.utils.PathConverter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.BinaryLogicalFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;

@XmlRootElement(name="rest")
public class Rest2LdapConfig {
	


	public enum AttributeMappingType {
		@XmlEnumValue("string")
		STRING, 
		@XmlEnumValue("enum")
		ENUM, 
		@XmlEnumValue("integer")
		INTEGER, 
		@XmlEnumValue("file")
		FILE, 
		@XmlEnumValue("resource")
		RESOURCE;
	}

	private static final Logger log = Logger.getLogger(Rest2LdapConfig.class);
	
	@XmlAttribute(name="ldapbase", required=true)
	private String ldapBase;

	@XmlElement(name="resource", required=true)
	private List<ResourceConfiguration> resources;

	

	@Override
	public String toString() {
		return "Configuration [ldapBase=" + ldapBase + ", resources="
				+ resources + "]";
	}


	@XmlRootElement(name="resource")
	public static class ResourceConfiguration {
		@XmlAttribute(name="name", required=true)
		private String name;
		
		@XmlAttribute(name="ldapbase", required=true)
		private String ldapBase;
		
		@XmlAttribute(name="ldapkey", required=true)
		private String ldapKey;
		
		@XmlAttribute(name="subresource")
		private boolean asSubResource = false;
		
		@XmlElement(name="representation")
		private List<RepresentationConfiguration> representations;

		@XmlElement(name="query")
		private QueryConfiguration query;

		protected Rest2LdapConfig config;

		
		public boolean isAsSubResource() {
			return asSubResource;
		}

		public PathConverter getPathConverter(){
			PathConverter pathConverter = new PathConverter(this.name, this.ldapBase, ldapKey);
			return pathConverter;
		}

		public RepresentationConfiguration getRepresentation(String view){
			for (RepresentationConfiguration r : representations) {
				if (StringUtils.equalsIgnoreCase(r.name, view)){
					return r;
				}
			}
			return null;
		}
		
		public List<RepresentationConfiguration> getRepresentations() {
			return new ArrayList<RepresentationConfiguration>(representations);
		}

		public DistinguishedName getDnFromUri(String uri){
			PathConverter pathConverter = this.getPathConverter();
			return pathConverter.getDnFromUri(uri);
		}
		
		public void setParentLdapBase(String parentLdapBase) {
			if (StringUtils.isBlank(this.ldapBase)){
				this.ldapBase = parentLdapBase;
			}
			else{
				this.ldapBase = this.ldapBase + "," + parentLdapBase;			
			}
		}

		@Override
		public String toString() {
			return "ResourceConfiguration [name=" + name + ", ldapBase="
					+ ldapBase + ", ldapKey=" + ldapKey + ", asSubResource="
					+ asSubResource + ", representations=" + representations
					+ ", query=" + query + "]";
		}

		public LdapCriteria getCriteria(final MultivaluedMap<String, String> allQueryParameters) {
		
			return new LdapCriteria() {
				
				@Override
				public String encode() {
					return filter().encode();
				}
				
				@Override
				public Filter filter() {
					AndFilter and = new AndFilter();
					if (query != null){
						for (QueryParamConfiguration queryParam : query.params) {
							if (allQueryParameters.containsKey(queryParam.name)){
								BinaryLogicalFilter subFilter = null;
								if (StringUtils.equals(queryParam.operand, "OR")){
									subFilter = new OrFilter();
								}
								else{
									subFilter = new AndFilter();
								}
								
								for (QueryParamAttributeConfiguration queryParamAttribute : queryParam.attributes) {
									Filter f = null;
									String value = allQueryParameters.getFirst(queryParam.name);
									
									if (StringUtils.equals(queryParamAttribute.type, "resource")){
										ResourceConfiguration res = ResourceConfiguration.this.config.getResourceConfig(queryParamAttribute.resource);
										PathConverter pathConverter = res.getPathConverter();
										value = pathConverter.getDnFromUri(value).toString();
										/*
										try {
											value = pathConverter.getDnFromUri(value).toString();
										} catch (InvalidNameException e) {
											value = null;
											log.warn("La valeur '"+value+"' du paramètre '"+queryParam.name+"' ne peut être transformé en resource '"+queryParamAttribute.resource+"': "+e.getMessage(), e);
										}*/
									}
									if (value != null){
										if (StringUtils.equals(queryParamAttribute.filter, "WhitespaceWildcardsFilter")){
											f = new WhitespaceWildcardsFilter(queryParamAttribute.ldapName, value);
										}
										else if (StringUtils.equals(queryParamAttribute.filter, "EqualsFilter")){
											f = new EqualsFilter(queryParamAttribute.ldapName, value);
										}
										else if (StringUtils.equals(queryParamAttribute.filter, "LikeFilter")){
											f = new LikeFilter(queryParamAttribute.ldapName, value);
										}
										if (f != null){
											subFilter.append(f);
										}
									}
								}							
								and.and(subFilter);
							}						
						}
					}
					return and;
				}
			};
		}		
	}

	@XmlRootElement(name="representation")
	public static class RepresentationConfiguration {
		@XmlAttribute(name="name")
		private String name;

		@XmlAttribute(name="extends")
		private String extendsName;

		@XmlAttribute(name="allattributes")
		private boolean includeAllAttributes = false;

		@XmlAttribute(name="updatable")
		private boolean updatable = false;

		@XmlElement(name="attribute")
		private List<AttributeMappingConfiguration> attributes = new ArrayList<Rest2LdapConfig.AttributeMappingConfiguration>();
		
		@Override
		public String toString() {
			return "RepresentationConfiguration [name=" + name
					+ ", extendsName=" + extendsName + ", attributes="
					+ attributes + "]";
		}

		public List<AttributeMappingConfiguration> getAttributes() {
			return attributes;
		}

		public boolean isIncludeAllAttributes() {
			return includeAllAttributes;
		}
		
		public boolean isUpdatable() {
			return updatable;
		}

		public void setExtendsAttributes(
				List<AttributeMappingConfiguration> extendsAttributes) {
			attributes.addAll(extendsAttributes);
		}

		public String getName() {
			return name;
		}

		public List<AttributeMappingConfiguration> getAttributeResourceReference() {
			List<AttributeMappingConfiguration> result = new ArrayList<Rest2LdapConfig.AttributeMappingConfiguration>();
			for (AttributeMappingConfiguration attr : attributes) {
				if (attr.isResource())
					result.add(attr);
			}
			return result;
		}
		
		
	}
	
	

	@XmlRootElement(name="attribute")
	public static class AttributeMappingConfiguration {

		@XmlAttribute(name="ldapname")
		private String ldapName;
		

		@XmlAttribute(name="name")
		private String name;
		
		@XmlAttribute(name="type")
		private AttributeMappingType type = AttributeMappingType.STRING;
		
		@XmlAttribute(name="resource")
		private String resourceName;

		@XmlAttribute(name="view")
		private String view;

		@XmlAttribute(name="defaultValue")
		private String defaultValue;
		
		//Rules
		
		@XmlAttribute(name="multiple")
		private boolean multiple = false;

		@XmlAttribute(name="contentType")
		private String contentType;

		@XmlAttribute(name="readonly")
		private boolean readonly = false;

		@XmlAttribute(name="required")
		private boolean required = false;

		@XmlAttribute(name="values")
		private String values;

		@XmlAttribute(name="maxlength")
		private Integer maxlength;

		@XmlAttribute(name="format")
		private String format;
		
		private List<AttributeRules> rules;


		private ResourceConfiguration resourceReference;
		private RepresentationConfiguration representationReference;
		
		protected AttributeMappingConfiguration() {
			
		}
		protected AttributeMappingConfiguration(String ldapName, String name,
				AttributeMappingType type, boolean multiple, String contentType) {
			super();
			this.ldapName = ldapName;
			this.name = name;
			this.type = type;
			this.multiple = multiple;
			this.contentType = contentType;
		}
		@Override
		public String toString() {
			return "AttributeMappingConfiguration [ldapName=" + ldapName
					+ ", name=" + name + ", type=" + type + ", resource=" + resourceName + ", multiple="
							+ multiple + ", contentType=" + contentType + "]";
		}
		public String getLdapName() {
			return ldapName;
		}
		public String getName() {
			return name;
		}
		public String getDefaultValue() {
			return defaultValue;
		}
		public String getContentType() {
			return contentType;
		}
		public boolean isMultiple() {
			return multiple;
		}
		
		public ResourceConfiguration getResourceReference() {
			return resourceReference;
		}
		public RepresentationConfiguration getRepresentationReference() {
			return representationReference;
		}
		public void buildAttributeRules() {
			rules = new ArrayList<AttributeRules>();
			if (Objects.nonNull(format))rules.add(new FormatRules(format));
			if (Objects.nonNull(maxlength))rules.add(new MaxLengthRules(maxlength));
			if (multiple) rules.add(new MultipleRules());
			if (readonly) rules.add(new ReadOnlyRules());
			if (required) rules.add(new RequiredRules());
			rules.add(new TypeRules(type, values, contentType));
		}
		
		public List<AttributeRules> getRules() {
			return rules;
		}
		
		public boolean isResource(){
			return this.type == AttributeMappingType.RESOURCE;
		}
		
		public boolean isFile(){
			return this.type == AttributeMappingType.FILE;
		}
		public boolean hasRuleOf(Class<? extends AttributeRules> clazz) {
			return getRuleOf(clazz) != null;
		}
		public AttributeRules getRuleOf(Class<? extends AttributeRules> clazz) {
			for (AttributeRules rule : rules) {
				if (clazz.isInstance(rule)){
					return rule;
				}
			}
			return null;
		}
	}
	


	@XmlRootElement(name="query")
	public static class QueryConfiguration {
		
		@XmlElement(name="param")
		private List<QueryParamConfiguration> params;

		@Override
		public String toString() {
			return "QueryConfiguration [params=" + params + "]";
		}
		
		
	}


	@XmlRootElement(name="param")
	public static class QueryParamConfiguration {

		@XmlAttribute(name="name")
		private String name;
		
		@XmlAttribute(name="operand")
		private String operand;

		@XmlElement(name="attribute")
		private List<QueryParamAttributeConfiguration> attributes;

		@Override
		public String toString() {
			return "QueryParamConfiguration [name=" + name + ", operand="
					+ operand + ", attributes=" + attributes + "]";
		}
		
		
	}

	@XmlRootElement(name="attribute")
	public static class QueryParamAttributeConfiguration {

		@XmlAttribute(name="ldapname")
		private String ldapName;
		
		@XmlAttribute(name="filter")
		private String filter;
		
		@XmlAttribute(name="type")
		private String type;
		
		@XmlAttribute(name="resource")
		private String resource;

		@Override
		public String toString() {
			return "QueryParamAttributeConfiguration [ldapName=" + ldapName
					+ ", filter=" + filter + ", type=" + type + ", resource="
					+ resource + "]";
		}
	}
	
	public ResourceConfiguration getResourceConfig(String resourceName) {
		if (this.resources == null)
			return null;
		for (ResourceConfiguration r : this.resources) {
			if (StringUtils.equalsIgnoreCase(resourceName, r.name))
				return r;
		}
		return null;
	}


	private void resolveExtendsAndDependencies() {
		if (this.resources == null)
			return;
		for (ResourceConfiguration resource : this.resources) {
			resource.setParentLdapBase(this.ldapBase);
			resource.config = this;
			for (RepresentationConfiguration representation : resource.representations) {
				buildAttributesExtends(resource, representation);
				buildAttributesRules(resource, representation);
				buildAttributesReference(resource, representation);
			}
		}
	}


	private void buildAttributesExtends(ResourceConfiguration resource, RepresentationConfiguration representation) {
		if (StringUtils.isNotBlank(representation.extendsName)){
			RepresentationConfiguration extendsRepresentation = resource.getRepresentation(representation.extendsName);
			representation.setExtendsAttributes(extendsRepresentation.attributes);
		}
	}	
	
	private void buildAttributesRules(ResourceConfiguration resource, RepresentationConfiguration representation) {
		for (AttributeMappingConfiguration attributeMapping : representation.attributes) {
			attributeMapping.buildAttributeRules();
		}
	}
	
	private void buildAttributesReference(ResourceConfiguration resource, RepresentationConfiguration representation) {
		for (AttributeMappingConfiguration attributeMapping : representation.attributes) {
			if (attributeMapping.type == AttributeMappingType.RESOURCE){
				ResourceConfiguration resourceReference = resource.config.getResourceConfig(attributeMapping.resourceName);
				if (resourceReference == null){
					log.warn("Unable to find the resource referenced by " + attributeMapping);
					break;
				}
				
				RepresentationConfiguration representationReference = attributeMapping.view == null ? null : resourceReference.getRepresentation(attributeMapping.view);
				if (attributeMapping.view != null && representationReference == null){
					log.warn("Unable to find the view referenced by " + attributeMapping);
					break;
				}
				
				attributeMapping.resourceReference = resourceReference;
				attributeMapping.representationReference = representationReference;
			}
		}
	}

	public static Rest2LdapConfig getInstance(String configFileName) throws JAXBException {

		log.info("Loading config from "+configFileName);
		InputStream is = Rest2LdapConfig.class.getClassLoader().getResourceAsStream(configFileName); 
		
		Rest2LdapConfig c;
		try {
			JAXBContext jc = JAXBContext.newInstance(Rest2LdapConfig.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			c = (Rest2LdapConfig) unmarshaller.unmarshal(is);
		} catch (JAXBException e) {
			log.error("Error when loadind config "+configFileName+": "+e.getMessage(), e);
			throw e;
		}
		
		c.resolveExtendsAndDependencies();

		log.info("Config loaded: "+c.toString());
		
		return c;
	}
}


