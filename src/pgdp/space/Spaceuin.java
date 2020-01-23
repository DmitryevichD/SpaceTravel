package pgdp.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pgdp.space.ConnectionType.NORMAL;

public class Spaceuin extends Thread {
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    private Beacon start;
    private Beacon destination;
    private FlightRecorder flightRecorder;
    private Map<Beacon, List<Beacon>> doneBeacon = new HashMap<>();
    private boolean isDone = false;

    public Spaceuin(Beacon start, Beacon destination, FlightRecorder flightRecorder) {
        super(SpaceDispatcher.INSTANCE.getThreadGroup(), "Thread-" + nextThreadNum());
        this.start = start;
        this.destination = destination;
        this.flightRecorder = flightRecorder;
        SpaceDispatcher.INSTANCE.lock(start);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void run() {
        flightRecorder.recordArrival(start);
        startTravel(start);
    }

    @Override
    public void interrupt() {
        isDone = true;
    }

    private void startTravel(Beacon start) {
        doneBeacon.computeIfAbsent(start, doneRoutes -> new ArrayList<>());

        while (doneBeacon.get(start).size() < start.connections().size() && !isDone) {
            for (BeaconConnection connection : start.connections()) {
                Beacon nextBeacon = connection.beacon();

                if (start.equals(nextBeacon)) {
                    doneBeacon.get(start).add(start);
                }

                if (doneBeacon.get(start).contains(nextBeacon)) {
                    continue;
                }

                if (SpaceDispatcher.INSTANCE.isLock(nextBeacon)) {
                    continue;
                }

                try {
                    if (connection.type() == NORMAL) {
                        goToNextBeacon(start, nextBeacon);
                    } else {
                        buildNewBeacon(nextBeacon);
                    }
                    doneBeacon.get(start).add(nextBeacon);
                } catch (Exception ex) {
                    // exception when lock Beacon
                }
            }
        }
    }

    private void goToNextBeacon(Beacon start, Beacon nextBeacon) {
        try {
            SpaceDispatcher.INSTANCE.lock(nextBeacon);
            flightRecorder.recordDeparture(start);
            flightRecorder.recordArrival(nextBeacon);
            SpaceDispatcher.INSTANCE.unlock(start);

            if (destination.equals(nextBeacon)) {
                flightRecorder.tellStory();
                SpaceDispatcher.INSTANCE.stopSearch();
            } else {
                startTravel(nextBeacon);
            }
        } catch (Exception ex) {
            // exception when lock Beacon
        }
    }

    private void buildNewBeacon(Beacon beacon) {
        Spaceuin spaceuin = new Spaceuin(beacon, destination, flightRecorder.createCopy());
        spaceuin.start();
    }
}
