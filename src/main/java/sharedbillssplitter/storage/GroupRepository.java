package sharedbillssplitter.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sharedbillssplitter.person.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
}
