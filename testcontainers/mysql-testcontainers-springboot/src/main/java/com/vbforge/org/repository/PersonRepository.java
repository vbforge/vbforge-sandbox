package com.vbforge.org.repository;

import com.vbforge.org.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByAlias(String alias);

    Optional<Person> findByEmail(String email);

    boolean existsByAlias(String alias);

    boolean existsByEmail(String email);

}
