package com.github.acme.quarkus.petclinic.web.resource;

import io.quarkus.test.junit.SubstrateTest;

@SubstrateTest
public class NativeVetResourceIT extends VetResourceTest {

    private static final String JSON_HENRY_STEVENS = "{\"id\":5,\"firstName\":\"Henry\",\"lastName\":\"Stevens\",\"nrOfSpecialties\":1,\"specialties\":[{\"id\":1,\"name\":\"radiology\"}]}";
    private static final String JSON_FIND_ALL_VETS = "[{\"id\":1,\"firstName\":\"James\",\"lastName\":\"Carter\",\"nrOfSpecialties\":0,\"specialties\":[]},{\"id\":2,\"firstName\":\"Helen\",\"lastName\":\"Leary\",\"nrOfSpecialties\":1,\"specialties\":[{\"id\":1,\"name\":\"radiology\"}]},{\"id\":3,\"firstName\":\"Linda\",\"lastName\":\"Douglas\",\"nrOfSpecialties\":2,\"specialties\":[{\"id\":3,\"name\":\"dentistry\"},{\"id\":2,\"name\":\"surgery\"}]},{\"id\":4,\"firstName\":\"Rafael\",\"lastName\":\"Ortega\",\"nrOfSpecialties\":1,\"specialties\":[{\"id\":2,\"name\":\"surgery\"}]},{\"id\":5,\"firstName\":\"Henry\",\"lastName\":\"Stevens\",\"nrOfSpecialties\":1,\"specialties\":[{\"id\":1,\"name\":\"radiology\"}]},{\"id\":6,\"firstName\":\"Sharon\",\"lastName\":\"Jenkins\",\"nrOfSpecialties\":0,\"specialties\":[]}]";

    // Execute the same tests but in native mode.


    @Override
    protected String getJsonFindVetByNameEndpoint() {
        return JSON_HENRY_STEVENS;
    }

    @Override
    protected String getJsonFindAllVets() {
        return JSON_FIND_ALL_VETS;
    }
}
