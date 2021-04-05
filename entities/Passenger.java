package entities;

import states.PassengerState;
import shared.DepartureAirport;
import shared.Plane;

public class Passenger extends Thread{
    private int ID;
    private PassengerState state;
    private DepartureAirport departureAirport;
    private Plane plane;

    public Passenger(int ID, DepartureAirport departureAirport, Plane plane) {
        this.ID = ID;
        this.departureAirport = departureAirport;
        this.plane = plane;
    }

    public int getID() {
        return this.ID;
    }

    public PassengerState getPassengerState() {
        return this.state;
    }

    public void setState(PassengerState state){
        this.state = state;
    }

    @Override
    public void run() {
        // implement life cycle
        this.departureAirport.travelToAirport();
        this.departureAirport.waitInQueue();
        this.departureAirport.showDocuments();
        this.plane.boardThePlane();
        this.plane.waitForEndOfFlight();
        this.plane.leaveThePlane();
    }
}