package com.github.acme.quarkus.petclinic.test.support;

import com.github.acme.quarkus.petclinic.model.Vet;
import com.github.acme.quarkus.petclinic.repository.VetRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mock repository for the Quarkus PetClinic.
 *
 * @author Erik-Berndt Scheper
 * @since 21-08-2019
 */
@Mock
@ApplicationScoped
public class MockVetRepository extends VetRepository implements PanacheRepository<Vet> {

    public static final String STEVENS = "Stevens";
    public static final String JENKINS = "Jenkins";

    @Override
    public Vet findByName(String name) {
        return listAllVets().stream().filter(v -> name.equals(v.lastName)).findFirst().orElse(null);
    }

    @Override
    public List<Vet> listAllVets() {
        return Arrays.asList(vetStevens(), vetJenkins());
    }

    private static Vet vetStevens() {
        Vet vet;
        vet = new Vet();
        vet.firstName = "Henry";
        vet.lastName = STEVENS;
        vet.specialties = Collections.emptySet();
        vet.id = 1L;
        return vet;
    }

    private static Vet vetJenkins() {
        Vet vet;
        vet = new Vet();
        vet.firstName = "Sharon";
        vet.lastName = JENKINS;
        vet.specialties = Collections.emptySet();
        vet.id = 2L;
        return vet;
    }

}
