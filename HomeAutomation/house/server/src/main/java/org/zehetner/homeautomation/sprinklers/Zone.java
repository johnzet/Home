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
    ZONE_1("Front Lower North"),      /* Black */
    ZONE_2("Front Lower West"),       /* Grey */
    ZONE_3("Front Lower East"),       /* Blue */
    ZONE_4("Front Upper North"),      /* Yellow */
    ZONE_5("Front Upper South"),      /* Purple */
    ZONE_6("Back North"),             /* Red */
    ZONE_7("Back Middle"),            /* Orange */
    ZONE_8("Back South");             /* Brown */

    private final String name;

    Zone(final String nameArg) {
        this.name = nameArg;
    }

    public String getName() {
        return this.name;
    }

    public int getPhysicalRelayNumber() {
        return this.ordinal();
    }
}
