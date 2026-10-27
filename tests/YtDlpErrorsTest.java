import be.yt320.s24.YtDlpErrors;
public final class YtDlpErrorsTest {
    static void check(String input, String expected) {
        String got = YtDlpErrors.extract(input);
        if (!expected.equals(got)) throw new AssertionError("expected=" + expected + " got=" + got);
    }
    public static void main(String[] args) {
        check("WARNING: old version\n\nERROR: video unavailable", "ERROR: video unavailable");
        check("WARNING: try update\nERROR: sign in required", "ERROR: sign in required");
        check("Error: network", "Error: network");
        check(null, "Onbekende downloadfout.");
        check("   ", "Onbekende downloadfout.");
        System.out.println("Geslaagd: 5 foutmeldingtests.");
    }
}
