package main;

import conf.Configuration;
import entities.*;
import shared.*;
import java.io.*;

public class AirLift {
    /**
     * Method that implements the whole simulation.
     * @param args The arguments of the method. Not used.
     * @throws IOException
     */
    public static void main(String[]args) throws IOException {
        /** 
         * An instance of GRI. It's used to store general information regarding the simulation.
        */
        GRI repository = new GRI();

        /**
         * An instance of DepartureAirport.
         */
        DepartureAirport departureAirport = new DepartureAirport(repository);
        
        /**
         * An instance of DestinationAirport.
         */
        DestinationAirport destinationAirport = new DestinationAirport(repository);
        
        /**
         * An instance of Plane.
         */
        Plane plane = new Plane(repository);
        
        /**
         * The pilot of the simulation.
         */
        Pilot pilot = new Pilot(departureAirport, destinationAirport, plane);

        /**
         * The passengers of the simulation.
         */
        Passenger[] passengers = new Passenger[Configuration.NUMBER_OF_PASSENGERS];

        /**
         * The hostess of the simulation.
         */
        Hostess hostess = new Hostess(departureAirport, plane);
        
        /**
         * Write the header of the log file in the file.
         */
        repository.logHeader();

        /**
         * For each passengers, instantiate it and start the thread.
         */
        for(int i = 0; i < passengers.length; i++){
            passengers[i] = new Passenger(i, departureAirport, plane);
            passengers[i].start();
        }

        /**
         * Start the hostess thread.
         */
        hostess.start();

        /**
         * Start the pilot thread.
         */
        pilot.start();
        
        /**
         * Join the threads.
         */
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

        /**
         * Log the summary and the legend of the simulation.
         */
        repository.logSummary();
        repository.logLegend();

    }
}