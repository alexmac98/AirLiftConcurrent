package entities;

import states.HostessState;
import conf.Configuration;
import shared.DepartureAirport;
import shared.Plane;

public class Hostess extends Thread{
    /**
     * The state of the hostess.
     */
    private HostessState state;

    /**
     * An instance of DepartureAirport.
     */
    private DepartureAirport departureAirport;
    
    private Plane plane;

    public Hostess(DepartureAirport departureAirport, Plane plane) {
        this.departureAirport = departureAirport;
        this.plane = plane;
    }

    /**
     * Method that returns the current state of the hostess.
     * @return The current state of the hostess.
     */
    public HostessState getHostessState() {
        return this.state;
    }

    /**
     * Method that sets the current state of the hostess.
     * @param state The wanted state for the hostess.
     */
    public void setState(HostessState state){
        this.state = state;
    }

    /**
     * Method that implements the defined life cycle of the hostess.
     */
    @Override
    public void run() {
        // implement life cycle
        int currentPassengers = 0;
        int passengersInQueue = 0;
        // The threshold for the number of passengers accepted in the plane.
        int threshold = 0;
        while(true){
            if(this.departureAirport.waitForNextFlight()) break;
            passengersInQueue = this.departureAirport.prepareForPassBoarding();
            threshold = passengersInQueue >= Configuration.MAX_PASSENGERS_PLANE ? Configuration.MAX_PASSENGERS_PLANE : passengersInQueue;
            while(currentPassengers < threshold){
                this.departureAirport.waitForNextPassenger();
                currentPassengers = this.departureAirport.checkDocuments();
            }
            this.plane.informPlaneReadyToTakeOff();
            currentPassengers = 0;
            this.departureAirport.setCurrentPassengers(currentPassengers);
        }
    }
}