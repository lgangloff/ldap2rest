package org.ldap.ws.ex;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.springframework.ldap.NameNotFoundException;


/**
 * Mapper de l'exception {@link NameNotFoundException}
 * Si cette exception survient, c'est que l'entrée demandé n'existe pas dans le LDAP.
 * Dans ce cas, un code HTTP {@link Status#NOT_FOUND} est retourné
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 *
 */
@Provider
public class NameNotFoundMapper implements
		ExceptionMapper<org.springframework.ldap.NameNotFoundException> {
	
	private static final Logger log = Logger
			.getLogger(NameNotFoundMapper.class);

	public Response toResponse(org.springframework.ldap.NameNotFoundException ex) {
		log.info(ex.getMessage());
		return Response.status(Status.NOT_FOUND)
				.entity("La ressource n'existe pas").type("text/plain")
				.build();
	}
}