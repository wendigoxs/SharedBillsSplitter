package sharedbillssplitter.person;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

@Entity
public class Person implements PersonHolder, Comparable<Person> {
    private String name;

    public Person(String name) {
        this.name = name;
    }

    public Person() {
    }

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    @Transient
    public List<Person> getPersons() {
        return List.of(this);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Person) && ((Person) obj).getName().equals(this.getName());
    }

    @Override
    public int compareTo(Person person) {
        return this.getName().compareTo(person.getName());
    }
}
