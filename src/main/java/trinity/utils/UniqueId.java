package trinity.utils;

/**
 * Class for generating unique ids
 */
public class UniqueId {

    private static int idc = 0;

    private UniqueId() {
    }

    /**
     * Gets a new unique identifier
     *
     * @return a unique identifier
     */
    public static String next() {
        return "u" + idc++;
    }
}
