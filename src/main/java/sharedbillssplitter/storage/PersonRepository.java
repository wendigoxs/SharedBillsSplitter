package sharedbillssplitter.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharedbillssplitter.person.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, String> {

}
