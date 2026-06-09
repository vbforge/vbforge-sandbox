package com.vbforge.org.service.impl;

import com.vbforge.org.dto.CreatePersonDTO;
import com.vbforge.org.dto.PersonDTO;
import com.vbforge.org.dto.UpdatePersonDTO;
import com.vbforge.org.entity.Person;
import com.vbforge.org.exception.DuplicateResourceException;
import com.vbforge.org.exception.ResourceNotFoundException;
import com.vbforge.org.mapper.PersonMapper;
import com.vbforge.org.repository.PersonRepository;
import com.vbforge.org.service.PersonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    //create
    @Override public PersonDTO createPerson(CreatePersonDTO createPersonDTO) {
        //check if alias already exist
        if(personRepository.existsByAlias(createPersonDTO.getAlias())) {
            throw new DuplicateResourceException("Person with alias " + createPersonDTO.getAlias() + " already exists");
        }
        //check if email already exist
        if(personRepository.existsByEmail(createPersonDTO.getEmail())) {
            throw new DuplicateResourceException("Person with email " + createPersonDTO.getEmail() + " already exists");
        }

        Person person  = PersonMapper.toEntity(createPersonDTO);
        return PersonMapper.toDTO(personRepository.save(person));

    }

    //read:
    // 1) all
    @Transactional(readOnly = true) @Override public List<PersonDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(PersonMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 2) by id
    @Transactional(readOnly = true) @Override public PersonDTO getPersonById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Person with id " + id + " not found"));
        return PersonMapper.toDTO(person);
    }

    // 3) by alias
    @Transactional(readOnly = true) @Override public PersonDTO getPersonByAlias(String alias) {
        Person person = personRepository.findByAlias(alias)
                .orElseThrow(()-> new ResourceNotFoundException("Person with alias " + alias + " not found"));
        return PersonMapper.toDTO(person);
    }

    // 4) by email
    @Transactional(readOnly = true) @Override public PersonDTO getPersonByEmail(String email) {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("Person with email " + email + " not found"));
        return PersonMapper.toDTO(person);
    }

    //update: full update
    @Override public PersonDTO updatePerson(Long id, UpdatePersonDTO updateDTO) {
        //find person by id
        Person person = personRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Person with id " + id + " not found"));

        // If alias is being updated, check if new alias already exists
        if(updateDTO.getAlias() != null && !updateDTO.getAlias().equals(person.getAlias())) {
            if(personRepository.existsByAlias(updateDTO.getAlias())) {
                throw new DuplicateResourceException("Person with alias " + updateDTO.getAlias() + " already exists");
            }
        }

        // If email is being updated, check if new email already exists
        if(updateDTO.getEmail() != null && !updateDTO.getEmail().equals(person.getEmail())) {
            if(personRepository.existsByEmail(updateDTO.getEmail())) {
                throw new DuplicateResourceException("Person with email " + updateDTO.getEmail() + " already exists");
            }
        }
        
        PersonMapper.updateEntity(updateDTO, person);
        return PersonMapper.toDTO(personRepository.save(person));
    }
    
    //delete
    @Override public void deletePerson(Long id) {
        if(!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person with id " + id + " not found");            
        }
        personRepository.deleteById(id);
    }
    
    //count persons
    @Transactional(readOnly = true) @Override public long countAllPersons() {
        return personRepository.count();
    }
    
    //exist by alias
    @Transactional(readOnly = true) @Override public boolean existsByAlias(String alias) {
        return personRepository.existsByAlias(alias);
    }
    
    //exist by email
    @Transactional(readOnly = true) @Override public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

}
