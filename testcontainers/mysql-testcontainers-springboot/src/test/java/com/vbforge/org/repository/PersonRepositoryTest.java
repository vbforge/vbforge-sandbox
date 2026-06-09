package com.vbforge.org.repository;

import com.vbforge.org.config.AbstractDataBaseTest;
import com.vbforge.org.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonRepositoryTest extends AbstractDataBaseTest {

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        //clean db before each test
        personRepository.deleteAll();
    }

    @Test
    void testSavePerson() {
        // Given
        Person person = new Person();
        person.setName("John Doe");
        person.setEmail("john@example.com");
        person.setAlias("johndoe");
        person.setPhone("+1234567890");

        // When
        Person saved = personRepository.save(person);

        // Then
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("john@example.com", saved.getEmail());
        assertEquals("johndoe", saved.getAlias());
        assertEquals("+1234567890", saved.getPhone());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testFindById() {
        // Given
        Person person = createPerson("Jane Smith", "jane@example.com", "janesmith", "+1987654321");
        Person saved = personRepository.save(person);

        // When
        Optional<Person> found = personRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Jane Smith", found.get().getName());
        assertEquals("jane@example.com", found.get().getEmail());
    }

    @Test
    void testFindByIdNotFound() {
        // When
        Optional<Person> found = personRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByAlias() {
        // Given
        Person person = createPerson("Bob Wilson", "bob@example.com", "bobwilson", "+1122334455");
        personRepository.save(person);

        // When
        Optional<Person> found = personRepository.findByAlias("bobwilson");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Bob Wilson", found.get().getName());
        assertEquals("bob@example.com", found.get().getEmail());
    }

    @Test
    void testFindByAliasNotFound() {
        // When
        Optional<Person> found = personRepository.findByAlias("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmail() {
        // Given
        Person person = createPerson("Alice Johnson", "alice@example.com", "alicej", "+1555666777");
        personRepository.save(person);

        // When
        Optional<Person> found = personRepository.findByEmail("alice@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Alice Johnson", found.get().getName());
        assertEquals("alicej", found.get().getAlias());
    }

    @Test
    void testFindByEmailNotFound() {
        // When
        Optional<Person> found = personRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByAlias() {
        // Given
        Person person = createPerson("Charlie Brown", "charlie@example.com", "charlieb", "+1999888777");
        personRepository.save(person);

        // When & Then
        assertTrue(personRepository.existsByAlias("charlieb"));
        assertFalse(personRepository.existsByAlias("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        // Given
        Person person = createPerson("Diana Prince", "diana@example.com", "dianap", "+1666555444");
        personRepository.save(person);

        // When & Then
        assertTrue(personRepository.existsByEmail("diana@example.com"));
        assertFalse(personRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testFindAll() {
        // Given
        personRepository.save(createPerson("Person 1", "person1@example.com", "person1", "+1111111111"));
        personRepository.save(createPerson("Person 2", "person2@example.com", "person2", "+2222222222"));
        personRepository.save(createPerson("Person 3", "person3@example.com", "person3", "+3333333333"));

        // When
        List<Person> allPersons = personRepository.findAll();

        // Then
        assertEquals(3, allPersons.size());
        assertTrue(allPersons.stream().anyMatch(p -> p.getAlias().equals("person1")));
        assertTrue(allPersons.stream().anyMatch(p -> p.getAlias().equals("person2")));
        assertTrue(allPersons.stream().anyMatch(p -> p.getAlias().equals("person3")));
    }

    @Test
    void testUpdatePerson() {
        // Given
        Person person = createPerson("Original Name", "original@example.com", "original", "+0000000000");
        Person saved = personRepository.save(person);

        // When
        saved.setName("Updated Name");
        saved.setEmail("updated@example.com");
        saved.setPhone("+9999999999");
        Person updated = personRepository.save(saved);

        // Then
        assertEquals("Updated Name", updated.getName());
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("+9999999999", updated.getPhone());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void testDeleteById() {
        // Given
        Person person = createPerson("To Delete", "delete@example.com", "todelete", "+4444444444");
        Person saved = personRepository.save(person);
        assertEquals(1, personRepository.count());

        // When
        personRepository.deleteById(saved.getId());

        // Then
        assertEquals(0, personRepository.count());
        assertFalse(personRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void testDeleteAll() {
        // Given
        personRepository.save(createPerson("Person A", "a@example.com", "persona", "+1111111111"));
        personRepository.save(createPerson("Person B", "b@example.com", "personb", "+2222222222"));
        assertEquals(2, personRepository.count());

        // When
        personRepository.deleteAll();

        // Then
        assertEquals(0, personRepository.count());
    }

    @Test
    void testCount() {
        // Given
        assertEquals(0, personRepository.count());

        personRepository.save(createPerson("Person 1", "p1@example.com", "p1", "+1111111111"));
        assertEquals(1, personRepository.count());

        personRepository.save(createPerson("Person 2", "p2@example.com", "p2", "+2222222222"));
        assertEquals(2, personRepository.count());
    }

    @Test
    void testUniqueConstraintOnAlias() {
        // Given
        Person person1 = createPerson("First User", "first@example.com", "uniquealias", "+1111111111");
        personRepository.save(person1);

        // When
        Person person2 = createPerson("Second User", "second@example.com", "uniquealias", "+2222222222");

        // Then
        assertThrows(Exception.class, () -> personRepository.save(person2));
    }

    @Test
    void testUniqueConstraintOnEmail() {
        // Given
        Person person1 = createPerson("First User", "unique@example.com", "firstuser", "+1111111111");
        personRepository.save(person1);

        // When
        Person person2 = createPerson("Second User", "unique@example.com", "seconduser", "+2222222222");

        // Then
        assertThrows(Exception.class, () -> personRepository.save(person2));
    }

    @Test
    void testMultiplePersonsWithDifferentAliases() {
        // Given
        Person person1 = createPerson("User 1", "user1@example.com", "user1", "+1111111111");
        Person person2 = createPerson("User 2", "user2@example.com", "user2", "+2222222222");
        Person person3 = createPerson("User 3", "user3@example.com", "user3", "+3333333333");

        // When
        personRepository.save(person1);
        personRepository.save(person2);
        personRepository.save(person3);

        // Then
        assertEquals(3, personRepository.count());
        assertTrue(personRepository.existsByAlias("user1"));
        assertTrue(personRepository.existsByAlias("user2"));
        assertTrue(personRepository.existsByAlias("user3"));
    }

    @Test
    void testCreatePersonsWithDifferentEmailsAndAliases() {
        // Given - All unique
        Person person1 = createPerson("User 1", "user1@example.com", "user1", "+1111111111");
        Person person2 = createPerson("User 2", "user2@example.com", "user2", "+2222222222");
        Person person3 = createPerson("User 3", "user3@example.com", "user3", "+3333333333");

        // When
        personRepository.save(person1);
        personRepository.save(person2);
        personRepository.save(person3);

        // Then
        assertEquals(3, personRepository.count());
    }

    @Test
    void testNullablePhoneField() {
        // Given - phone is optional
        Person person = new Person();
        person.setName("No Phone");
        person.setEmail("nophone@example.com");
        person.setAlias("nophone");
        person.setPhone(null);  // Explicitly null

        // When
        Person saved = personRepository.save(person);

        // Then
        assertNotNull(saved.getId());
        assertNull(saved.getPhone());
    }

    @Test
    void testTimestampsAreAutoGenerated() {
        // Given
        Person person = createPerson("Timestamp Test", "timestamp@example.com", "timestamptest", "+9999999999");

        // When
        Person saved = personRepository.save(person);

        // Then
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        // Wait a moment and update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        saved.setName("Updated Timestamp Test");
        Person updated = personRepository.save(saved);

        // Updated timestamp should be later
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(updated.getCreatedAt()) ||
                updated.getUpdatedAt().equals(updated.getCreatedAt()));
    }



    // Helper method to create Person objects
    private Person createPerson(String name, String email, String alias, String phone) {
        Person person = new Person();
        person.setName(name);
        person.setEmail(email);
        person.setAlias(alias);
        person.setPhone(phone);
        return person;
    }

}