package main;

import conf.Configuration;
import entities.*;
import shared.*;

public class AirLift {
    public static void main(String[]args){
        GRI repository = new GRI();
        DepartureAirport departureAirport = new DepartureAirport(repository);
        DestinationAirport destinationAirport = new DestinationAirport(repository);
        Plane plane = new Plane(repository);

        Pilot pilot = new Pilot(departureAirport, destinationAirport, plane);
        Passenger[] passengers = new Passenger[Configuration.NUMBER_OF_PASSENGERS];
        Hostess hostess = new Hostess(departureAirport);
        

        for(int i = 0; i < passengers.length; i++){
            passengers[i] = new Passenger(i, departureAirport, plane);
            passengers[i].start();
        }
        hostess.start();
        pilot.start();
        
        
        
        try{
            hostess.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        
        try{
            pilot.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        

        for(Passenger p : passengers) {
            try{
                p.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        


    }
}