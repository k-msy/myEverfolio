package thirdparty.zaim;

import java.util.Comparator;

public class ZaimComparator implements Comparator<ZaimObject> {

    @Override
    public int compare(ZaimObject o1, ZaimObject o2) {
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
