package pgdp.space;

import java.util.ArrayList;
import java.util.List;

public enum SpaceDispatcher {
    DISPATCHER("SpaceGroup");

    private final ThreadGroup threadGroup;
    private final List<Beacon> beacons = new ArrayList<>();

    SpaceDispatcher(String groupName) {
        this.threadGroup = new ThreadGroup(groupName);
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public boolean lock(Beacon beacon) {
        synchronized (beacons) {
            if (beacons.contains(beacon)) {
                return false;
            } else {
                beacons.add(beacon);
                return true;
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
}
