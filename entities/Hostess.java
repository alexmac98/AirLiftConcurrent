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
    
    /**
     * An instance of Plane.
     */
    private Plane plane;

    // constructor
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
     * There are 3 main variables: 
     *  - currentPassengers: counts the number of passengers checked;
     *  - passengersInQueue: the number of passengers currently waiting in the queue;
     *  - threshold: Is the estipulated number of passengers accepted in the current flight.
     * <p></p>
     * The life cycle of the hostess is explained in the following way:
     * while there are flights to be made, the hostess prepares for pass boarding and the passengersInQueue variable
     * is updated; the threshold is updated as the maximum number of passengers per plane if the number of passengers waiting
     * in queue is higher than it, else it's updated as the number of passengers waiting in queue;
     * then, and while the variable currentPassengers is smaller than the defined threshold, the hostess waits for the next
     * passengers and then checks his documents, updating the vaiable currentPassengers.
     * When all the passengers for that flight are checked and boarded, the hostess informs the plane is ready to take off.
     * Reset the currentPassengers variable and reset the current passengers in the departure airport.   
     */
    @Override
    public void run() {
        int currentPassengers = 0;
        int passengersInQueue = 0;
        int threshold = 0;
        while(!this.departureAirport.waitForNextFlight()){
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