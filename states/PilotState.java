package states;

/**  
 * Definition of the Pilot states
 */
public enum PilotState{
    AT_TRANSFER_GATE("ATG"),
    READY_FOR_BOARDING("RFB"),
    WAIT_FOR_BOARDING("WFB"),
    FLYING_FORWARD("FF"),
    DEBOARDING("D"),
    FLYING_BACK("FB");

    private final String logRepresentation;
    /**
     * Constructor to add a new state
     * @param logRepresentation Textual representation of the state
     */
    private PilotState(String logRepresentation){
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