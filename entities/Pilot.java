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
     * There are 2 main variables:
     *  - threshold: Is the estipulated number of passengers accepted in the current flight;
     *  - passengersInQueue: the number of passengers currently waiting in the queue;
     * The life cycle of the pilot is explained in the following way:
     * While there are passengers to be transported, park at transfer gate and inform plane is ready
     * for boarding, and update passengersInQueue variable. Set the threshold variable to the max number
     * of passengers allowed per flight if the number of passengers in queue is bigger than the max number of 
     * allowed passengers, else set the threshold to the passengers waiting in queue.
     * Set the expected passengers to enter the plane, then the pilot waits for all to board. 
     * Once all the expected passengers are inside the plane, the pilot flies the plane to the destination
     * and announces it's arrival once it lands and parks. Then, it waits for all the passengers to leave the 
     * plane and then flies it back to the departure point. 
     */
    @Override
    public void run() {
        int threshold = 0;
        int passengersInQueue = 0;
        while(!this.departureAirport.parkAtTransferGate()){
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