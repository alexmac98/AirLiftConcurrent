package states;

/**  
 * Definition of the Hostess states
 */
public enum HostessState{
    WAIT_FOR_NEXT_FLIGHT("WFNF"),
    WAIT_FOR_PASSENGER("WFP"),
    CHECK_PASSENGER("CP"),
    READY_TO_FLY("RTF");

    private final String logRepresentation;
    /**
     * Constructor to add a new state
     * @param logRepresentation Textual representation of the state
     */
    private HostessState(String logRepresentation){
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