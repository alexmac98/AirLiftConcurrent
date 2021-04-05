package shared;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import conf.Configuration;
import entities.Passenger;
import entities.Pilot;
import states.PassengerState;
import states.PilotState;
import utils.Log;

public class Plane {
    private ReentrantLock mutex;

    private Condition[] COND_PASSENGERS;
    private Condition COND_PILOT;

    private int passengersBoarded;


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
    }

    /** Passenger Methods */
    public void boardThePlane() {
        Passenger passenger = null;
        try{
            this.mutex.lock();

            passenger = (Passenger) (Thread.currentThread());
            int id = passenger.getID();

            Log.print("Plane", String.format("Passenger %d entered the plane.", id));

            this.passengersBoarded++;

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void waitForEndOfFlight() {
        Passenger passenger = null;
        try{
            this.mutex.lock();

            passenger = (Passenger) (Thread.currentThread());
            passenger.setState(PassengerState.IN_FLIGHT);
            int id = passenger.getID();

            Log.print("Plane", String.format("Passenger %d is waiting for the end of flight.", id));

            this.COND_PASSENGERS[id].await();


        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void leaveThePlane() {
        Passenger passenger = null;

        try{
            this.mutex.lock();
            
            passenger = (Passenger) (Thread.currentThread());
            passenger.setState(PassengerState.AT_DESTINATION);
            int id = passenger.getID();
            
            Log.print("Plane", String.format("Passenger %d left the plane", id));
            this.passengersBoarded--;

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    /** Pilot methods */
    public void waitForAllInBoard() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.WAIT_FOR_BOARDING);
            Log.print("Plane", "Pilot is waiting for all passengers to enter the plane.");
            
            this.COND_PILOT.await();

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void announceArrival() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            Log.print("Plane", "Pilot announced arrival.");
            
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.DEBOARDING);

            for(Condition c : this.COND_PASSENGERS) c.signal();


        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }
    
}