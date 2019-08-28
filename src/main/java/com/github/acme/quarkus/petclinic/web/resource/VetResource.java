package com.github.acme.quarkus.petclinic.web.resource;

import com.github.acme.quarkus.petclinic.model.Vet;
import com.github.acme.quarkus.petclinic.repository.VetRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/vets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VetResource {

    @Inject
    VetRepository vetRepository;

    @GET
    public List<Vet> list() {
        return vetRepository.listAllVets();
    }

    @GET
    @Path("/name/{name}")
    public Vet findByName(@PathParam("name") String name) {
        return vetRepository.findByName(name);
    }

}
