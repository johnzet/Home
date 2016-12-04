package org.zehetner.homeautomation.sprinklers;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/20/12
 * Time: 9:28 AM
 */
public enum Zone {

    // Note:  The physical relay number is the ordinal of this enum.

    ALL_OFF("All Off"),
    ZONE_1("Front North"),      	/* Wire Colors? */
    ZONE_2("Front South"),       	/*  */
    ZONE_3("Back West"),       		/*  */
    ZONE_4("Back Middle"),      	/*  */
    ZONE_5("Back East"),      		/*  */
    ZONE_6("BROKEN Front Drip"),           /*  */
    ZONE_7("Not Used"),             /*  */
    ZONE_8("Not Used");             /*  */

    private final String name;

    Zone(final String nameArg) {
        this.name = nameArg;
    }

    public static  Zone getZone(final int zoneNumber) {
        return Zone.values()[zoneNumber];
    }

    public String getName() {
        return this.name;
    }

    public int getPhysicalRelayNumber() {
        return this.ordinal();
    }
}
