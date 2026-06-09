package com.vbforge.org.mapper;

import com.vbforge.org.dto.CreatePersonDTO;
import com.vbforge.org.dto.PersonDTO;
import com.vbforge.org.dto.UpdatePersonDTO;
import com.vbforge.org.entity.Person;
import java.time.LocalDateTime;

public class PersonMapper {
    
    public static PersonDTO toDTO(Person person) {
        if (person == null) {
            return null;
        }
        
        return new PersonDTO(
            person.getId(),
            person.getName(),
            person.getEmail(),
            person.getAlias(),
            person.getPhone(),
            person.getCreatedAt(),
            person.getUpdatedAt()
        );
    }
    
    public static Person toEntity(CreatePersonDTO createDTO) {
        if (createDTO == null) {
            return null;
        }
        
        Person person = new Person();
        person.setName(createDTO.getName());
        person.setEmail(createDTO.getEmail());
        person.setAlias(createDTO.getAlias());
        person.setPhone(createDTO.getPhone());
        
        return person;
    }
    
    public static void updateEntity(UpdatePersonDTO updateDTO, Person person) {
        if (updateDTO == null || person == null) {
            return;
        }
        
        if (updateDTO.getName() != null) {
            person.setName(updateDTO.getName());
        }
        
        if (updateDTO.getEmail() != null) {
            person.setEmail(updateDTO.getEmail());
        }
        
        if (updateDTO.getAlias() != null) {
            person.setAlias(updateDTO.getAlias());
        }
        
        if (updateDTO.getPhone() != null) {
            person.setPhone(updateDTO.getPhone());
        }
        
        person.setUpdatedAt(LocalDateTime.now());
    }
}