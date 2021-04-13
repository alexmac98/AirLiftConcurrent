package entities;

import states.HostessState;
import conf.Configuration;
import shared.DepartureAirport;

public class Hostess extends Thread{
    private HostessState state;
    private DepartureAirport departureAirport;

    public Hostess(DepartureAirport departureAirport) {
        this.departureAirport = departureAirport;
    }

    public HostessState getHostessState() {
        return this.state;
    }

    public void setState(HostessState state){
        this.state = state;
    }

    @Override
    public void run() {
        // implement life cycle
        int currentPassengers = 0;
        int passengersInQueue = 0;
        int threshold = 0;
        while(true){
            if(this.departureAirport.waitForNextFlight()) break;
            passengersInQueue = this.departureAirport.prepareForPassBoarding();
            threshold = passengersInQueue >= Configuration.MAX_PASSENGERS_PLANE ? Configuration.MAX_PASSENGERS_PLANE : passengersInQueue;
            while(currentPassengers < threshold){
                this.departureAirport.waitForNextPassenger();
                currentPassengers = this.departureAirport.checkDocuments();
            }
            this.departureAirport.informPlaneReadyToTakeOff();
            currentPassengers = 0;
        }
    }
}