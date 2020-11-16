package sharedbillssplitter.person;

import java.util.Collection;
import java.util.Collections;

public interface PersonHolder {
    Collection<Person> getPersons();

    PersonHolder EMPTY_HOLDER = Collections::emptySet;
}
