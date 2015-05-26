package org.ldap.test.rest.update;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ldap.core.rules.MaxLengthRules;
import org.ldap.core.rules.ReadOnlyRules;
import org.ldap.core.rules.RequiredRules;
import org.ldap.core.rules.TypeRules;
import org.ldap.test.rest.BaseRESTUnit;

public class UpdateTest extends BaseRESTUnit {

	@Test
	public void testUpdateResourceNotPresent(){
		String ressource = "/people";
		
		expect().log().ifError()
		.statusCode(400)
		.given()
		.when()
		.content("{\"attributes\" : { \"tel-mobile\" : null }}")
		.contentType("application/json")
		.put(ressource);
	}

	@Test
	public void testUpdateResourceNotFound(){
		String ressource = "/people/nexistepasdutout";
		
		expect().log().ifError()
		.statusCode(404)
		.given()
		.when()
		.content("{\"attributes\" : { \"tel-mobile\" : null }}")
		.contentType("application/json")
		.put(ressource);
	}
	
	
	@Test
	public void testUpdateForbidden(){

		String tel = "06.06.06.06.06";
		String ressource = "/people/v.rossi?view=full";
		
		expect().log().ifError()
			.statusCode(403)
			.given()
			.when()
			.content("{\"attributes\" : { \"tel-mobile\" : \""+ tel +"\" }}")
			.contentType("application/json")
			.put(ressource);
		
	}

	@Test
	public void testUpdateSuccess(){

		String actualName = "ROSSI";		
		String expectedName = "Lorenzo";		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo(actualName))
			.body("attributes.prenom", equalTo("Valentino"))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings", nullValue() )
			.given()
			.when()
			.content("{\"attributes\" : { \"nom\" : \""+ expectedName +"\" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo(expectedName))
			.body("attributes.prenom", equalTo("Valentino"))
			.given()
			.when().get(ressource);
	}
	

	@Test
	public void testUpdateReadOnlyAttribute(){

		String actualEmployeeNumber = "112";		
		String ressource = "/people/v.rossi";
		
		expect().log().ifError()
			.statusCode(200)
			.body("attributes.num-employe", equalTo(actualEmployeeNumber))
			.given()
			.when().get(ressource);
		
		
		expect().log().all()
			.statusCode(200)
			.body("warnings",Matchers.hasItem(Matchers.containsString(ReadOnlyRules.class.getSimpleName())) )
			.given()
			.when()
			.content("{\"attributes\" : { \"num-employe\" : \"234\" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.num-employe", equalTo(actualEmployeeNumber))
			.given()
			.when().get(ressource);
	}
	

	@Test
	public void testUpdateCleanAttribute(){

		String actualFirstName = "Valentino";		
		String expectedFirstName = null;		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.prenom", equalTo(actualFirstName))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings", nullValue() )
			.given()
			.when()
			.content("{\"attributes\" : { \"prenom\" : "+ expectedFirstName +" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().all()
			.statusCode(200)
			.body("attributes.prenom", nullValue())
			.given()
			.when().get(ressource);
	}

	@Test
	public void testUpdateCleanRequiredAttribute(){

		String actualName = "ROSSI";		
		String expectedName = null;		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo(actualName))
			.body("attributes.prenom", equalTo("Valentino"))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings",Matchers.hasItem(Matchers.containsString(RequiredRules.class.getSimpleName())) )
			.given()
			.when()
			.content("{\"attributes\" : { \"nom\" : "+ expectedName +" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo(actualName))
			.body("attributes.prenom", equalTo("Valentino"))
			.given()
			.when().get(ressource);
	}

	@Test
	public void testUpdateTooLongAttribute(){

		String actualName = "ROSSI";		
		String expectedName = "ROSSSIIIIIIIIIIIIIIIII";		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo(actualName))
			.body("attributes.prenom", equalTo("Valentino"))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings",Matchers.hasItem(Matchers.containsString(MaxLengthRules.class.getSimpleName())) )
			.given()
			.when()
			.content("{\"attributes\" : { \"nom\" : \""+ expectedName +"\" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo(actualName))
			.body("attributes.prenom", equalTo("Valentino"))
			.given()
			.when().get(ressource);
	}
	

	@Test
	public void testUpdateEnumOkAttribute(){

		String actualTitle = "M.";		
		String expectedTitle = "Mlle";		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.civilite", equalTo(actualTitle))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings", nullValue())
			.given()
			.when()
			.content("{\"attributes\" : { \"civilite\" : \""+ expectedTitle +"\" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.civilite", equalTo(expectedTitle))
			.given()
			.when().get(ressource);
	}
	

	@Test
	public void testUpdateEnumFailAttribute(){

		String actualTitle = "M.";		
		String expectedTitle = "Yop";		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.civilite", equalTo(actualTitle))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings",Matchers.hasItem(Matchers.containsString(TypeRules.class.getSimpleName())) )
			.given()
			.when()
			.content("{\"attributes\" : { \"civilite\" : \""+ expectedTitle +"\" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.civilite", equalTo(actualTitle))
			.given()
			.when().get(ressource);
	}

	@Test
	public void testUpdateIntegerFailAttribute(){

		String actualTel = "0383335566";		
		String expectedTel = "yop";		
		String ressource = "/people/v.rossi";
		
		expect()
			.log().ifError()
			.statusCode(200)
			.body("attributes.tel-externe", equalTo(actualTel))
			.given()
		.when()
			.get(ressource);
		
		
		expect().log().ifError()
			.statusCode(200)
			.body("warnings",Matchers.hasItem(Matchers.containsString(TypeRules.class.getSimpleName())) )
			.given()
			.when()
			.content("{\"attributes\" : { \"tel-externe\" : \""+ expectedTel +"\" }}")
			.contentType("application/json")
			.put(ressource);
		

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.tel-externe", equalTo(actualTel))
			.given()
			.when().get(ressource);
	}
}