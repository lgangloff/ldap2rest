package org.ldap.test.rest.create;

import static com.jayway.restassured.RestAssured.expect;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ldap.core.rules.ReadOnlyRules;
import org.ldap.core.rules.RequiredRules;
import org.ldap.core.rules.TypeRules;
import org.ldap.test.rest.BaseRESTUnit;

public class CreateTest extends BaseRESTUnit {

	@Test
	public void testCreateResourceSansId(){
		String ressource = "/people";
		
		expect().log().ifError()
		.statusCode(400)
		.body(Matchers.equalTo("An ID must be provided"))
		.given()
		.when()
		.content("{\"attributes\" : { \"tel-mobile\" : null }}")
		.contentType("application/json")
		.post(ressource);
	}

	@Test
	public void testCreateWrongResource(){
		String ressource = "/people/blabla";
		
		expect().log().ifError()
		.statusCode(400)
		.body(Matchers.equalTo("In order to create a resource of type 'people', POST a request on '/people' not on '/people/blabla'"))
		.given()
		.when()
		.content("{\"attributes\" : { \"tel-mobile\" : null }}")
		.contentType("application/json")
		.post(ressource);
	}
	

	@Test
	public void testCreateNoResource(){
		String ressource = "/plop";
		
		expect().log().ifError()
		.statusCode(400)
		.body(Matchers.equalTo("Unable to identify the ressource 'plop'"))
		.given()
		.when()
		.content("{\"attributes\" : { \"tel-mobile\" : null }}")
		.contentType("application/json")
		.post(ressource);
	}
	

	@Test
	public void testCreateResourceNoAttribute(){
		String ressource = "/people";
		
		expect().log().ifError()
		.statusCode(400)
		.body("warnings",
				Matchers.hasItems(
						Matchers.containsString(RequiredRules.class.getSimpleName()), 
						Matchers.containsString("nom"), 
						Matchers.containsString("civilite") ))
		.given()
		.when()
		.content("{\"id\" : \"l.gangloff\" }}")
		.contentType("application/json")
		.post(ressource);
	}

	@Test
	public void testCreateResourceBadAttribute(){
		String ressource = "/people";
		
		expect().log().ifError()
		.statusCode(400)
		.body("warnings",
				Matchers.hasItems(
						Matchers.containsString(TypeRules.class.getSimpleName()), 
						Matchers.not(Matchers.containsString("nom")), 
						Matchers.containsString("civilite") ))
		.given()
		.when()
		.content("{\"id\" : \"l.gangloff\", "
				+ "\"attributes\" : { "
				+ "\"civilite\" : \"BaddValue\","
				+ "\"nom\" : \"Gangloff\" }}")
				
		.contentType("application/json")
		.post(ressource);
	}
	

	@Test
	public void testCreateResourceMinimumAttribute(){
		String ressource = "people";
		String id = "l.gangloff";
		String civilite = "M.";
		String nom = "Gangloff";
		String newRessource = ressource + "/" + id;
		
		String content = "{\"id\" : \""+id+"\", "
				+ "\"attributes\" : { "
				+ "\"civilite\" : \""+civilite+"\","
				+ "\"nom\" : \""+nom+"\" }}";
		
		expect().log().all()
		.statusCode(201)
		.body(Matchers.equalTo(""))
		.header("Location", newRessource)
		.given()
		.when()
		.content(content)				
		.contentType("application/json")
		.post(ressource);
		
		
		expect().log().all()
		.statusCode(200)
		.body("id", Matchers.equalTo(newRessource))
		.body("attributes.nom", Matchers.equalTo(nom))
		.body("attributes.civilite", Matchers.equalTo(civilite))
		.given()
		.when()
		.get(newRessource);
	}
}