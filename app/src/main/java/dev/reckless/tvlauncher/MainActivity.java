package dev.reckless.tvlauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int COLUMN_COUNT = 4;
    private final Handler handler = new Handler();
    private LinearLayout appRows;
    private TextView clock;
    private TextView appCount;
    private final List<AppEntry> apps = new ArrayList<>();

    private final Runnable clockUpdater = new Runnable() {
        @Override
        public void run() {
            if (clock != null) {
                clock.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
            }
            handler.postDelayed(this, 30000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(16, 19, 25));
        getWindow().setNavigationBarColor(Color.rgb(16, 19, 25));
        setContentView(R.layout.activity_main);

        appRows = findViewById(R.id.appRows);
        clock = findViewById(R.id.clock);
        appCount = findViewById(R.id.appCount);
        TextView settingsButton = findViewById(R.id.settingsButton);
        TextView refreshButton = findViewById(R.id.refreshButton);

        settingsButton.setOnClickListener(v -> openTvSettings());
        refreshButton.setOnClickListener(v -> loadApps());

        loadApps();
        handler.post(clockUpdater);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(clockUpdater);
        super.onDestroy();
    }

    private void loadApps() {
        if (appRows == null) return;
        appRows.removeAllViews();
        apps.clear();

        PackageManager pm = getPackageManager();
        Set<String> seen = new HashSet<>();
        collectApps(pm, new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), seen);
        collectApps(pm, new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), seen);

        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry a, AppEntry b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });

        for (int start = 0; start < apps.size(); start += COLUMN_COUNT) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(10);
            appRows.addView(row, rowParams);

            int end = Math.min(start + COLUMN_COUNT, apps.size());
            for (int i = start; i < end; i++) {
                AppEntry entry = apps.get(i);
                row.addView(createAppTile(entry), new LinearLayout.LayoutParams(0, dp(112), 1f));
            }
            for (int i = end - start; i < COLUMN_COUNT; i++) {
                View spacer = new View(this);
                row.addView(spacer, new LinearLayout.LayoutParams(0, dp(112), 1f));
            }
        }

        appCount.setText(apps.size() + " apps available  •  Use the remote to navigate");
        if (apps.isEmpty()) {
            Toast.makeText(this, "No launchable apps were found.", Toast.LENGTH_LONG).show();
        }
    }

    private void collectApps(PackageManager pm, Intent query, Set<String> seen) {
        List<ResolveInfo> results = pm.queryIntentActivities(query, 0);
        for (ResolveInfo info : results) {
            String packageName = info.activityInfo.packageName;
            if (packageName.equals(getPackageName()) || seen.contains(packageName)) continue;

            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent == null) {
                launchIntent = new Intent(Intent.ACTION_MAIN);
                launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                launchIntent.setClassName(packageName, info.activityInfo.name);
            }
            Drawable icon;
            CharSequence label;
            try {
                icon = info.loadIcon(pm);
                label = info.loadLabel(pm);
            } catch (RuntimeException ex) {
                continue;
            }
            if (label == null || icon == null) continue;
            seen.add(packageName);
            apps.add(new AppEntry(packageName, label.toString(), icon, launchIntent));
        }
    }

    private View createAppTile(AppEntry entry) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setFocusable(true);
        tile.setFocusableInTouchMode(true);
        tile.setClickable(true);
        tile.setBackgroundResource(R.drawable.tile_background);
        tile.setPadding(dp(6), dp(7), dp(6), dp(6));
        tile.setContentDescription(entry.label);

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(entry.icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(46), dp(46));
        iconParams.bottomMargin = dp(6);
        tile.addView(icon, iconParams);

        TextView label = new TextView(this);
        label.setText(entry.label);
        label.setTextColor(Color.rgb(239, 243, 249));
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tile.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tile.setOnClickListener(v -> launchApp(entry));
        tile.setOnFocusChangeListener((v, hasFocus) -> {
            v.animate().scaleX(hasFocus ? 1.04f : 1.0f)
                    .scaleY(hasFocus ? 1.04f : 1.0f).setDuration(100L).start();
        });

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tile.getLayoutParams();
        return tile;
    }

    private void launchApp(AppEntry entry) {
        try {
            Intent intent = new Intent(entry.launchIntent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ex) {
            Toast.makeText(this, "Couldn't open " + entry.label, Toast.LENGTH_SHORT).show();
        }
    }

    private void openTvSettings() {
        Intent[] candidates = new Intent[] {
                new Intent("com.amazon.tv.settings.OPEN_SETTINGS"),
                new Intent("android.settings.SETTINGS"),
                new Intent("android.settings.APPLICATION_SETTINGS")
        };
        for (Intent intent : candidates) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException | SecurityException ignored) {
                // Try the next supported settings entry point.
            }
        }
        Toast.makeText(this, "TV Settings couldn't be opened from this launcher.", Toast.LENGTH_LONG).show();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static final class AppEntry {
        final String packageName;
        final String label;
        final Drawable icon;
        final Intent launchIntent;

        AppEntry(String packageName, String label, Drawable icon, Intent launchIntent) {
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
            this.launchIntent = launchIntent;
        }
    }
}
