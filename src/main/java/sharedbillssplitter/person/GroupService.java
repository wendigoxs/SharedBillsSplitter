package sharedbillssplitter.person;

import org.springframework.beans.factory.annotation.Autowired;
import sharedbillssplitter.storage.GroupStorage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GroupService {
    public static final String EMPTY_STRING = "";
    GroupStorage groupStorage;

    public GroupService(@Autowired GroupStorage groupStorage) {
        this.groupStorage = groupStorage;
    }

    public String createGroup(String groupName, Collection<Person> persons) {
        groupStorage.put(new Group(groupName, persons));
        return EMPTY_STRING;
    }

    public String showGroup(String groupName) {
        return Optional.ofNullable(groupStorage.get(groupName))
                .orElse(Group.UNKNOWN_GROUP)
                .toString();
    }


    public Collection<Person> parsePersonHoldersString(final String personsStr) {
        if (personsStr == null || personsStr.isBlank()) {
            return Collections.emptySet();
        }
        Set<Person> personsToAdd = new HashSet<>();
        Set<Person> personsToRemove = new HashSet<>();
        for (String strToken : personsStr.trim().split(",")) {
            strToken = strToken.trim();
            boolean isNeg = isNeg(strToken);
            String personName = parseName(strToken);
            Collection<Person> persons = isGroup(personName)
                    ? getGroupPersons(personName)
                    : List.of(new Person(personName));
            if (isNeg) {
                personsToRemove.addAll(persons);
            } else {
                personsToAdd.addAll(persons);
            }
        }
        personsToAdd.removeAll(personsToRemove);
        return personsToAdd;
    }

    public Set<Person> getGroupPersons(String name) {
        return Optional.ofNullable(groupStorage.get(name)).map(Group::getPersons).orElse(Collections.emptySet());
    }

    public boolean isGroup(String name) {
        return name.equals(name.toUpperCase());
    }

    private boolean isNeg(String name) {
        return name.startsWith("-");
    }

    private String parseName(String name) {
        return name.trim().replaceFirst("^([-+]?)", "");
    }

    public String addToGroup(String groupName, Collection<Person> persons) {
        Group group = groupStorage.get(groupName);
        group.getPersons().addAll(persons);
        groupStorage.put(group);
        return EMPTY_STRING;
    }

    public String removeFromGroup(String groupName, Collection<Person> persons) {
        Group group = groupStorage.get(groupName);
        group.getPersons().removeAll(persons);
        groupStorage.put(group);
        return EMPTY_STRING;
    }

    public Map<Person, Person> secretSantaShuffle(Collection<Person> persons) {
        List<Person> sortedPersons = new ArrayList<>(new TreeSet<>(persons));
        List<Integer> indexes = shuffleSecretSantaIndexes(persons.size());

        LinkedHashMap<Person, Person> result = IntStream.range(0, persons.size())
                .boxed()
                .collect(Collectors.toMap(idx -> sortedPersons.get(idx), idx -> sortedPersons.get(indexes.get(idx)), (a, b) -> a, LinkedHashMap::new));

        return result;
    }

    private List<Integer> shuffleSecretSantaIndexes(int listSize) {
        List<Integer> indexList = IntStream.range(0, listSize).boxed().collect(Collectors.toList());
        if (listSize < 3) {
            Collections.reverse(indexList);
            return indexList;
        }
        int count = 0;
        while (true) {
            count++;
            List<Integer> list = shuffleList(indexList);
            if (checkSecretSantaIndexes(list)) {
                return list;
            }
        }
    }

    private List<Integer> shuffleList(List<Integer> list) {
        Random random = new Random();

        int n = list.size();
        for (int i = 0; i < n; i++) {
            int swapIdx = random.nextInt(n - i) + i;
            swap(list, i, swapIdx);
        }
        return list;
    }

    private void swap(List<Integer> list, int idx1, int idx2) {
        Integer first = list.get(idx1);
        Integer second = list.get(idx2);
        list.set(idx1, second);
        list.set(idx2, first);
    }

    private boolean checkSecretSantaIndexes(List<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            int value = list.get(i);
            final boolean isGiftToSelf = value == i;
            final boolean isGiftLoop = list.get(value) == i;
            if (isGiftToSelf || isGiftLoop) {
                return false;
            }
        }
        return true;
    }

}
