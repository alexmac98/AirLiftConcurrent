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
        while(true){
            this.departureAirport.prepareForPassBoarding();
            
            do
            {
                this.departureAirport.waitForNextPassenger();
            }while(this.departureAirport.checkDocuments() <= Configuration.NUMBER_OF_PASSENGERS);

            this.departureAirport.informPlaneReadyToTakeOff();
            this.departureAirport.waitForNextFlight();
        }
    }

}