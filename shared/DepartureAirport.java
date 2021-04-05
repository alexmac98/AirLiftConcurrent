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

    private GRI repository;

    private Queue<Passenger> passengersQueue;


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

    }

    

    /** Hostess Methods */
    public void prepareForPassBoarding() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
            
            // sleep 
            this.COND_HOSTESS.await();
            // wake up
            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.WAIT_FOR_PASSENGER);
            
            // signal first passenger
            // who is the first passenger?
            Passenger firstPassenger = this.passengersQueue.peek();
            int id = firstPassenger.getID();
            
            this.COND_PASSENGERS[id].signal();

            Log.print("DepartureAirport", String.format("Hostess signaled the first passenger (%d) waiting in queue.", id));


        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void checkDocuments() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
            
            
            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.CHECK_PASSENGER);

            Passenger firstPassenger = this.passengersQueue.remove();
            int id = firstPassenger.getID();

            Log.print("DepartureAirport", String.format("Hostess checked documents of passenger %d.", id));

        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
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
            Log.print("DepartureAirport", String.format("Hostess waiting for passenger %d.", id));
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

            this.passengersQueue.add(passenger);
            
            // sleep
            this.COND_PASSENGERS[passenger.getID()].await();

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

            Log.print("DepartureAirport", String.format("Passenger %d showing documents.", id));

            this.COND_HOSTESS.signal();
            
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    /** Pilot Methods */
    public void informPlaneReadyForBoarding() {
        try{
            this.mutex.lock();
            
            Log.print("DepartureAirport", "Pilot informs plane is ready for boarding.");

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
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    public void parkAtTransferGate() {
        try{
            this.mutex.lock();
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }
    
}