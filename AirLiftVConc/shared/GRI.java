package shared;

import conf.Configuration;
import states.Event;
import states.HostessState;
import states.PassengerState;
import states.PilotState;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
public class GRI {
    
    private ReentrantLock mutex;
    private PassengerState[] passengersStates;
    private HostessState hostessState;
    private PilotState pilotState;

    private int FN;
    private int InQ; // In Queue
    private int InF; // In Flight
    private int PTAL;

    private HashMap<Integer, Integer> mapFlightPassengers;

    private FileWriter fw;
    private BufferedWriter bw;
    

    public GRI(){
        this.passengersStates = new PassengerState[Configuration.NUMBER_OF_PASSENGERS];
        for(int i = 0; i < this.passengersStates.length; i++){
            this.passengersStates[i] = PassengerState.GOING_TO_AIRPORT;
        }
        this.hostessState = HostessState.WAIT_FOR_NEXT_FLIGHT;
        this.pilotState = PilotState.AT_TRANSFER_GATE;

        this.mutex = new ReentrantLock();
        this.mapFlightPassengers = new HashMap<>();

        try{
            this.fw = new FileWriter("log.txt", true);
            this.bw = new BufferedWriter(fw);
        }catch(IOException e){
            e.printStackTrace();
        }
    
    }

    /**
     * Method that sets the current state of a passenger.
     * @param id The id of the passenger.
     * @param state The wanted state for the passenger.
     */
    public void setPassengerState(int id, PassengerState state){
        try{
            this.mutex.lock();
            this.passengersStates[id] = state;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }
    /**
     * Method that sets the current state of the hostess.
     * @param state The wanted state for the hostess.
     */
    public void setHostessState(HostessState state){
        try{
            this.mutex.lock();
            this.hostessState = state;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that sets the current state of the pilot.
     * @param state The wanted state for the pilot.
     */
    public void setPilotState(PilotState state){
        try{
            this.mutex.lock();
            this.pilotState = state;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that increments the flight number.
     */
    public void setFlightNumber(){
        try{
            this.mutex.lock();
            this.FN++;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that sets the number of passengers currently waiting in queue.
     * @param InQ The size of the queue.
     */
    public void setInQ(int InQ){
        try{
            this.mutex.lock();
            this.InQ = InQ;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that sets the number of passengers currently in flight.
     * @param InF The numebr of passengers in flight.
     */
    public void setInF(int InF){
        try{
            this.mutex.lock();
            this.InF = InF;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that sets the number of passengers that have already arrived at their destination.
     */
    public void setPTAL(){
        try{
            this.mutex.lock();
            this.PTAL++;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that sets the number of passengers per flight.
     * @param n The number of passengers.
     */
    public void setPassengersPerFlight(int n){
        try{
            this.mutex.lock();
            this.mapFlightPassengers.put(this.FN, n); 
        }catch(Exception e){

        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that writes the header of the log layout to a file.
     * @throws IOException
     */
    public void logHeader() throws IOException{
        try{
            this.mutex.lock();
            String header = "";
            header += "Airlift - Description of the internal state\n";
            
            String h1 = String.format("%4s  %4s  ", "PT", "HT");
            String h2 = this.pilotState + "  " + this.hostessState + "  ";
            for(int i = 0; i < this.passengersStates.length; i++){
                h1 += i < 10 ? String.format("%4s  ", "P0" + i) : String.format("%4s  ", "P" + i );
                h2 += String.format("%4s  ",this.passengersStates[i]);
            }
            h1 += String.format("%4s  %4s  %4s\n ", "InQ", "InF", "PTAL");
            h2 += String.format("%4s  %4s  %4s\n", this.InQ, this.InF, this.PTAL);

            header += h1 + h2;
            this.bw.write(header);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that writes an event to the log file.
     * There are 5 different events, namely BOARDING_STARTED, PASSENGER_CHECKED, DEPARTED, ARRIVED, RETURNING.
     * @param event The event of the log.
     * @param value The value to be written. Note: this value is only used as an id on PASSENGER_CHECKED and as a number on DEPARTED.  
     * @throws IOException
     */
    public void logEvent(Event event, int value) throws IOException{
        try{
            this.mutex.lock();
            String line = "";
            switch(event){
                case BOARDING_STARTED:
                    line = String.format("\nFlight %d: boarding started.\n", this.FN);
                    break;
                case PASSENGER_CHECKED:
                    line = String.format("\nFlight %d: passenger %d checked.\n", this.FN, value);
                    break;
                case DEPARTED:
                    line = String.format("\nFlight %d: departed with %d passengers.\n", this.FN, value);
                    break;
                case ARRIVED:
                    line = String.format("\nFlight %d: arrived.\n", this.FN);
                    break;
                case RETURNING:
                    line = String.format("\nFlight %d: returning.\n", this.FN);
                    break;
            }
            this.bw.write(line);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method used to write to the log file the current status of the simulation, containing information about each passenger's state, the pilot's state, the hostess's state, 
     * the current number of passengers waiting in queue, the current number of passengers in flight, and the number of passengers that arrived to their destination.
     * @throws IOException
     */
    public void logStatus() throws IOException{
        try{
            this.mutex.lock();
            String line = String.format("%4s  %4s  ", this.pilotState, this.hostessState);
            for(int i = 0; i < this.passengersStates.length; i++){
                line += String.format("%4s  ", this.passengersStates[i]);
            }
            line += String.format("%4s  %4s  %4s\n", this.InQ, this.InF, this.PTAL);
            this.bw.write(line);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /**
     * Method that writes to the log file the summary of the simulation. It consists only on information regarding the number of passengers in each occured flight.
     * @throws IOException
     */
    public void logSummary() throws IOException {
        try{
            this.mutex.lock();
            String summary = "\nAirlift sum up:\n";
    
            for(int i = 0; i < this.mapFlightPassengers.size(); i++){
                summary += String.format("Flight %d transported %d passengers\n", i+1, this.mapFlightPassengers.get(i+1));
            }
            this.bw.write(summary);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }
    /**
     * Method that write to the log file the legend of the simulation.
     * @throws IOException
     */
    public void logLegend() throws IOException {
        try{
            this.mutex.lock();    
            String legend = "\nLegend:\n";
            legend += String.format("%4s  -  state of the pilot\n", "PT");
            legend += String.format("%4s  - state of the hostess\n", "HT");
            legend += String.format("%4s  - state of the passenger ##\n", "P##");
            legend += String.format("%4s  - number of passengers presently forming a queue to board the plane\n", "InQ");
            legend += String.format("%4s  - number of passengers in the plane\n", "InF");
            legend += String.format("%4s  - number of passengers that have already arrived at their destination\n", "PTAL");
            this.bw.write(legend);
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }
}