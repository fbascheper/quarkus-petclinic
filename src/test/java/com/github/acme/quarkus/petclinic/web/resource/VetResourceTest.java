package com.github.acme.quarkus.petclinic.web.resource;

import com.github.acme.quarkus.petclinic.repository.VetRepository;
import com.github.acme.quarkus.petclinic.test.support.MockVetRepository;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class VetResourceTest {

    private static final String JSON_HENRY_STEVENS = "{\"id\":1,\"firstName\":\"Henry\",\"lastName\":\"Stevens\",\"nrOfSpecialties\":0,\"specialties\":[]}";
    private static final String JSON_LIST_ALL = "[{\"id\":1,\"firstName\":\"Henry\",\"lastName\":\"Stevens\",\"nrOfSpecialties\":0,\"specialties\":[]},{\"id\":2,\"firstName\":\"Sharon\",\"lastName\":\"Jenkins\",\"nrOfSpecialties\":0,\"specialties\":[]}]";

    @Inject
    VetRepository vetRepository;

    @Test
    public void unittestFindVetByNameEndpoint() {
        String name = MockVetRepository.STEVENS;

        given()
                .pathParam("name", name)
                .when().get("/vets/name/{name}")
                .then()
                .statusCode(200)
                .body(is(getJsonFindVetByNameEndpoint()));

    }

    @Test
    public void testFindAllVets() {
        given()
                .when().get("/vets")
                .then()
                .statusCode(200)
                .body(is(getJsonFindAllVets()));
    }

    protected String getJsonFindVetByNameEndpoint() {
        return JSON_HENRY_STEVENS;
    }

    protected String getJsonFindAllVets() {
        return JSON_LIST_ALL;
    }
}
