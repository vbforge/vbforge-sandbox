package com.vbforge.org.service;

import com.vbforge.org.dto.CreatePersonDTO;
import com.vbforge.org.dto.PersonDTO;
import com.vbforge.org.dto.UpdatePersonDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PersonService {
    //create
    PersonDTO createPerson(CreatePersonDTO createPersonDTO);

    //read:
    // 1) all
    @Transactional(readOnly = true)
    List<PersonDTO> getAllPersons();

    // 2) by id
    @Transactional(readOnly = true)
    PersonDTO getPersonById(Long id);

    // 3) by alias
    @Transactional(readOnly = true)
    PersonDTO getPersonByAlias(String alias);

    // 4) by email
    @Transactional(readOnly = true)
    PersonDTO getPersonByEmail(String email);

    //update: full update
    PersonDTO updatePerson(Long id, UpdatePersonDTO updateDTO);

    //delete
    void deletePerson(Long id);

    //count persons
    @Transactional(readOnly = true)
    long countAllPersons();

    //exist by alias
    @Transactional(readOnly = true)
    boolean existsByAlias(String alias);

    //exist by email
    @Transactional(readOnly = true)
    boolean existsByEmail(String email);
}
