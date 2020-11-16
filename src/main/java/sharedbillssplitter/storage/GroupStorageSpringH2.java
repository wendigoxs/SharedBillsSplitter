package sharedbillssplitter.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sharedbillssplitter.person.Group;

@Component(value = "GROUP_STORAGE_SPRING_H2")
public class GroupStorageSpringH2 implements GroupStorage {

    @Autowired
    GroupRepository groupRepository;

    @Override
    public void put(Group group) {
        groupRepository.save(group);
    }

    @Override
    public Group get(String groupName) {
        return groupRepository.findById(groupName).orElse(null);
    }
}
