package com.vbforge.org.controller;

import com.vbforge.org.dto.CreatePersonDTO;
import com.vbforge.org.dto.PersonDTO;
import com.vbforge.org.dto.UpdatePersonDTO;
import com.vbforge.org.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    //create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody CreatePersonDTO createDTO) {
        PersonDTO person = personService.createPerson(createDTO);

        return ResponseEntity.created(URI.create("/api/persons/" + person.getId())).body(person);
//      or
//      return new ResponseEntity<>(person, HttpStatus.CREATED);
    }

    //get all
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<PersonDTO>> getAllPersons() {
        List<PersonDTO> persons = personService.getAllPersons();
        return ResponseEntity.ok(persons);
    }

    //get by id
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        PersonDTO person = personService.getPersonById(id);
        return ResponseEntity.ok(person);

    }

    //get by alias
    @GetMapping("/alias/{alias}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PersonDTO> getPersonByAlias(@PathVariable String alias) {
        PersonDTO person = personService.getPersonByAlias(alias);
        return ResponseEntity.ok(person);
    }

    //get by email
    @GetMapping("/email/{email}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PersonDTO> getPersonByEmail(@PathVariable String email) {
        PersonDTO person = personService.getPersonByEmail(email);
        return ResponseEntity.ok(person);
    }

    //update
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id, @Valid @RequestBody UpdatePersonDTO updateDTO) {
        PersonDTO person = personService.updatePerson(id, updateDTO);
        return ResponseEntity.ok(person);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PersonDTO> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    //count
    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Long> getPersonCount() {
        long counted = personService.countAllPersons();
        return ResponseEntity.ok(counted);
    }


}
