package org.ldap.test.rest.search;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ldap.test.rest.BaseRESTUnit;

public class InternalSearchTest extends BaseRESTUnit {

	
	@Test
	public void testEveryPeople(){

		expect().log().ifError()
			.statusCode(200)
			.body("total", equalTo(2))
			.body("results.id", containsInAnyOrder("people/v.rossi", "people/j.doe"))
			.given()
			.when().get("/people");
	}

	@Test
	public void testEveryPeopleLikeR(){

		expect().log().ifError()
			.statusCode(200)
			.body("total", equalTo(1))
			.body("results.id", not(contains("people/j.doe")))
			.given()
			.when().get("/people?nomPrenom=R");
	}
	

	@Test
	public void testGetRossi(){

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo("ROSSI"))
			.body("attributes.tel-mobile", nullValue())
			.given()
			.when().get("/people/v.rossi");
	}

	@Test
	public void testGetFullRossi(){

		expect().log().ifError()
			.statusCode(200)
			.body("attributes.nom", equalTo("ROSSI"))
			.body("attributes.tel-mobile", equalTo("06.18.18.18.18"))
			.given()
			.when().get("/people/v.rossi?view=full");
	}
	

	@Test
	public void testGetLinks(){

		expect().log().ifError()
			.statusCode(200)
			.body("links.rel", containsInAnyOrder("this", "this/default", "this/full"))
			.given()
			.when().get("/people/v.rossi?view=full");
	}
}