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

    public void setFlightNumber(int FN){
        try{
            this.mutex.lock();
            this.FN = FN;
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
        String header = "";
        header += "Airlift - Description of the internal state\n";
        
        String h1 = "PT\tHT\t";
        String h2 = this.pilotState + "\t" + this.hostessState + "\t";
        for(int i = 0; i < this.passengersStates.length; i++){
            h1 += i < 10 ? "P0" + i + "\t" : "P" + i + "\t";
            h2 += this.passengersStates[i] + "\t";
        }
        h1 += "InQ\tInF\tPTAL\n";
        h2 += this.InQ + "\t" + this.InF + "\t" + this.PTAL + "\n";

        header += h1 + h2;

        this.writeLog(header);
    }

    public void logEvent(Event event, int value) throws IOException{
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
        this.writeLog(line);
    }

    public void logStatus() throws IOException{
        String line = this.pilotState + "\t" + this.hostessState + "\t";
        for(int i = 0; i < this.passengersStates.length; i++){
            line += this.passengersStates[i] + "\t";
        }
        line += this.InQ + "\t" + this.InF + "\t" + this.PTAL + "\n";
        this.writeLog(line);
    }

    public void logSummary() throws IOException {
        String summary = "Airlift sum up:\n";

        for(int i = 0; i < this.mapFlightPassengers.size(); i++){
            summary += String.format("Flight %d transported %d passengers\n", i+1, this.mapFlightPassengers.get(i));
        }
        this.writeLog(summary);
    }
    
    public void logLegend() throws IOException {
        String legend = "Legend:\n";
        legend += "PT\t- state of the pilot\n";
        legend += "HT\t- state of the hostess\n";
        legend += "P##\t- state of the passenger ##\n";
        legend += "InQ\t- number of passengers presently forming a queue to board the plane\n";
        legend += "InF\t- number of passengers in the plane\n";
        legend += "PT\t- number of passengers taht have already arrived at their destination\n";
        
        this.writeLog(legend);
    }
    
    private void writeLog(String line) throws IOException{
        this.bw.write(line);
    }

}