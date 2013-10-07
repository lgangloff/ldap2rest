package org.ldap.ws.resources;

import javax.naming.InvalidNameException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**  
 * @author Loic Gangloff: loic.gangloff@gmail
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/{path:.+}")
public interface ILdapResource {

	@GET	
	public Response getLdapResource(
			@PathParam("path") String path,
			@QueryParam("view") @DefaultValue("default") String view) throws InvalidNameException;
		
}
