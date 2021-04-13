package entities;

import states.PilotState;
import conf.Configuration;
import shared.DepartureAirport;
import shared.DestinationAirport;
import shared.Plane;

public class Pilot extends Thread{

    /**
     * The state of the pilot.
     */
    private PilotState state;

    /**
     * An instance of DepartureAirport.
     */
    private DepartureAirport departureAirport;

    /**
     * An instance of DestinationAirport.
     */
    private DestinationAirport destinationAirport;

    /**
     * An instance of Plane.
     */
    private Plane plane;

    public Pilot(DepartureAirport departureAirport,
                 DestinationAirport destinationAirport, 
                 Plane plane) {
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.plane = plane;
    }

    /**
     * Method that returns the state of the pilot.
     * @return The state of the pilot.
     */
    public PilotState getPilotState() {
        return this.state;
    }

    /**
     * Method that sets the state of the pilot.
     * @param state The wanted state for the pilot.
     */
    public void setState(PilotState state){
        this.state = state;
    }

    /**
     * Method that implements the life cycle of the pilot.
     */
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