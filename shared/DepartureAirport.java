package shared;

import entities.Hostess;
import entities.Passenger;
import entities.Pilot;
import states.HostessState;
import states.PassengerState;
import states.PilotState;
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
    private Condition COND_INITIAL_SYNC_PILOT;
    private Condition COND_INITIAL_SYNC_HOSTESS;

    private GRI repository;

    private Queue<Passenger> passengersQueue;

    private int checkedPassengers;

    private int currentPassengers;

    private boolean INITIAL_SYNC_COMPLETED;

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
        this.COND_INITIAL_SYNC_HOSTESS = this.mutex.newCondition();
        this.COND_INITIAL_SYNC_PILOT = this.mutex.newCondition();
        this.checkedPassengers = 0;
        this.currentPassengers = 0;
        this.INITIAL_SYNC_COMPLETED = false;
    }

    /** Hostess Methods */
    public boolean waitForNextFlight() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
            Log.print("Debug", "PFPB - Checked Passengers: " + this.checkedPassengers);
            if(this.checkedPassengers == Configuration.NUMBER_OF_PASSENGERS) return true;

            Log.print("InitialSync", "Hostess is waiting for initial synchronization to be completed.");
            if(!this.INITIAL_SYNC_COMPLETED) this.COND_INITIAL_SYNC_HOSTESS.await();
            
            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.WAIT_FOR_NEXT_FLIGHT);
            this.repository.setHostessState(HostessState.WAIT_FOR_NEXT_FLIGHT);
            this.repository.logStatus();

            Log.print("DepartureAirport", "Hostess is waiting for next flight");

            this.COND_INITIAL_SYNC_PILOT.signal();
            this.COND_HOSTESS.await();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }

        return false;
    }
    public int prepareForPassBoarding() {
        try{
            this.mutex.lock();
            Log.print("DepartureAirport", "Hostess is preparing for pass boarding.");
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
        return this.passengersQueue.size();
    }

    public int checkDocuments() {
        Hostess hostess = null;
        try{
            this.mutex.lock();

            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.CHECK_PASSENGER);
            this.repository.setHostessState(HostessState.CHECK_PASSENGER);
            this.repository.logStatus();
            
            Passenger firstPassenger = this.passengersQueue.remove();
            int id = firstPassenger.getID();

            Log.print("DepartureAirport", String.format("Hostess is checking documents of passenger %d.", id));
            this.COND_PASSENGERS[id].signal();

            this.checkedPassengers++;
            this.currentPassengers++;

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
        return this.currentPassengers;
    }

    public void waitForNextPassenger() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
            
            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.WAIT_FOR_PASSENGER);
            this.repository.setHostessState(HostessState.WAIT_FOR_PASSENGER);
            this.repository.logStatus();
            
            Passenger firstPassenger = this.passengersQueue.peek();
            int id = firstPassenger.getID();

            this.COND_PASSENGERS[id].signal();
            Log.print("DepartureAirport", String.format("Hostess is waiting for passenger %d.", id));
            this.COND_HOSTESS.await();
        }catch(Exception e){
            e.printStackTrace();
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
            this.repository.setHostessState(HostessState.READY_TO_FLY);
            this.repository.logStatus();

            Log.print("DepartureAirport", "Hostess informs plane is ready to take off.");
            this.COND_PILOT.signal();

            this.currentPassengers = 0;

        }catch(Exception e){
            e.printStackTrace();
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
            this.repository.setPassengerState(id, PassengerState.GOING_TO_AIRPORT);
            this.repository.logStatus();
            
            Log.print("DepartureAirport", String.format("Passenger %d is traveling to airport.", id));

            this.COND_PASSENGERS[id].await(new Random().nextInt(2), TimeUnit.SECONDS);
            

        }catch(Exception e){
            e.printStackTrace();
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
            this.repository.setPassengerState(id, PassengerState.IN_QUEUE);
            this.repository.logStatus();

            this.passengersQueue.add(passenger);
            this.repository.setInQ(this.passengersQueue.size());

            Log.print("DepartureAirport", String.format("Passenger %d is waiting in queue.", id));
            
            if(this.passengersQueue.size() == Configuration.NUMBER_OF_PASSENGERS){
                Log.print("InitialSync", "All the passengers are waiting in queue. Initial Synchronization completed.");
                this.COND_INITIAL_SYNC_HOSTESS.signal();
                this.INITIAL_SYNC_COMPLETED = true;
            }

            // sleep
            this.COND_PASSENGERS[id].await();

        }catch(Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /** Pilot Methods */
    public boolean parkAtTransferGate() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.AT_TRANSFER_GATE);
            this.repository.setPilotState(PilotState.AT_TRANSFER_GATE);
            this.repository.logStatus();

            Log.print("Debug", "PAT - Checked Passengers: " + this.checkedPassengers);
            if(this.checkedPassengers == Configuration.NUMBER_OF_PASSENGERS) return true;
            Log.print("InitialSync", "Pilot is waiting for initial synchronization to be completed.");
            if(!this.INITIAL_SYNC_COMPLETED) this.COND_INITIAL_SYNC_PILOT.await();

            this.repository.setFlightNumber();
            Log.print("DepartureAirport", "Pilot parking at transfer gate.");

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }

        return false;
    }

    public int informPlaneReadyForBoarding() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.READY_FOR_BOARDING);
            this.repository.setPilotState(PilotState.READY_FOR_BOARDING);
            this.repository.logStatus();
            Log.print("DepartureAirport", "Pilot informs Hostess that plane is ready for boarding.");
            this.COND_HOSTESS.signal();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
        return this.passengersQueue.size();
    }

    /** Pilot Methods */
    public void flyToDestinationPoint() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.FLYING_FORWARD);
            this.repository.setPilotState(PilotState.FLYING_FORWARD);
            this.repository.logStatus();
            
            Log.print("DepartureAirport", "Pilot started flight to destination airport.");

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    
    
}