package org.ldap.test.rest;

import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ldap.test.BaseLDAPUnitTest;
import org.ldap.ws.RESTApplication;

import com.jayway.restassured.RestAssured;

@RunWith(Arquillian.class)
@Run(RunModeType.AS_CLIENT)
public abstract class BaseRESTUnit extends BaseLDAPUnitTest {

	protected static String WEB_XML = "src/test/resources/web-test.xml";
	
    @Deployment()
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addClass(RESTApplication.class).
                setWebXML(new File(WEB_XML));

    }
    
	@Before
	public void setUp() {
		RestAssured.basePath = "/test/rest/";
		RestAssured.port= 9090;
		RestAssured.config = newConfig().decoderConfig(decoderConfig().defaultContentCharset("UTF-8"));
	}
}