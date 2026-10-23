package dev.reckless.tvlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private static final String PREFS = "reckless_launcher_prefs";
    private static final int REQUEST_WALLPAPER = 421;
    private LinearLayout content;
    private SharedPreferences prefs;
    private int accent;
    private int bg;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        accent = prefs.getInt("accent", Color.rgb(26,174,102));
        bg = prefs.getInt("background", Color.rgb(16,21,28));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);
        scroll.setPadding(dp(36), dp(24), dp(36), dp(28));
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
        buildHome();
    }

    private void buildHome() {
        content.removeAllViews();
        title("RECKLESS TV SETTINGS", "Choose a section");
        section("DEVICE SETTINGS");
        addButton("Network / Wi-Fi settings", "Open Fire TV network settings", new Runnable() {
            @Override public void run() { openSystem("android.settings.WIFI_SETTINGS"); }
        });
        addButton("Display settings", "Open available display settings", new Runnable() {
            @Override public void run() { openSystem("android.settings.DISPLAY_SETTINGS"); }
        });
        addButton("Sound settings", "Open available sound settings", new Runnable() {
            @Override public void run() { openSystem("android.settings.SOUND_SETTINGS"); }
        });
        addButton("Applications", "Manage installed apps", new Runnable() {
            @Override public void run() { openSystem("android.settings.APPLICATION_SETTINGS"); }
        });
        addButton("Fire TV system settings", "Some controls are provided by Fire OS", new Runnable() {
            @Override public void run() { openSystem("android.settings.SETTINGS"); }
        });
        section("LAUNCHER");
        addButton("Customize appearance", "Colors, tile size, and app grid", new Runnable() {
            @Override public void run() { buildAppearance(); }
        });
        addButton("Back to launcher", "Return to your app grid", new Runnable() {
            @Override public void run() { finish(); }
        });
        focusFirst();
    }

    private void buildAppearance() {
        content.removeAllViews();
        title("CUSTOMIZE LAUNCHER", "Changes are saved on this device");
        section("HIGHLIGHT COLOR");
        addButton("Green (default)", "Reckless green", chooseAccent(Color.rgb(26,174,102)));
        addButton("Blue", "Blue highlight", chooseAccent(Color.rgb(48,132,255)));
        addButton("Purple", "Purple highlight", chooseAccent(Color.rgb(171,92,255)));
        addButton("Orange", "Orange highlight", chooseAccent(Color.rgb(255,145,48)));
        addButton("Red", "Red highlight", chooseAccent(Color.rgb(245,74,88)));

        section("BACKGROUND COLOR");
        addButton("Deep navy", "Default dark background", chooseBackground(Color.rgb(16,21,28)));
        addButton("Pure black", "Black background", chooseBackground(Color.rgb(0,0,0)));
        addButton("Charcoal", "Slightly lighter dark background", chooseBackground(Color.rgb(30,32,36)));
        addButton("Dark green", "Green-tinted background", chooseBackground(Color.rgb(9,28,22)));
        addButton("Choose background image", "Pick an image if Fire OS provides a file picker", new Runnable() {
            @Override public void run() { chooseWallpaper(); }
        });
        addButton("Remove background image", "Use the selected solid background color", new Runnable() {
            @Override public void run() {
                prefs.edit().remove("wallpaper_uri").apply();
                Toast.makeText(SettingsActivity.this, "Background image removed", Toast.LENGTH_SHORT).show();
            }
        });

        section("APP GRID");
        addButton("3 columns", "Larger tiles, fewer apps per row", chooseColumns(3));
        addButton("4 columns (default)", "Balanced grid", chooseColumns(4));
        addButton("5 columns", "More apps per row", chooseColumns(5));
        addButton("6 columns", "Compact grid", chooseColumns(6));

        section("TILE HEIGHT");
        final TextView tileValue = new TextView(this);
        tileValue.setTextColor(Color.WHITE);
        tileValue.setTextSize(15);
        tileValue.setPadding(0, dp(8), 0, dp(4));
        final int current = prefs.getInt("tile_height", 112);
        tileValue.setText("Tile height: " + current + " dp");
        content.addView(tileValue);
        SeekBar seek = new SeekBar(this);
        seek.setMax(80);
        seek.setProgress(current - 80);
        seek.setContentDescription("App tile height");
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int value = progress + 80;
                tileValue.setText("Tile height: " + value + " dp");
                if (fromUser) prefs.edit().putInt("tile_height", value).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        content.addView(seek, new LinearLayout.LayoutParams(-1, dp(48)));
        addButton("Restore appearance defaults", "Green highlight, navy background, four columns", new Runnable() {
            @Override public void run() {
                prefs.edit().clear().apply();
                accent = Color.rgb(26,174,102);
                bg = Color.rgb(16,21,28);
                buildAppearance();
            }
        });
        addButton("Back to settings", "Return to settings hub", new Runnable() {
            @Override public void run() { buildHome(); }
        });
        focusFirst();
    }

    private Runnable chooseAccent(final int color) {
        return new Runnable() {
            @Override public void run() {
                accent = color;
                prefs.edit().putInt("accent", color).apply();
                Toast.makeText(SettingsActivity.this, "Highlight color saved", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Runnable chooseBackground(final int color) {
        return new Runnable() {
            @Override public void run() {
                bg = color;
                prefs.edit().putInt("background", color).apply();
                prefs.edit().remove("wallpaper_uri").apply();
                Toast.makeText(SettingsActivity.this, "Background color saved", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private Runnable chooseColumns(final int columns) {
        return new Runnable() {
            @Override public void run() {
                prefs.edit().putInt("columns", columns).apply();
                Toast.makeText(SettingsActivity.this, columns + " column layout saved", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void chooseWallpaper() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_WALLPAPER);
        } catch (Exception e) {
            Toast.makeText(this, "This Fire TV does not provide an image picker", Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WALLPAPER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            prefs.edit().putString("wallpaper_uri", uri.toString()).apply();
            Toast.makeText(this, "Background image saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSystem(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception e) {
            Toast.makeText(this, "That settings page isn't available on this Fire TV", Toast.LENGTH_LONG).show();
        }
    }

    private void title(String heading, String subtitle) {
        TextView h = new TextView(this);
        h.setText(heading);
        h.setTextColor(accent);
        h.setTextSize(13);
        h.setLetterSpacing(0.12f);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(h, params(WRAP, dp(28)));
        TextView s = new TextView(this);
        s.setText(subtitle);
        s.setTextColor(Color.rgb(243,246,249));
        s.setTextSize(25);
        s.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        s.setPadding(0, 0, 0, dp(18));
        content.addView(s, params(WRAP, WRAP));
    }

    private void section(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.rgb(132,144,158));
        t.setTextSize(11);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setLetterSpacing(0.1f);
        t.setPadding(0, dp(15), 0, dp(6));
        content.addView(t, params(WRAP, WRAP));
    }

    private void addButton(String label, String detail, final Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setFocusable(true);
        card.setFocusableInTouchMode(false);
        card.setClickable(true);
        card.setPadding(dp(16), dp(10), dp(16), dp(10));
        card.setBackgroundResource(R.drawable.tile_background);
        TextView h = new TextView(this);
        h.setText(label);
        h.setTextColor(Color.WHITE);
        h.setTextSize(15);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(h, params(-1, WRAP));
        TextView d = new TextView(this);
        d.setText(detail);
        d.setTextColor(Color.rgb(132,144,158));
        d.setTextSize(11);
        d.setPadding(0, dp(3), 0, 0);
        card.addView(d, params(-1, WRAP));
        LinearLayout.LayoutParams lp = params(-1, dp(62));
        lp.bottomMargin = dp(7);
        content.addView(card, lp);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean focused) {
                v.setSelected(focused);
                v.refreshDrawableState();
            }
        });
    }

    private void focusFirst() {
        content.post(new Runnable() {
            @Override public void run() {
                if (content.getChildCount() > 1) content.getChildAt(2).requestFocus();
            }
        });
    }

    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }
    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
