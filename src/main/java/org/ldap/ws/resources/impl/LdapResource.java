package org.ldap.ws.resources.impl;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ldap.Rest2LdapConfig;
import org.ldap.Rest2LdapConfig.RepresentationConfiguration;
import org.ldap.Rest2LdapConfig.ResourceConfiguration;
import org.ldap.beans.LdapEntry;
import org.ldap.core.ILdapDAO;
import org.ldap.criteria.LdapCriteria;
import org.ldap.utils.LdapEntryRepresentationRulesChecker;
import org.ldap.utils.PaginateList;
import org.ldap.ws.repr.LdapEntryRepresentation;
import org.ldap.ws.repr.SearchResultRepresentation;
import org.ldap.ws.resources.ILdapResource;
import org.springframework.ldap.core.DistinguishedName;

/**
 * 
 * @author loic.gangloff@gmail.com
 *
 */
public class LdapResource implements ILdapResource {
	
	private static final Logger log = Logger.getLogger(LdapResource.class);

	@Inject
	private Rest2LdapConfig config;
	
	@Inject
	private ILdapDAO ldapDAO;

	@Context
	private SecurityContext securityContext;

	@QueryParam("pageSize")
	@DefaultValue("10")
	private int pageSize;	

	@QueryParam("startPage")
	@DefaultValue("1")
	private int startPage;
	
	@Context
	private UriInfo uriInfo;

	@Override
	public Response getLdapResource(String path, String view){


		String[] segments = StringUtils.splitPreserveAllTokens(path, "/");
		String resourceName = segments[0];
		String realpath = StringUtils.removeStart(path, resourceName);
		realpath = StringUtils.removeStart(realpath, "/");
		ResourceConfiguration resource = config.getResourceConfig(resourceName);
		
		if (resource == null){
			return Response.noContent().build();
		}
		
		RepresentationConfiguration repr = resource.getRepresentation(view);
		
		
		Response response;
		//On liste toutes les ressources
		if (StringUtils.isEmpty(realpath)){
			
			log.debug("GET /"+path+" mapped as ressource '"+resourceName+"' and searching all");
			
			response = findAllRessource(resource, repr);
		}
		//On liste les sous ressources
		else if (StringUtils.equals(segments[segments.length-1],"*") && resource.isAsSubResource()){
			realpath = StringUtils.removeEnd(realpath, "*");

			log.debug("GET /"+path+" mapped as ressource '"+resourceName+"' and searching in subchild of '"+realpath+"'");
			
			response = findAllRessource(resource, repr, realpath);
		}
		//On veut le détail d'une ressource
		else{

			log.debug("GET /"+path+" mapped as ressource '"+resourceName+"' and getting '"+realpath+"'");
			
			response = getResource(resource, repr, realpath);
		}
		

		log.debug("GET /"+path+" - Response: "+response);
		
		return response;
	}
	
	
	@Override
	public Response updateLdapResource(String path, String view, LdapEntryRepresentation submitRepresentation){

		
		String[] segments = StringUtils.splitPreserveAllTokens(path, "/");
		String resourceName = segments[0];
		String realpath = StringUtils.removeStart(path, resourceName);
		realpath = StringUtils.removeStart(realpath, "/");
		ResourceConfiguration resource = config.getResourceConfig(resourceName);

		submitRepresentation.setId(path);
		
		log.debug("PUT /"+path+" mapped as ressource '"+resourceName+"' on view " + view + " and content " + submitRepresentation);
		
		if (resource == null){
			return Response.noContent().build();
		}
		
		RepresentationConfiguration repr = resource.getRepresentation(view);
		
		LdapEntryRepresentation cleanForUpdate = LdapEntryRepresentationRulesChecker.cleanForUpdate(submitRepresentation, config, resource, repr, null);
				
		LdapEntry updatedEntry = ldapDAO.updateLdapEntry(cleanForUpdate.parse());
		
		cleanForUpdate.format(updatedEntry);
		
		return Response.ok(cleanForUpdate).build();
	}



	private Response getResource(ResourceConfiguration resource, RepresentationConfiguration repr, String path){

		
		DistinguishedName dn = resource.getDnFromUri(path);
		LdapEntry e = ldapDAO.getLdapEntry(dn);
		LdapEntryRepresentation eRepr = new LdapEntryRepresentation(config, resource, repr);
		eRepr.format(e);
		
		Response response = Response.ok(eRepr).build();		
	
		return response;
	}
	
	private Response findAllRessource(final ResourceConfiguration resource, final RepresentationConfiguration repr){
		return findAllRessource(resource, repr, "");
	}
	
	private Response findAllRessource(final ResourceConfiguration resource, final RepresentationConfiguration repr, String uri){

		DistinguishedName dn = new DistinguishedName(resource.getDnFromUri(uri));
		LdapCriteria criteria = resource.getCriteria(uriInfo.getQueryParameters(true));
		
		List<LdapEntry> entries = ldapDAO.findAllLdapEntry(dn, criteria);
		
		
		PaginateList<LdapEntry> entriesPaginate = new PaginateList<LdapEntry>(startPage, pageSize);
		entriesPaginate.setResults(entries);
		entriesPaginate.paginate();
		
		SearchResultRepresentation search = new SearchResultRepresentation(entriesPaginate);
		search.setConfig(config);
		search.setResource(resource);
		search.setRepresentation(repr);
		search.buildLink(uriInfo);
		
		return Response.ok(search).build();
	}
}
