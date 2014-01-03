package org.ldap.ws;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.ldap.Rest2LdapConfig;
import org.ldap.core.ILdapDAO;
import org.ldap.ws.ex.NameNotFoundMapper;
import org.ldap.ws.resources.impl.LdapResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.LdapTemplate;



/**
 * Classe définissant toutes les ressources de l'application REST
 * 
 * @author Loic Gangloff: loic.gangloff@gmail.com
 *
 */
public class RESTApplication extends Application {
	
	private static final Logger log = Logger.getLogger(RESTApplication.class);

	private Set<Class<?>> classes = new HashSet<Class<?>>();
	private Set<Object> singletons = new HashSet<Object>();
	
	public static String SPRING_CONFIG = "applicationContext-ldap.xml";
	
	@Inject
	public RESTApplication(ServiceLocator serviceLocator) {
		log.info("Initialisation de l'application");
		classes.add(LdapResource.class);
		classes.add(NameNotFoundMapper.class);

		classes.add(MultiPartFeature.class);
		
		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		singletons.add(provider);
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(SPRING_CONFIG);
		Rest2LdapConfig config = ctx.getBean("rest2ldapConfig", Rest2LdapConfig.class);
		ILdapDAO ldapDAO = ctx.getBean(ILdapDAO.class);
		
        DynamicConfiguration dc = Injections.getConfiguration(serviceLocator);
        Injections.addBinding(
                Injections.newBinder(ctx).to(ApplicationContext.class), dc);
        Injections.addBinding(
                Injections.newBinder(config).to(Rest2LdapConfig.class), dc);
        Injections.addBinding(
        		Injections.newBinder(ldapDAO).to(ILdapDAO.class), dc);

        dc.commit();

		testLdapConnection(ctx);        
	}

	private void testLdapConnection(
			ClassPathXmlApplicationContext classPathXmlApplicationContext) {
		log.info("Initialisation OK");
		log.info("Test connection...");
		
		try {
			LdapTemplate ldapTemplate = classPathXmlApplicationContext.getBean(LdapTemplate.class);
			ldapTemplate.lookup("");

			log.info("Test connection OK");
		} catch (RuntimeException e) {
			log.fatal("Test connection échoué", e);
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

}
