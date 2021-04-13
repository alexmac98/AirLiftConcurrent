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
        int checked = 0;
        while(true){
            if(this.departureAirport.waitForNextFlight()) break;
            this.departureAirport.prepareForPassBoarding();
            while(checked < Configuration.NUMBER_OF_PASSENGERS){
                this.departureAirport.waitForNextPassenger();
                checked = this.departureAirport.checkDocuments();
            }
            this.departureAirport.informPlaneReadyToTakeOff();
        }
    }

}