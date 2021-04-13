package entities;

import states.PilotState;
import conf.Configuration;
import shared.DepartureAirport;
import shared.DestinationAirport;
import shared.Plane;

public class Pilot extends Thread{
    private PilotState state;
    private DepartureAirport departureAirport;
    private DestinationAirport destinationAirport;
    private Plane plane;

    public Pilot(DepartureAirport departureAirport,
                 DestinationAirport destinationAirport, 
                 Plane plane) {
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.plane = plane;
    }

    public PilotState getPilotState() {
        return this.state;
    }

    public void setState(PilotState state){
        this.state = state;
    }

    @Override
    public void run() {
        // implement life cycle
        int threshold = 0;
        int passengersInQueue = 0;
        while(true){
            if(this.departureAirport.parkAtTransferGate()) break;
            passengersInQueue = this.departureAirport.informPlaneReadyForBoarding();
            threshold = passengersInQueue >= Configuration.MAX_PASSENGERS_PLANE ? Configuration.MAX_PASSENGERS_PLANE : passengersInQueue;
            this.plane.setExpectedPassengers(threshold);
            this.plane.waitForAllInBoard();
            this.departureAirport.flyToDestinationPoint();
            this.plane.announceArrival();
            this.destinationAirport.flyToDeparturePoint();
            
        }
    }

}