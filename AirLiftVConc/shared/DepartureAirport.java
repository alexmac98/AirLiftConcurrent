package shared;

import entities.Hostess;
import entities.Passenger;
import entities.Pilot;
import states.HostessState;
import states.PassengerState;
import states.PilotState;
import states.Event;
import conf.Configuration;
import utils.Log;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.Random;

public class DepartureAirport{
    
    /**
     * The mutex of the shared region.
     */
    private ReentrantLock mutex;

    /**
     * List of conditions to handle the passengers.
     */
    private Condition[] COND_PASSENGERS;

    /**
     * Condition to handle the hostess.
     */
    private Condition COND_HOSTESS;

    /**
     * Condition to handle the pilot.
     */
    private Condition COND_PILOT;

    /**
     * Condition to handle the pilot in the synching phase.
     */
    private Condition COND_INITIAL_SYNC_PILOT;

    /**
     * Condition to handle the hostess in the synching phase.
     */
    private Condition COND_INITIAL_SYNC_HOSTESS;

    /**
     * The repository
     */
    private GRI repository;

    /**
     * The queue of passengers waiting.
     */
    private Queue<Passenger> passengersQueue;

    /**
     * Total number of passengers that are checked.
     */
    private int checkedPassengers;

    /**
     * Number of passengers dealt for the present flight.
     */
    private int currentPassengers;

    /**
     * Just a flag to signal when the initial sync is completed.
     */
    private boolean INITIAL_SYNC_COMPLETED;

    // constructor
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

    /**
     * Method that sets the number of current passengers dealt
     * for the present flight. 
     * @param currentPassengers The number of current passengers.
     */
    public void setCurrentPassengers(int currentPassengers){
        try{
            this.mutex.lock();
            this.currentPassengers = currentPassengers;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    // Hostess Methods
    
    /**
     * Method that mimics the hostess waiting for the next flight.
     * THe initial synching of the hostess happens here. Once the initial sync is completed, the hostess wakes up
     * and starts working. The repository updates the current hostess state to WAIT_FOR_NEXT_FLIGHT, and the hostess
     * signals the pilot that she's ready to work and then waits for further instructions.
     * @return A flag that mentions if the day of the hostess is over or not. If the checked passengers is equal to the number
     * of passengers in the simulation, the day is over.
     */
    public boolean waitForNextFlight() {
        Hostess hostess = null;
        try{
            this.mutex.lock();
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

    /**
     * Method that mimics the hostess preparing for pass boarding.
     * @return The number of passengers currently waiting in queue.
     */
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

    /**
     * Method that mimics the hostess checking a passenger's documents.
     * The repository updates the state of the hostess to CHECK_PASSENGER and the 
     * number of passengers waiting in queue. The hostess signals the first passenger
     * in the queue, that is currently waiting for her to check his documents.
     */
    public int checkDocuments() {
        Hostess hostess = null;
        try{
            this.mutex.lock();

            hostess = (Hostess) (Thread.currentThread());
            hostess.setState(HostessState.CHECK_PASSENGER);
            
            Passenger firstPassenger = this.passengersQueue.remove();
            int id = firstPassenger.getID();

            this.repository.logEvent(Event.PASSENGER_CHECKED, id);
            this.repository.setHostessState(HostessState.CHECK_PASSENGER);
            this.repository.setInQ(this.passengersQueue.size());
            this.repository.logStatus();
            

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

    /**
     * Method that mimics the hostess waiting for the next passenger in queue to arrive to the balcony.
     * The repository updates the state of the Hostess to WAIT_FOR_PASSENGER. The hostess signals the next
     * passenger in queue to come. The hostess waits for him to show her his documents.
     */
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

    // Passenger methods
    /**
     * Method that mimics the passenger travelling to airport.
     * The repository updates the respective passenger's state to GOING_TO_AIRPORT. 
     * The passenger takes random time to get to the airport.
     */
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

    /**
     * Method that mimics the passenger waiting in queue.
     * The repository updates the respective passenger's state to IN_QUEUE.
     * The passenger is added to the queue and the repository updates the respective
     * number of passengers waiting.
     * If all the passengers of the simulation are waiting in queue, the initial synchronization 
     * is completed and the hostess is woken up to start working.
     * The passenger then waits.
     */
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

            this.COND_PASSENGERS[id].await();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that mimics the passenger showing the documents to the hostess.
     * The passenger signals the hostess to check his documents. He then awaits for 
     * the hostess to finish checking them.
     */
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

    // Pilot Methods
    /**
     * Method that mimics the pilot parking the plane at the transfer gate.
     * The repository updates the state of the pilot to AT_TRANSFER_GATE and the flight number.
     * If the initial synchronization was not completed, the pilot waits.
     * @return A flag that mentions if the work day of the pilot is over or not. If all the passengers of the simulation were dealt with, 
     * the work day is over.
     */
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

    /**
     * Method that mimics the pilot announcing that the plane is ready for boarding.
     * The repository updates the pilot state to READY_FOR_BOARDING. The pilot signals the hostess
     * to start the boarding.
     * @return The number of passengers currently waiting in queue.
     */
    public int informPlaneReadyForBoarding() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            this.repository.logEvent(Event.BOARDING_STARTED, -1);
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

    /**
     * Method that mimics the pilot flying to the destination point.
     * The repository updates the state of the pilot to FLYING_FORWARD.
     */
    public void flyToDestinationPoint() {
        Pilot pilot = null;
        try{
            this.mutex.lock();
            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.FLYING_FORWARD);
            this.repository.setPilotState(PilotState.FLYING_FORWARD);
            this.repository.logStatus();
            
            Log.print("DepartureAirport", "Pilot started flight to destination airport.");
            this.COND_PILOT.await(new Random().nextInt(2), TimeUnit.SECONDS);
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    
    
}