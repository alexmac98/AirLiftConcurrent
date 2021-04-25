package states;

public enum Event {
    /**
     * The plane boarding has started.
     */
    BOARDING_STARTED,
    /**
     * A passenger was checked.
     */
    PASSENGER_CHECKED,

    /** 
     * The plane has departed.
     */
    DEPARTED,

    /**
     * The plane has arrived.
     */
    ARRIVED,

    /**
     * The plane is returning.
     */
    RETURNING;
}
