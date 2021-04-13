package shared;

import entities.Pilot;
import states.PilotState;
import utils.Log;

import java.util.concurrent.locks.ReentrantLock;

public class DestinationAirport{

    private GRI repository;
    private ReentrantLock mutex;
    
    public DestinationAirport(GRI repository){
        this.repository = repository;
        this.mutex = new ReentrantLock();
    }

    public void flyToDeparturePoint() {
        Pilot pilot = null;
        try{
            this.mutex.lock();

            pilot = (Pilot) (Thread.currentThread());
            pilot.setState(PilotState.FLYING_BACK);
            this.repository.setPilotState(PilotState.FLYING_BACK);
            this.repository.logStatus();

            Log.print("DestinationAirport", "Pilot is flying back to the departure airport.");

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.mutex.unlock();
        }
    }

    
}