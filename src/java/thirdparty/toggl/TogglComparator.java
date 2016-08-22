package thirdparty.toggl;

import java.util.Comparator;

public class TogglComparator
        implements Comparator<TogglObject> {

    @Override
    public int compare(TogglObject o1, TogglObject o2) {
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
