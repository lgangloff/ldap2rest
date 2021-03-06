package org.ldap.test;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.schema.Schema;

public class BaseLDAPUnitTest{
	
	private static InMemoryDirectoryServer ds;

	@Before
	public void setupClass() throws Exception {		

		InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
		config.addAdditionalBindCredentials("cn=Directory Manager", "password");
		config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", 389));
		config.setSchema(
				Schema.getSchema(
						"src/test/resources/schema.ldif"
		));

		ds = new InMemoryDirectoryServer(config);
		ds.importFromLDIF(true, "src/test/resources/data.ldif");
		ds.startListening();
	}
	
	@After
	public void afterClass(){
		ds.shutDown(true);
	}

	
	protected Object[] getAttributeValues(String dn, String attributeName){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-ldap.xml");
		LdapTemplate ldapTemplate = (LdapTemplate) context.getBean("ldapTemplate");

		DirContextAdapter dircontext = (DirContextAdapter) ldapTemplate.lookup(dn);
		
		return dircontext.getObjectAttributes(attributeName);
	}
}