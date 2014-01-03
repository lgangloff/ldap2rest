package org.ldap.ws.repr;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * 
 * Représentation d'un lien
 * Une lien a un identifiant (rel), une url (href) et un mediatype associé à l'url (type)
 * 
 * @author Loic Gangloff: loic.gangloff@atos.net
 */
@XmlRootElement(name = "link")
@JsonSerialize(include = Inclusion.NON_NULL)
public class LinkRepresentation {

	private String rel;
	private String href;
	private String type;

	public LinkRepresentation() {
		super();
	}

	public LinkRepresentation(String rel, String href, String type) {
		super();
		this.rel = rel;
		this.href = href;
		this.type = type;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
