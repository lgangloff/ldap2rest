package org.ldap.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ldap.Rest2LdapConfig;
import org.ldap.Rest2LdapConfig.AttributeMappingConfiguration;
import org.ldap.Rest2LdapConfig.RepresentationConfiguration;
import org.ldap.Rest2LdapConfig.ResourceConfiguration;
import org.ldap.Rest2LdapContext;
import org.ldap.core.rules.AttributeRules;
import org.ldap.core.rules.ReadOnlyRules;
import org.ldap.core.rules.RequiredRules;
import org.ldap.ws.repr.LdapEntryRepresentation;

public class LdapEntryRepresentationRulesChecker {

	
	
	public static LdapEntryRepresentation cleanForUpdate(LdapEntryRepresentation submitRepresentation, Rest2LdapConfig config, ResourceConfiguration resource, RepresentationConfiguration representationConfig, Rest2LdapContext context){

		LdapEntryRepresentation cleanedRepresentation = new LdapEntryRepresentation(config, resource, representationConfig);
		cleanedRepresentation.setId(submitRepresentation.getId());
		

		for (AttributeMappingConfiguration attribute : representationConfig.getAttributes()) {
			if (submitRepresentation.getAttributes().containsKey(attribute.getName())){
				List<AttributeRules> failedRules = new ArrayList<AttributeRules>();
				
				for (AttributeRules rule : attribute.getRules()) {
					if (!rule.isUpdatable(context, submitRepresentation.getAttributes().get(attribute.getName()))){
						failedRules.add(rule);
					}
				}
				
				if (failedRules.isEmpty()){
					cleanedRepresentation.getAttributes().put(attribute.getName(), submitRepresentation.getAttributes().get(attribute.getName()));
				}
				else{
					for (AttributeRules attributeRules : failedRules) {
						cleanedRepresentation.addWarningMessage(cleanedRepresentation.getId() + " is not updatable because of " + attributeRules.getClass().getSimpleName() + " on attribute " + attribute.getName() + " with value " + cleanedRepresentation.getAttributes().get(attribute.getName()));
						
					}
				}
			}
		}
			
		return cleanedRepresentation;
	}

	public static LdapEntryRepresentation cleanForCreate(LdapEntryRepresentation submitRepresentation, Rest2LdapConfig config, ResourceConfiguration resource, RepresentationConfiguration representationConfig, Rest2LdapContext context){
		
		LdapEntryRepresentation cleaned = cleanForUpdate(submitRepresentation, config, resource, representationConfig, context);

		for (AttributeMappingConfiguration attribute : representationConfig.getAttributes()) {
			if (attribute.hasRuleOf(ReadOnlyRules.class) && attribute.getDefaultValue() != null){
				if (attribute.isMultiple()) {
					List<String> split = new ArrayList<String> (
							Arrays.asList(StringUtils.split(attribute.getDefaultValue(), ",")));
					cleaned.getAttributes().put(attribute.getName(), split);
				} else
					cleaned.getAttributes().put(attribute.getName(), attribute.getDefaultValue());
			}
			
			if (attribute.hasRuleOf(RequiredRules.class)){
				AttributeRules rule = attribute.getRuleOf(RequiredRules.class);
				if (!rule.isUpdatable(context, cleaned.getAttributes().get(attribute.getName()))){
					cleaned.addWarningMessage(cleaned.getId() + " is not creatable because of " + rule.getClass().getSimpleName() + " on attribute " + attribute.getName() + " with value " + cleaned.getAttributes().get(attribute.getName()));
				}
			}
		}
		
		return cleaned;
	}
}
