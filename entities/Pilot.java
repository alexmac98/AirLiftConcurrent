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
        while(true){
            this.departureAirport.informPlaneReadyForBoarding();
            this.plane.setExpectedPassengers(Configuration.NUMBER_OF_PASSENGERS);
            this.plane.waitForAllInBoard();
            this.departureAirport.flyToDestinationPoint();
            this.plane.announceArrival();
            this.destinationAirport.flyToDeparturePoint();
            this.departureAirport.parkAtTransferGate();
            
        }
    }

}