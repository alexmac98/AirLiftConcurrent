package states;

/**  
 * Definition of the Pilot states
 */
public enum PilotState{
    /**
     * The pilot is at the transfer gate.
     */
    AT_TRANSFER_GATE("ATG"),

    /**
     * The pilot is ready for boarding.
     */
    READY_FOR_BOARDING("RFB"),

    /**
     * The pilot is waiting for all the passengers to board.
     */
    WAIT_FOR_BOARDING("WFB"),

    /**
     * The pilot is flying to the destination airport.
     */
    FLYING_FORWARD("FF"),

    /**
     * The pilot is waiting for all the passengers to leave the plane.
     */
    DEBOARDING("D"),

    /**
     * The pilot is returning to the departure airport.
     */
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