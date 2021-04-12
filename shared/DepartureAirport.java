package shared;

import entities.Hostess;
import entities.Passenger;
import states.HostessState;
import states.PassengerState;
import conf.Configuration;
import utils.Log;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.Random;

public class DepartureAirport{
    
    private ReentrantLock mutex;

    private Condition[] COND_PASSENGERS;
    private Condition COND_HOSTESS;
    private Condition COND_PILOT;
    private Condition COND_INITIAL_SYNC;

    private GRI repository;

    private Queue<Passenger> passengersQueue;

    private int checkedPassengers;

    public DepartureAirport(GRI repository) {
        this.repository = repository;
        this.mutex = new ReentrantLock();
        this.passengersQueue = new LinkedList<Passenger>();

        this.COND_PASSENGERS = new Condition[Configuration.NUMBER_OF_PASSENGERS];
        for(int i = 0; i < this.COND_PASSENGERS.length; i++){
            this.COND_PASSENGERS[i] = this.mutex.newCondition();
        }
        this.COND_HOSTESS = this.mutex.newCondition();
        this.COND_PILOT = this.mutex.newCondition();
        this.COND_INITIAL_SYNC = this.mutex.newCondition();
        this.checkedPassengers = 0;
    }

    /** Hostess Methods */
    public void prepareForPassBoarding() {
        try{
            this.mutex.lock();
            
            Log.print("InitialSync", "Hostess is waiting for initial synchronization to be completed.");
            this.COND_INITIAL_SYNC.await();

            Log.print("DepartureAirport", "Hostess is waiting for pilot signal.");
            
            this.COND_HOSTESS.await();
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public int checkDocuments() {
        Hostess hostess = null;
        try{
            this.mutex.lock();

            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.CHECK_PASSENGER);
            
            Passenger firstPassenger = this.passengersQueue.remove();
            int id = firstPassenger.getID();

            Log.print("DepartureAirport", String.format("Hostess is checking documents of passenger %d.", id));
            this.COND_PASSENGERS[id].signal();

            this.checkedPassengers++;

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }

        return this.checkedPassengers;
    }

    public void waitForNextPassenger() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
            
            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.WAIT_FOR_PASSENGER);
            
            Passenger firstPassenger = this.passengersQueue.remove();
            int id = firstPassenger.getID();

            this.COND_PASSENGERS[id].signal();
            Log.print("DepartureAirport", String.format("Hostess is waiting for passenger %d.", id));
            this.COND_HOSTESS.await();
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void informPlaneReadyToTakeOff() {
        Hostess hostess = null;
        try{
            this.mutex.lock();

            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.READY_TO_FLY);
            
            Log.print("DepartureAirport", "Hostess informs plane is ready to take off.");

            this.COND_PILOT.signal();

            this.checkedPassengers = 0;

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void waitForNextFlight() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
            
            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.READY_TO_FLY);

            Log.print("DepartureAirport", "Hostess is waiting for next flight");

            this.COND_HOSTESS.await();

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    /** Passenger Methods */
    public void travelToAirport() {
        Passenger passenger = null;
        try{
            this.mutex.lock();

            passenger = (Passenger) (Thread.currentThread());
            passenger.setState(PassengerState.GOING_TO_AIRPORT);
            int id = passenger.getID();
            
            Log.print("DepartureAirport", String.format("Passenger %d is traveling to airport.", id));

            this.COND_PASSENGERS[id].await(new Random().nextInt(2), TimeUnit.SECONDS);
            

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void waitInQueue() {
        Passenger passenger = null;
        try{
            this.mutex.lock();
            
            passenger = (Passenger) (Thread.currentThread());
            passenger.setState(PassengerState.IN_QUEUE);
            int id = passenger.getID();
            this.passengersQueue.add(passenger);
            
            Log.print("DepartureAirport", String.format("Passenger %d is waiting in queue.", id));
            
            if(this.passengersQueue.size() == Configuration.NUMBER_OF_PASSENGERS){
                Log.print("InitialSync", "All the passengers are waiting in queue. Initial Synchronization completed.");
                this.COND_INITIAL_SYNC.signalAll();
            }

            // sleep
            this.COND_PASSENGERS[id].await();

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void showDocuments() {
        Passenger passenger = null;

        try{
            this.mutex.lock();

            passenger = (Passenger) (Thread.currentThread());
            int id = passenger.getID();

            Log.print("DepartureAirport", String.format("Passenger %d is showing his documents to Hostess.", id));
            this.COND_HOSTESS.signal();
            this.COND_PASSENGERS[id].await();
            
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    /** Pilot Methods */
    public void informPlaneReadyForBoarding() {
        try{
            this.mutex.lock();

            Log.print("InitialSync", "Pilot is waiting for initial synchronization to be completed.");
            this.COND_INITIAL_SYNC.await();
            
            Log.print("DepartureAirport", "Pilot informs Hostess that plane is ready for boarding.");

            this.COND_HOSTESS.signal();

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    /** Pilot Methods */
    public void flyToDestinationPoint() {
        try{
            this.mutex.lock();
            
            Log.print("DepartureAirport", "Pilot started flight to destination airport.");

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void parkAtTransferGate() {
        try{
            this.mutex.lock();

            Log.print("DepartureAirport", "Pilot parking at transfer gate.");
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }
    
}