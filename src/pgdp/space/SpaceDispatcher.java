package pgdp.space;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread safe singleton for sync threads.
 */
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

    public Beacon lockBeacon(Beacon beacon) {
        //sync locks by object beacon
        synchronized (beacons) {
            if (beacons.contains(beacon)){
                throw new RuntimeException();
            } else {
                beacons.add(beacon);
                return beacon;
            }
        }
    }

    public void stopSearch() {
        //sync locks by object beacon
        synchronized (beacons) {
            //stop all thread
            threadGroup.interrupt();
            //clean all lock
            beacons.clear();
        }
    }

    public void unlockBeacon(Beacon beacon) {
        //sync locks by object beacon
        synchronized (beacons) {
            beacons.remove(beacon);
        }
    }
}
