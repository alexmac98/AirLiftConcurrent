package entities;

import states.PassengerState;
import shared.DepartureAirport;
import shared.Plane;

public class Passenger extends Thread{
    /**
     * The ID of the passenger.
     */
    private int ID;

    /**
     * The state of the passenger.
     */
    private PassengerState state;

    /**
     * An instance of DepartureAirport.
     */
    private DepartureAirport departureAirport;

    /**
     * An instance of Plane.
     */
    private Plane plane;

    
    public Passenger(int ID, DepartureAirport departureAirport, Plane plane) {
        this.ID = ID;
        this.departureAirport = departureAirport;
        this.plane = plane;
    }

    /**
     * Method that returns the id of the passenger. 
     * @return The id of the passenger.
     */
    public int getID() {
        return this.ID;
    }

    /**
     * Method that returns the state of the passenger.
     * @return The state of the passenger.
     */
    public PassengerState getPassengerState() {
        return this.state;
    }

    /**
     * Method that sets the state of the passenger.
     * @param state The wanted state for the passenger.
     */
    public void setState(PassengerState state){
        this.state = state;
    }

    /**
     * Method that implements the passenger's life cycle.
     * The life cycle of the passengers is straight forward.
     * Firstly, the passenger travel to the departure airport.
     * Then it waits in queue to check in, and once it's his turn, he shows
     * the hostess his documents. Once the hostess validates his documents, 
     * he then boards the plane, waits for the flight to end and leave the plane.
     */
    @Override
    public void run() {
        this.departureAirport.travelToAirport();
        this.departureAirport.waitInQueue();
        this.departureAirport.showDocuments();
        this.plane.boardThePlane();
        this.plane.waitForEndOfFlight();
        this.plane.leaveThePlane();
    }
}