package org.ldap.utils;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;

import org.ldap.Rest2LdapConfig;
import org.ldap.Rest2LdapConfig.AttributeMappingConfiguration;
import org.ldap.Rest2LdapConfig.RepresentationConfiguration;
import org.ldap.Rest2LdapConfig.ResourceConfiguration;
import org.ldap.Rest2LdapContext;
import org.ldap.core.rules.AttributeRules;
import org.ldap.ws.repr.LdapEntryRepresentation;

public class LdapEntryRepresentationRulesChecker {

	
	
	public static LdapEntryRepresentation cleanForUpdate(LdapEntryRepresentation submitRepresentation, Rest2LdapConfig config, ResourceConfiguration resource, RepresentationConfiguration representationConfig, Rest2LdapContext context){
		if (submitRepresentation == null)
			throw new BadRequestException();
		
		if (!representationConfig.isUpdatable())
			throw new ForbiddenException();
		
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
						cleanedRepresentation.addWarningMessage(attribute.getName() + " is not updatable because of " + attributeRules.getClass().getSimpleName());
					}
				}
			}
		}
			
		return cleanedRepresentation;
	}

}
