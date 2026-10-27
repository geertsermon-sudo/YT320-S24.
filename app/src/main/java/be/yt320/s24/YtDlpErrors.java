package be.yt320.s24;

/** A concise, copyable error report for yt-dlp failures. */
public final class YtDlpErrors {
    private YtDlpErrors() {}

    public static String extract(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Onbekende downloadfout.";
        String clean = raw.replaceAll("\\u001B\\[[;\\d]*m", "").replace('\r', '\n').trim();
        int lastError = Math.max(clean.lastIndexOf("ERROR:"), clean.lastIndexOf("Error:"));
        String focus = lastError >= 0 ? clean.substring(lastError).trim() : clean;
        if (focus.length() > 1200) {
            focus = lastError >= 0 ? focus.substring(0, 1200) :
                focus.substring(focus.length() - 1200);
        }
        return focus.isEmpty() ? "Download mislukt. Kopieer de foutmelding." : focus;
    }
}
