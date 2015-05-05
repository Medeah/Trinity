package trinity;

// TODO: this is not good.
//
public class UniqueId {
    private static int idc = 0;
    public static String next() {
        return "_u" + idc++;
    }
}
