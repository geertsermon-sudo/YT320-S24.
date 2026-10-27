package be.yt320.s24;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yausername.youtubedl_android.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(16, 19, 24);
    private static final int PANEL = Color.rgb(29, 34, 41);
    private static final int WHITE = Color.rgb(242, 245, 247);
    private static final int MUTED = Color.rgb(157, 167, 178);
    private static final int ACCENT = Color.rgb(164, 244, 198);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText urlInput;
    private Button downloadButton, pasteButton;
    private TextView statusView, infoView;
    private ProgressBar progressBar;
    private boolean busy, shareStarted;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        renderUi();
        handleShare(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        shareStarted = false;
        handleShare(intent);
    }

    private void renderUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(44), dp(24), dp(24));
        scroll.addView(root);

        TextView logo = text("♪", 35, BG, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(rounded(ACCENT, 20));
        root.addView(logo, new LinearLayout.LayoutParams(dp(70), dp(70)));

        TextView title = text("YT320", 38, WHITE, true);
        LinearLayout.LayoutParams titleParam = new LinearLayout.LayoutParams(-1, -2);
        titleParam.topMargin = dp(20);
        root.addView(title, titleParam);
        root.addView(text("YouTube → MP3. Zonder gedoe.", 16, MUTED, false));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));
        panel.setBackground(rounded(PANEL, 20));
        LinearLayout.LayoutParams panelParam = new LinearLayout.LayoutParams(-1, -2);
        panelParam.topMargin = dp(30);
        root.addView(panel, panelParam);

        panel.addView(text("YOUTUBE-LINK", 12, MUTED, true));
        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setHint("Plak je YouTube-link hier");
        urlInput.setHintTextColor(MUTED);
        urlInput.setTextColor(WHITE);
        urlInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        urlInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        urlInput.setBackground(rounded(BG, 12));
        LinearLayout.LayoutParams inp = new LinearLayout.LayoutParams(-1, dp(56));
        inp.topMargin = dp(12);
        panel.addView(urlInput, inp);

        pasteButton = button("Plakken", PANEL, WHITE);
        LinearLayout.LayoutParams pasteParams = new LinearLayout.LayoutParams(-1, dp(44));
        pasteParams.topMargin = dp(10);
        panel.addView(pasteButton, pasteParams);
        pasteButton.setOnClickListener(v -> paste());

        downloadButton = button("↓  DOWNLOAD MP3", ACCENT, BG);
        LinearLayout.LayoutParams downloadParams = new LinearLayout.LayoutParams(-1, dp(62));
        downloadParams.topMargin = dp(22);
        panel.addView(downloadButton, downloadParams);
        downloadButton.setOnClickListener(v -> startDownload());

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, dp(8));
        progressParams.topMargin = dp(20);
        panel.addView(progressBar, progressParams);

        statusView = text("Klaar voor je eerste download.", 14, WHITE, false);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(18);
        panel.addView(statusView, statusParams);
        infoView = text("MP3 · doel 320 kbps · Downloads", 13, MUTED, false);
        panel.addView(infoView);
        TextView foot = text("Tip: YouTube → Delen → YT320.\nHet downloaden start meteen.", 14, MUTED, false);
        LinearLayout.LayoutParams footParams = new LinearLayout.LayoutParams(-1, -2);
        footParams.topMargin = dp(25);
        root.addView(foot, footParams);
        setContentView(scroll);
    }

    private void handleShare(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return;
        if (!"text/plain".equals(intent.getType())) return;
        String link = YoutubeLinks.find(intent.getStringExtra(Intent.EXTRA_TEXT));
        if (link == null) return;
        urlInput.setText(link);
        if (!busy && !shareStarted) {
            shareStarted = true;
            urlInput.post(this::startDownload);
        }
    }

    private void paste() {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null || !manager.hasPrimaryClip()) {
            Toast.makeText(this, "Klembord is leeg.", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData data = manager.getPrimaryClip();
        if (data == null || data.getItemCount() == 0) return;
        CharSequence contents = data.getItemAt(0).coerceToText(this);
        String link = YoutubeLinks.find(contents == null ? "" : contents.toString());
        if (link == null) Toast.makeText(this, "Geen YouTube-link gevonden.", Toast.LENGTH_SHORT).show();
        else urlInput.setText(link);
    }

    private void startDownload() {
        if (busy) return;
        String url = YoutubeLinks.find(urlInput.getText().toString());
        if (url == null) {
            statusView.setText("Plak eerst een geldige YouTube-link.");
            return;
        }
        busy = true;
        downloadButton.setEnabled(false);
        pasteButton.setEnabled(false);
        downloadButton.setAlpha(0.6f);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        statusView.setText("Voorbereiden…");
        infoView.setText("Hou de app open tijdens downloaden.");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null) input.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
        executor.execute(() -> download(url));
    }

    private void download(String url) {
        File directory = new File(getCacheDir(), "yt_" + System.nanoTime());
        try {
            if (!directory.mkdirs()) throw new IllegalStateException("Kan tijdelijke map niet maken.");
            post("Downloadmotor starten…");
            YoutubeDL.getInstance().init(getApplicationContext());
            FFmpeg.getInstance().init(getApplicationContext());
            YoutubeDLRequest request = new YoutubeDLRequest(url);
            request.addOption("--no-playlist");
            request.addOption("--format", "bestaudio/best");
            request.addOption("--extract-audio");
            request.addOption("--audio-format", "mp3");
            request.addOption("--audio-quality", "320K");
            request.addOption("--embed-metadata");
            request.addOption("--no-mtime");
            request.addOption("--output", new File(directory, "%(title).150B [%(id)s].%(ext)s").getAbsolutePath());
            post("Audio downloaden…");
            YoutubeDL.getInstance().execute(request, (progress, eta) -> runOnUiThread(() -> {
                int pct = Math.max(0, Math.min(100, Math.round(progress)));
                progressBar.setProgress(pct);
                statusView.setText(pct >= 99 ? "Omzetten naar MP3…" :
                    String.format(Locale.getDefault(), "Downloaden: %d%%", pct));
            }));
            File[] mp3s = directory.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".mp3"));
            if (mp3s == null || mp3s.length == 0) throw new IllegalStateException("Geen MP3 gemaakt.");
            Arrays.sort(mp3s, Comparator.comparingLong(File::lastModified).reversed());
            File mp3 = mp3s[0];
            if (mp3.length() == 0) throw new IllegalStateException("Leeg MP3-bestand.");
            post("Opslaan in Downloads…");
            saveToDownloads(mp3);
            runOnUiThread(() -> {
                statusView.setText("✓ Klaar! " + mp3.getName());
                infoView.setText("Mijn bestanden → Downloads");
                Toast.makeText(this, "MP3 staat in Downloads!", Toast.LENGTH_LONG).show();
                reset();
            });
        } catch (Exception ex) {
            runOnUiThread(() -> {
                statusView.setText("Download mislukt.");
                String message = ex.getMessage();
                infoView.setText(message == null ? "Kan deze video niet downloaden." :
                    message.substring(0, Math.min(180, message.length())));
                reset();
            });
        } finally {
            delete(directory);
        }
    }

    private void saveToDownloads(File mp3) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, mp3.getName());
        values.put(MediaStore.Downloads.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/");
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        Uri result = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (result == null) throw new IllegalStateException("Kan Downloads niet openen.");
        boolean success = false;
        try (FileInputStream source = new FileInputStream(mp3);
             OutputStream dest = getContentResolver().openOutputStream(result)) {
            if (dest == null) throw new IllegalStateException("Kan MP3 niet opslaan.");
            byte[] buffer = new byte[65536];
            int n;
            while ((n = source.read(buffer)) != -1) dest.write(buffer, 0, n);
            success = true;
        } finally {
            if (success) {
                ContentValues complete = new ContentValues();
                complete.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(result, complete, null, null);
            } else getContentResolver().delete(result, null, null);
        }
    }

    private void post(String value) { runOnUiThread(() -> statusView.setText(value)); }
    private void reset() {
        busy = false;
        shareStarted = false;
        downloadButton.setEnabled(true);
        pasteButton.setEnabled(true);
        downloadButton.setAlpha(1.0f);
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private static void delete(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) delete(child);
        }
        file.delete();
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private Button button(String value, int bg, int fg) {
        Button view = new Button(this);
        view.setText(value);
        view.setAllCaps(false);
        view.setTextColor(fg);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setBackground(rounded(bg, 12));
        return view;
    }
    private GradientDrawable rounded(int color, int rad) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(rad));
        return shape;
    }
    private int dp(float v) { return (int)(getResources().getDisplayMetrics().density * v + 0.5f); }
    @Override protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
