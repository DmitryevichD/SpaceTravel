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

    private final Beacon startBeacon;
    private final Beacon destinationBeacon;
    private final FlightRecorder flightRecorder;
    //Beacons, where was Pingu
    private final List<Beacon> pastBeacons = new ArrayList<>();
    //Current beacon, when blocked the Pingu
    private Beacon currentLockBeacon;

    public Spaceuin(Beacon startBeacon, Beacon destinationBeacon, FlightRecorder flightRecorder) {
        super(DISPATCHER.getThreadGroup(), "Thread-" + nextThreadNum());
        this.startBeacon = startBeacon;
        this.destinationBeacon = destinationBeacon;
        this.flightRecorder = flightRecorder;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void run() {
        while (!isInterrupted() ) {
            try {
                currentLockBeacon = DISPATCHER.lockBeacon(startBeacon);
                arrivedMsg(startBeacon);
                startTravel(startBeacon);
                break;
            } catch (Exception ex) {
                //lock beacon is failed, try again, until interrupted
            }
        }
    }

    @Override
    public void interrupt() {
        //when thread is interrupt - unlock current beacon
        DISPATCHER.unlockBeacon(currentLockBeacon);
        super.interrupt();
    }

    private void startTravel(Beacon start) {
        for (BeaconConnection connection : start.connections()) {

            //check interrupted
            if (isInterrupted()) {
                break;
            }

            Beacon nextBeacon = connection.beacon();

            //check equals start and next beacon
            if (start.equals(nextBeacon)) {
                continue;
            }

            //check beacon where was Pingu
            if (pastBeacons.contains(nextBeacon)) {
                continue;
            }

            if (connection.type() == NORMAL) {
                pastBeacons.add(start); //mark start beacon as past and go to next beacon
                goToNextBeacon(start, nextBeacon);
            } else {
                buildNewBeacon(nextBeacon);
            }

        }

        //when all connected beacon is done, mart current beacon as past, unlock him and go back
        pastBeacons.add(start);
        departureMsg(start);
        DISPATCHER.unlockBeacon(start);
    }

    private void goToNextBeacon(Beacon start, Beacon nextBeacon) {
        //unlock current beacon and go to next beacon
        DISPATCHER.unlockBeacon(start);
        departureMsg(start);

        while (!isInterrupted()) {
            try {
                currentLockBeacon = DISPATCHER.lockBeacon(nextBeacon);
                break;
            } catch (Exception ex) {
                //lock beacon is failed, try again, until interrupted
            }
        }

        arrivedMsg(nextBeacon);

        if (destinationBeacon.equals(nextBeacon)) {
            if (!isInterrupted()) {
                DISPATCHER.stopSearch();
                flightRecorder.tellStory();
                DISPATCHER.unlockBeacon(nextBeacon);
            }
        } else {
            startTravel(nextBeacon);
            arrivedMsg(start);
        }
    }

    private void buildNewBeacon(Beacon beacon) {
        Spaceuin spaceuin = new Spaceuin(beacon, destinationBeacon, flightRecorder.createCopy());
        spaceuin.start();
    }

    //record message until interrupt
    private void arrivedMsg(Beacon beacon) {
        if (!isInterrupted()) {
            flightRecorder.recordArrival(beacon);
        }
    }

    //record message until interrupt
    private void departureMsg(Beacon beacon) {
        if (!isInterrupted()) {
            flightRecorder.recordDeparture(beacon);
        }
    }
}
