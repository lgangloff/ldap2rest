package org.ldap.ws.resources.impl;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ldap.Rest2LdapConfig;
import org.ldap.Rest2LdapConfig.AttributeMappingConfiguration;
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
		
		if (resource == null || StringUtils.isEmpty(realpath) || submitRepresentation == null){
			throw new BadRequestException();
		}
		
		submitRepresentation.setId(path);
		
		log.debug("PUT /"+path+" mapped as ressource '"+resourceName+"' on view " + view + " and content " + submitRepresentation);
				
		RepresentationConfiguration repr = resource.getRepresentation(view);
		
		if (repr == null)
			throw new BadRequestException();
		
		if (!repr.isUpdatable())
			throw new ForbiddenException();
		
		
		LdapEntryRepresentation cleanForUpdate = LdapEntryRepresentationRulesChecker.cleanForUpdate(submitRepresentation, config, resource, repr, null);
				
		LdapEntry updatedEntry = ldapDAO.updateLdapEntry(cleanForUpdate.parse());
		loadReference(repr, updatedEntry);
		
		cleanForUpdate.format(updatedEntry);
		
		return Response.ok(cleanForUpdate).build();
	}

	@Override
	public Response createLdapResource(String path, String view, LdapEntryRepresentation submitRepresentation) {

		String[] segments = StringUtils.splitPreserveAllTokens(path, "/");
		String resourceName = segments[0];
		String realpath = StringUtils.removeStart(path, resourceName);
		realpath = StringUtils.removeStart(realpath, "/");
		ResourceConfiguration resource = config.getResourceConfig(resourceName);

		if (resource == null){
			return Response.status(Status.BAD_REQUEST).entity("Unable to identify the ressource '" + resourceName+"'").build();
		}
		if (StringUtils.isNotEmpty(realpath)){
			return Response.status(Status.BAD_REQUEST).entity("In order to create a resource of type '"+resourceName+"', POST a request on '/"+resourceName +"' not on '/"+path+"'").build();
		}
		if (StringUtils.isBlank(submitRepresentation.getId())){
			return Response.status(Status.BAD_REQUEST).entity("An ID must be provided").build();
		}
		
		log.debug("POST /"+path+" mapped as ressource '"+resourceName+"' on view " + view + " and content " + submitRepresentation);
				
		RepresentationConfiguration repr = resource.getRepresentation(view);
		
		LdapEntryRepresentation cleanForCreate = LdapEntryRepresentationRulesChecker.cleanForCreate(submitRepresentation, config, resource, repr, null);

		if (cleanForCreate.getWarnings() != null){
			return Response.status(Status.BAD_REQUEST).entity(cleanForCreate).build();
		}

		log.debug("Content after cleaning up " + cleanForCreate);
		
		LdapEntry createdEntry = ldapDAO.createLdapEntry(cleanForCreate.parse());
		loadReference(repr, createdEntry);
		
		cleanForCreate.format(createdEntry);
		
		URI createdUri = UriBuilder.fromPath(resource.getPathConverter().getUriFromDn(createdEntry.getDn())).build();
		return Response.created(createdUri).build();
	}


	private Response getResource(ResourceConfiguration resource, RepresentationConfiguration repr, String path){

		
		DistinguishedName dn = resource.getDnFromUri(path);
		LdapEntry e = ldapDAO.getLdapEntry(dn);
		
		loadReference(repr, e);
		
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
		
		loadReference(repr, entriesPaginate);
		
		SearchResultRepresentation search = new SearchResultRepresentation(entriesPaginate);
		search.setConfig(config);
		search.setResource(resource);
		search.setRepresentation(repr);
		search.buildLink(uriInfo);
		
		return Response.ok(search).build();
	}
	

	private void loadReference(final RepresentationConfiguration repr, Iterable<LdapEntry> entries){
		for (LdapEntry entry : entries) {
			loadReference(repr, entry);
		}
	}
	
	private void loadReference(final RepresentationConfiguration repr, LdapEntry entry){
		
		List<AttributeMappingConfiguration> attrs = repr.getAttributeResourceReference();
		
		//TODO: Optimiser le chargement des lazy !!!
		for (AttributeMappingConfiguration attr : attrs) {
			List<LdapEntry> lazyReferenceAttributeValues = entry.getAttributeValuesAsLdapEntry(attr.getLdapName());
			for (LdapEntry lazyLdapEntry : lazyReferenceAttributeValues) {
				LdapEntry loadedLdapEntry = ldapDAO.getLdapEntry(lazyLdapEntry.getDn());
				
				// Permettra de charger les references de reference
				// TODO: Attention à la profondeur et gerer les boucle infinie
				//loadReference(attr.getRepresentationReference(), loadedLdapEntry); 
				
				lazyLdapEntry.mergeAttributes(loadedLdapEntry);
			}
		}
		
	}
}
