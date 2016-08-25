package thirdparty.todoist;

import java.util.Comparator;

public class TodoistComparator implements Comparator<TodoistObject> {

    @Override
    public int compare(TodoistObject o1, TodoistObject o2) {
        long utc1 = o1.getUtcDate();
        long utc2 = o2.getUtcDate();
        if (utc1 > utc2) {
            return 1;
        }
        if (utc1 == utc2) {
            return 0;
        }
        return -1;
    }
}
