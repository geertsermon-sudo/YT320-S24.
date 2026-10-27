package be.yt320.s24;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YoutubeLinks {
    private static final Pattern LINKS = Pattern.compile("https?://[^\\s<>\\\"']+", Pattern.CASE_INSENSITIVE);
    private YoutubeLinks() {}

    public static String find(String text) {
        if (text == null) return null;
        Matcher m = LINKS.matcher(text);
        while (m.find()) {
            String candidate = m.group().replaceAll("[.,;!?)\\]}>]+$", "");
            if (isYoutube(candidate)) return candidate;
        }
        return null;
    }

    public static boolean isYoutube(String url) {
        try {
            URI uri = new URI(url);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null) return false;
            String host = uri.getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.ROOT);
            return host.equals("youtube.com") || host.endsWith(".youtube.com")
                || host.equals("youtu.be")
                || host.equals("youtube-nocookie.com") || host.endsWith(".youtube-nocookie.com");
        } catch (Exception ex) { return false; }
    }
}
