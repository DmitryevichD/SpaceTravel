package pgdp.space;

import java.util.ArrayList;
import java.util.List;

import static pgdp.space.ConnectionType.NORMAL;
import static pgdp.space.SpaceDispatcher.DISPATCHER;

public class Spaceuin extends Thread {
    private static int threadInitNumber;

    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    private Beacon start;
    private Beacon destination;
    private FlightRecorder flightRecorder;
    private boolean isDone = false;
    private List<Beacon> pastPoints = new ArrayList<>();

    public Spaceuin(Beacon start, Beacon destination, FlightRecorder flightRecorder) {
        super(DISPATCHER.getThreadGroup(), "Thread-" + nextThreadNum());
        this.start = start;
        this.destination = destination;
        this.flightRecorder = flightRecorder;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void run() {
        while (DISPATCHER.lock(start) && !isDone) {
            arrived(start);
            startTravel(start);
        }
    }

    @Override
    public void interrupt() {
        isDone = true;
    }

    private void startTravel(Beacon start) {
        for (BeaconConnection connection : start.connections()) {

            if (isDone) {
                break;
            }

            Beacon nextBeacon = connection.beacon();

            if (start.equals(nextBeacon)) {
                continue;
            }

            if (pastPoints.contains(nextBeacon)) {
                continue;
            }

            if (connection.type() == NORMAL) {
                pastPoints.add(start);
                goToNextBeacon(start, nextBeacon);
            } else {
                arrived(nextBeacon);
                buildNewBeacon(nextBeacon);
            }

        }
        pastPoints.add(start);
    }

    private void goToNextBeacon(Beacon start, Beacon nextBeacon) {
        while (DISPATCHER.lock(nextBeacon) && !isDone) {
            continue;
        }

        arrived(nextBeacon);
        DISPATCHER.unlock(start);

        if (destination.equals(nextBeacon)) {
            flightRecorder.tellStory();
            DISPATCHER.stopSearch();
        } else {
            startTravel(nextBeacon);
            arrived(start);
        }
    }

    private void buildNewBeacon(Beacon beacon) {
        Spaceuin spaceuin = new Spaceuin(beacon, destination, flightRecorder.createCopy());
        spaceuin.start();
    }

    private void arrived(Beacon beacon) {
        if (!isDone) {
            flightRecorder.recordArrival(beacon);
        }
    }
}
