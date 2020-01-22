package pgdp.space;

import java.util.ArrayList;
import java.util.List;

public enum SpaceDispatcher {
    INSTANCE("SpaceGroup");

    private final ThreadGroup threadGroup;
    private final List<Beacon> beacons = new ArrayList<>();

    SpaceDispatcher(String groupName) {
        this.threadGroup = new ThreadGroup(groupName);
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public void lock(Beacon beacon) {
        synchronized (beacons) {
            if (beacons.contains(beacon)) {
                throw new RuntimeException();
            } else {
                beacons.add(beacon);
            }
        }
    }

    public void stopSearch() {
        threadGroup.interrupt();
    }

    public void unlock(Beacon beacon) {
        synchronized (beacons) {
            beacons.remove(beacon);
        }
    }

    public boolean isLock(Beacon beacon) {
        return beacons.contains(beacon);
    }
}
