package org.ldap.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.ldap.filter.Filter;

/**
 * Classe utilitaire permettant de tracer le temps d'éxécution des recherches
 * Si plus de {@code MAX_TIME_BEFORE_WARN}, alors la trace sera en niveau WARN
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 *
 */
public class LdapSearchTimeExecutionUtil {

	private static final int MAX_TIME_BEFORE_WARN = 1000;

	private static final Logger log = Logger.getLogger(LdapSearchTimeExecutionUtil.class);
	
	private long start;
	private long end;
	
	/**
	 * Méthode à appeler avant le lancement de la recherche
	 */
	public void start(){
		start = System.currentTimeMillis();
	}
	
	/**
	 * Méthode à appeler après la recherche
	 * 
	 * @param filter - Le filtre LDAP utilisé pour la recherche
	 * @param results - La liste des résultats de la recherche
	 */
	public void end(Filter filter, List<?> results){
		end = System.currentTimeMillis();
		
		long time = end - start;
		
		if (log.isTraceEnabled() || time > MAX_TIME_BEFORE_WARN){
			String message = "LdapSearch ("+filter.encode()+") - "+results.size()+" results in "+time+" ms.";
			if (time > MAX_TIME_BEFORE_WARN)
				log.warn(message);
			else
				log.trace(message);
		}
	}
}
