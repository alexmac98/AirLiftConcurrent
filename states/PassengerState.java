package states;

/**  
 * Definition of the Passenger states
 */
public enum PassengerState{
    /**
     * The passenger is going to the airport.
     */
    GOING_TO_AIRPORT("GTA"),

    /** 
     * The passenger is waiting in queue.
     */
    IN_QUEUE("IQ"),

    /**
     * The passenger is in flight.
     */
    IN_FLIGHT("IF"),

    /**
     * The passenger arrived at the destination.
     */
    AT_DESTINATION("AD");

    private final String logRepresentation;
    /**
     * Constructor to add a new state
     * @param logRepresentation Textual representation of the state
     */
    private PassengerState(String logRepresentation){
        this.logRepresentation = logRepresentation;
    }

    /**
     * Method that returns a String representation of a state
     * @return String representation of a state
     */
    @Override 
    public String toString(){
        return this.logRepresentation;
    }

}