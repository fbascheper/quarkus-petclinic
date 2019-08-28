package com.github.acme.quarkus.petclinic.repository;

import com.github.acme.quarkus.petclinic.model.Vet;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * quarkus-petclinic - Description.
 *
 * @author Erik-Berndt Scheper
 * @since 12-08-2019
 */
@ApplicationScoped
public class VetRepository implements PanacheRepository<Vet> {

    // put your custom logic here as instance methods

    public Vet findByName(String name) {
        return find("lastName", name).firstResult();
    }

    public List<Vet> listAllVets() {
        return listAll();
    }
}
