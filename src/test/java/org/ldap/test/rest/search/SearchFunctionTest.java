package org.ldap.test.rest.search;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ldap.test.rest.BaseRESTUnit;

public class SearchFunctionTest extends BaseRESTUnit {


	@Test
	public void testSearchAllFunction(){

		expect().log().ifError()
			.statusCode(200)
			.body("results.attributes.libelle", containsInAnyOrder("Secrétaire", "Responsable de pôle"))
			.given()
			.when().get("/fonction");
	}
	

	@Test
	public void testSearchFunction(){

		expect().log().ifError()
			.statusCode(200)
			.body("results.attributes.libelle", contains("Secrétaire"))
			.given()
			.when().get("/fonction?libelle=Secré*");
	}
	

	@Test
	public void testGetSecretaire(){

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.libelle", equalTo("Secrétaire"))
			.given()
			.when().get("/fonction/secretaire");
	}
	

	@Test
	public void testGetSecretaireFull(){

		expect().log().all()
			.statusCode(200)
			.body("attributes.libelle", equalTo("Secrétaire"))
			.body("attributes.membres.id", containsInAnyOrder("people/v.rossi", "people/j.doe"))
			.given()
			.when().get("/fonction/secretaire?view=full");
	}

}