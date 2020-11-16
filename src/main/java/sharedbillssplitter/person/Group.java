package sharedbillssplitter.person;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "groups")
public class Group implements PersonHolder {

    public static final Group UNKNOWN_GROUP = new Group(null) {
        @Override
        public String toString() {
            return "Unknown group";
        }
    };

    private String name;

    private Set<Person> persons;

    public Group(String name) {
        this();
        this.name = name;
    }

    public Group() {
        persons = new HashSet<>();
    }

    public Group(String name, Collection<Person> persons) {
        this(name);
        this.persons.addAll(persons);
    }


    @Override
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name = "group_person",
            joinColumns = {@JoinColumn(name = "groupName")},
            inverseJoinColumns = {@JoinColumn(name = "personNameName")}
    )
    public Set<Person> getPersons() {
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        this.persons = persons;
    }

    @Override
    public String toString() {
        Stream<Person> personStream = Optional.ofNullable(getPersons()).stream().flatMap(Collection::stream);
        return personStream
                .map(Person::toString)
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + persons.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Group) && ((Group) obj).name.equals(this.name) && ((Group) obj).persons.equals(this.persons);
    }
}
