import be.yt320.s24.YoutubeLinks;

public final class YoutubeLinksTest {
    static int tests = 0;
    static void check(String input, String want) {
        String got = YoutubeLinks.find(input);
        if (want == null ? got != null : !want.equals(got)) {
            throw new AssertionError("input=" + input + " expected=" + want + " actual=" + got);
        }
        tests++;
    }
    public static void main(String[] args) {
        check("https://youtu.be/AbCd123?si=z", "https://youtu.be/AbCd123?si=z");
        check("Check dit: https://www.youtube.com/watch?v=AbCd123", "https://www.youtube.com/watch?v=AbCd123");
        check("https://music.youtube.com/watch?v=ABC!", "https://music.youtube.com/watch?v=ABC");
        check("https://youtube.com/shorts/AbCd", "https://youtube.com/shorts/AbCd");
        check("https://youtube.com.evil.org/watch?v=ABC", null);
        check("http://youtube.com/watch?v=ABC", null);
        check("ftp://youtube.com/watch?v=ABC", null);
        check("https://youtube.com@evil.org/watch?v=ABC", null);
        check("https://example.org/a https://youtu.be/ABC", "https://youtu.be/ABC");
        check("Geen link", null);
        check(null, null);
        System.out.println("Geslaagd: " + tests + " linktests.");
    }
}
