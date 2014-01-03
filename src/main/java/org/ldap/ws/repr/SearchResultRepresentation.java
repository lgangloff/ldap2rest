package org.ldap.ws.repr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ldap.Rest2LdapConfig;
import org.ldap.Rest2LdapConfig.RepresentationConfiguration;
import org.ldap.Rest2LdapConfig.ResourceConfiguration;
import org.ldap.beans.LdapEntry;
import org.ldap.utils.PaginateList;

/**
 * 
 * Représentation d'une recherche paginée
 * 
 * @author Loic Gangloff: loic.gangloff@atos.net
 */
@XmlRootElement(name = "search-result")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SearchResultRepresentation {


	private PaginateList<LdapEntry> paginateList;
	
	private List<LinkRepresentation> links;
	

	private Rest2LdapConfig config;
	private ResourceConfiguration resource;
	private RepresentationConfiguration representation;

	public SearchResultRepresentation(PaginateList<LdapEntry> paginateList) {
		super();
		this.paginateList = paginateList;
		this.links = new ArrayList<LinkRepresentation>();

	}

	@XmlElement()
	public List<LinkRepresentation> getLinks() {
		return links;
	}
	
	@XmlElement(name = "results")
	public List<LdapEntryRepresentation> getResults() {
		List<LdapEntryRepresentation> results = new ArrayList<LdapEntryRepresentation>();
		for (LdapEntry entry : paginateList) {
			LdapEntryRepresentation entryRepresentation = new LdapEntryRepresentation();
			entryRepresentation.setRepresentation(representation);
			entryRepresentation.setResource(resource);
			entryRepresentation.setConfig(config);
			entryRepresentation.buildRepresentation(entry);
			results.add(entryRepresentation);
		}
		return results;
	}

	@XmlElement
	public int getPageSize() {
		return paginateList.getPageSize();
	}

	@XmlElement
	public int getTotal() {
		return paginateList.getTotal();
	}

	@XmlElement
	public int getStartPage() {
		return paginateList.getStartPage();
	}

	@XmlElement
	public int getLastPage() {
		return paginateList.getLastPage();
	}

	public void buildLink(UriInfo uriInfo) {

		UriBuilder internBuilder = UriBuilder.fromPath(uriInfo.getPath());		
		for (Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
			internBuilder.queryParam(entry.getKey(), entry.getValue().toArray());
		}
		
		internBuilder.replaceQueryParam("pageSize", this.getPageSize());

		addLinkIfOk(
				true,
				LinkSearchRelation.CURRENT,
				internBuilder.replaceQueryParam("startPage",
						this.getStartPage()));
		addLinkIfOk(hasFirstPage(), LinkSearchRelation.FIRST,
				internBuilder.replaceQueryParam("startPage", 1));
		addLinkIfOk(hasPrevPage(), LinkSearchRelation.PREV,
				internBuilder.replaceQueryParam("startPage", getPrevPage()));
		addLinkIfOk(hasNextPage(), LinkSearchRelation.NEXT,
				internBuilder.replaceQueryParam("startPage", getNextPage()));
		addLinkIfOk(hasLastPage(), LinkSearchRelation.LAST,
				internBuilder.replaceQueryParam("startPage", getLastPage()));
	}

	private void addLinkIfOk(boolean ok, LinkSearchRelation relation,
			UriBuilder builder) {
		if (ok) {
			this.links.add(new LinkRepresentation(relation.value, builder
					.build().toString(), relation.type));
		}
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

	private int getNextPage() {
		return this.getStartPage() + 1;
	}

	private int getPrevPage() {
		return this.getStartPage() - 1;
	}

	private boolean hasLastPage() {
		return this.getStartPage() < getLastPage();
	}

	private boolean hasFirstPage() {
		return this.getStartPage() > 1;
	}

	private boolean hasPrevPage() {
		return this.getStartPage() > 1;
	}

	private boolean hasNextPage() {
		return getNextPage() <= getLastPage();
	}

	private enum LinkSearchRelation {
		CURRENT("current"), FIRST("first"), PREV("prev"), NEXT("next"), LAST(
				"last");

		private String value;
		private String type;

		LinkSearchRelation(String value) {
			this(value, MediaType.APPLICATION_JSON);
		}

		LinkSearchRelation(String value, String type) {
			this.value = value;
			this.type = type;
		}
	}
}
