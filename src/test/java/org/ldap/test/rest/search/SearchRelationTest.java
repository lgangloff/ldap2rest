package org.ldap.test.rest.search;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ldap.test.rest.BaseRESTUnit;

public class SearchRelationTest extends BaseRESTUnit {

	
	@Test
	public void testGetJDoe(){

		expect().log().all()
			.statusCode(200)
			.body("attributes.fonction", contains("test"))
			.given()
			.when().get("/people/j.doe");
	}

}