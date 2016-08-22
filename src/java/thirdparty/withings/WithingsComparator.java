package thirdparty.withings;

import java.util.Comparator;

public class WithingsComparator
        implements Comparator<WithingsObject> {

    @Override
    public int compare(WithingsObject o1, WithingsObject o2) {
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
