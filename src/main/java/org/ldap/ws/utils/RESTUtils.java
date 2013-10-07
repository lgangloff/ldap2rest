package org.ldap.ws.utils;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;



/**
 * 
 * Class utilitaire permettant de faciliter certaines opération REST
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 */
public class RESTUtils {

	private static final Logger log = Logger.getLogger(RESTUtils.class);
	
	/**
	 * Renvoie un code {@link Status#NO_CONTENT} si l'objet est null
	 * 
	 * @param o
	 * @throws WebApplicationException
	 */
	public static void checkNotNull(Object o) throws WebApplicationException{
		if (o == null){
			throw new WebApplicationException(Status.NO_CONTENT);
		}
	}
	/**
	 * Renvoie un code {@link Status#NO_CONTENT} si la liste est nulle ou vide
	 * 
	 * @param o
	 * @throws WebApplicationException
	 */
	public static void checkNotNull(List<?> list) throws WebApplicationException{
		if (list == null || list.size() == 0){
			throw new WebApplicationException(Status.NO_CONTENT);
		}
	}
}
