package shared;

import conf.Configuration;
import states.Event;
import states.HostessState;
import states.PassengerState;
import states.PilotState;
import utils.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    /** Set Methods */
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

    public void setPTAL(int PTAL){
        try{
            this.mutex.lock();
            this.PTAL = PTAL;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    /** Get Methods */


    /** Log Methods */

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

    public void logEvent(Event event, int value) throws IOException{
        try{
            this.mutex.lock();
            String line = "";
            switch(event){
                case BOARDING_STARTED:
                    line = String.format("\nFlight %d: boarding started.", this.FN);
                    break;
                case PASSENGER_CHECKED:
                    line = String.format("\nFlight %d: passenger %d checked.", this.FN, value);
                    break;
                case DEPARTED:
                    line = String.format("\nFlight %d: departed with %d passengers.", this.FN, value);
                    break;
                case ARRIVED:
                    line = String.format("\nFlight %d: arrived.", this.FN);
                    break;
                case RETURNING:
                    line = String.format("\nFlight %d: returning.", this.FN);
                    break;
            }
            this.bw.write(line);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

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

    public void logSummary() throws IOException {
        try{
            this.mutex.lock();
            String summary = "Airlift sum up:\n";
    
            for(int i = 0; i < this.mapFlightPassengers.size(); i++){
                summary += String.format("Flight %d transported %d passengers\n", i+1, this.mapFlightPassengers.get(i));
            }
            this.bw.write(summary);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }
    
    public void logLegend() throws IOException {
        try{
            this.mutex.lock();    
            String legend = "Legend:\n";
            legend += "PT  - state of the pilot\n";
            legend += "HT  - state of the hostess\n";
            legend += "P##  - state of the passenger ##\n";
            legend += "InQ  - number of passengers presently forming a queue to board the plane\n";
            legend += "InF  - number of passengers in the plane\n";
            legend += "PT  - number of passengers taht have already arrived at their destination\n";
            this.bw.write(legend);
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }
}