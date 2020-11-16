package sharedbillssplitter.storage;

import sharedbillssplitter.person.Group;

public interface GroupStorage {
    void put(Group group);

    Group get(String groupName);
}
