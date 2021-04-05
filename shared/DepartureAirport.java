package shared;

import java.util.Queue;
import java.util.LinkedList;

import entities.Passenger;

public class DepartureAirport{
    
    private Queue<Passenger> passengersQueue;


    public DepartureAirport() {
        this.passengersQueue = new LinkedList<Passenger>();
    }

    /** Hostess Methods */
    public void prepareForPassBoarding() {
    }

    public void checkDocuments() {
    }

    public void waitForNextPassenger() {
    }

    public void informPlaneReadyToTakeOff() {
    }

    public void waitForNextFlight() {
    }

    /** Passenger Methods */
    public void travelToAirport() {
    }

    public void waitInQueue() {
    }

    public void showDocuments() {
    }

    /** Pilot Methods */
    public void informPlaneReadyForBoarding() {
    }

    public void flyToDeparturePoint() {
    }

    public void parkAtTransferGate() {
    }
    
}