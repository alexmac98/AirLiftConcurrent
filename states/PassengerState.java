package states;

/**  
 * Definition of the Passenger states
 */
public enum PassengerState{
    GOING_TO_AIRPORT("GTA"),
    IN_QUEUE("IQ"),
    IN_FLIGHT("IF"),
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