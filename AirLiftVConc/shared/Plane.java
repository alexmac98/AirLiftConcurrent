package shared;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import conf.Configuration;
import entities.Passenger;
import entities.Pilot;
import entities.Hostess;
import states.PassengerState;
import states.PilotState;
import states.HostessState;
import states.Event;
import utils.Log;

public class Plane {
    private ReentrantLock mutex;
    private Condition[] COND_PASSENGERS;
    private Condition COND_PILOT;
    private Condition COND_HOSTESS;
    private int passengersBoarded;
    private int expectedPassengers;
    private GRI repository;

    public Plane(GRI repository) {
        this.repository = repository;
        this.passengersBoarded = 0;
        this.mutex = new ReentrantLock();
        this.COND_PASSENGERS = new Condition[Configuration.NUMBER_OF_PASSENGERS];
        for(int i = 0; i < this.COND_PASSENGERS.length; i++){
            this.COND_PASSENGERS[i] = this.mutex.newCondition();
        }
        this.COND_PILOT = this.mutex.newCondition();
        this.COND_HOSTESS = this.mutex.newCondition();
    }

    // Set Methods
    /**
     * Method that sets the expected number of passenger for the current flight.
     * @param expectedPassengers The expected number of passengers for the flight.
     */
    public void setExpectedPassengers(int expectedPassengers) {
        this.expectedPassengers = expectedPassengers;
    }

    // Passenger Methods
    /**
     * Method that mimics the passenger boarding the plane.
     * The repository updates the number of passengers in the current plane. 
     * If all the expected passengers are boarded, signal the hostess to inform the pilot.
     * The repository updates the number of passengers per flight.
     */
    public void boardThePlane() {
        Passenger passenger = null;
        try{
            this.mutex.lock();

            passenger = (Passenger) (Thread.currentThread());
            int id = passenger.getID();

            Log.print("Plane", String.format("Passenger %d entered the plane.", id));

            this.passengersBoarded++;

            this.repository.setInF(this.passengersBoarded);

            if(this.passengersBoarded == this.expectedPassengers) {
                while(!this.mutex.hasWaiters(this.COND_HOSTESS)) this.COND_PASSENGERS[id].await(500, TimeUnit.MILLISECONDS); 
                this.COND_HOSTESS.signal();
                this.repository.setPassengersPerFlight(this.expectedPassengers);
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that mimics the passenger waiting for the end of the flight.
     * The repository updates the state of the respective passenger to IN_FLIGHT.
     */
    public void waitForEndOfFlight() {
        Passenger passenger = null;
        try{
            this.mutex.lock();

            passenger = (Passenger) (Thread.currentThread());
            passenger.setState(PassengerState.IN_FLIGHT);
            int id = passenger.getID();
            this.repository.setPassengerState(id, PassengerState.IN_FLIGHT);
            this.repository.logStatus();

            Log.print("Plane", String.format("Passenger %d is waiting for the end of flight.", id));

            this.COND_PASSENGERS[id].await();


        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that mimics the passenger leaving the plane.
     * The repository updates the respective passenger's state to AT_DESTINATION, the number 
     * of passengers in the current flight and the total number of passengers that arrived aat the destination.
     * The last passenger signals the pilot.
     */
    public void leaveThePlane() {
        Passenger passenger = null;

        try{
            this.mutex.lock();
            
            passenger = (Passenger) (Thread.currentThread());
            passenger.setState(PassengerState.AT_DESTINATION);
            int id = passenger.getID();
            this.repository.setPassengerState(id, PassengerState.AT_DESTINATION);
            
            Log.print("Plane", String.format("Passenger %d left the plane", id));
            this.passengersBoarded--;
            this.repository.setInF(this.passengersBoarded);
            this.repository.setPTAL();
            this.repository.logStatus();

            if(this.passengersBoarded == 0) {
                Log.print("Plane", String.format("The plane is now empty. Pilot is signaled.", id));
                this.COND_PILOT.signal();
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    // Pilot Methods
    /**
     * Method that mimics the pilot waiting for all the expected passengers to enter the plane.
     * The repository updates the pilot state to WAIT_FOR_BOARDING.
     * The pilot awaits.
     */
    public void waitForAllInBoard() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.WAIT_FOR_BOARDING);
            this.repository.setPilotState(PilotState.WAIT_FOR_BOARDING);
            this.repository.logStatus();

            Log.print("Plane", "Pilot is now waiting for the passengers to enter the plane.");
            
            this.COND_PILOT.await();
            
            this.repository.logEvent(Event.DEPARTED, this.passengersBoarded);

        }catch(Exception e){
            e.printStackTrace();       
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that mimics the pilot announcing the arrival.
     * The repository updates the pilot state to DEBOARDING. 
     * The pilot wakes up every passenger and waits for them all to exit the plane.
     */
    public void announceArrival() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            Log.print("Plane", "Pilot announced arrival to all the passengers.");
            this.repository.logEvent(Event.ARRIVED, -1);
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.DEBOARDING);
            this.repository.setPilotState(PilotState.DEBOARDING);
            
            this.repository.logStatus();

            for(Condition c : this.COND_PASSENGERS) c.signal();

            Log.print("Plane", "Pilot is now waiting for the passengers to leave the plane.");
            this.COND_PILOT.await();


        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    // Hostess methods
    /**
     * Method that mimics the hostess informing the pilot that the plane is ready to take off.
     * The hostess waits for all the passengers to enter the plane.
     * The repository updates the hostess state to READY_TO_FLY. The hostess signals the pilot to take off.
     */
    public void informPlaneReadyToTakeOff() {
        Hostess hostess = null;
        try{
            this.mutex.lock();

            Log.print("Plane", "Hostess is waiting for all the passengers to enter the plane");
            this.COND_HOSTESS.await();

            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.READY_TO_FLY);
            this.repository.setHostessState(HostessState.READY_TO_FLY);
            this.repository.logStatus();
            
            Log.print("Plane", "Hostess informs plane is ready to take off.");
            this.COND_PILOT.signal();

            // this.currentPassengers = 0;

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }
    
}