package dev.reckless.tvlauncher;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(26,174,102);
    private static final int TEXT = Color.rgb(243,246,249);
    private static final int MUTED = Color.rgb(132,144,158);
    private GridLayout appGrid;
    private LinearLayout quickActions;
    private ScrollView scroll;
    private HorizontalScrollView quickScroll;
    private TextView clockView, dateView, countView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        appGrid=findViewById(R.id.app_grid);
        quickActions=findViewById(R.id.quick_actions);
        scroll=findViewById(R.id.scroll);
        quickScroll=findViewById(R.id.quick_scroll);
        clockView=findViewById(R.id.clock);
        dateView=findViewById(R.id.date);
        countView=findViewById(R.id.app_count);
        scroll.setFocusable(false);
        scroll.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        updateClock();
        buildQuickActions();
        loadApps();
    }

    @Override protected void onResume() {
        super.onResume();
        if(appGrid!=null){ updateClock(); loadApps(); }
    }

    private void updateClock() {
        Date now=new Date();
        clockView.setText(new SimpleDateFormat("h:mm a",Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEE, MMM d",Locale.getDefault()).format(now));
    }

    private void loadApps() {
        appGrid.removeAllViews();
        PackageManager pm=getPackageManager();
        List<ResolveInfo> found=new ArrayList<>();
        Set<String> seen=new HashSet<>();
        Intent leanback=new Intent(Intent.ACTION_MAIN);
        leanback.addCategory("android.intent.category.LEANBACK_LAUNCHER");
        collect(pm,leanback,found,seen);
        Intent launcher=new Intent(Intent.ACTION_MAIN);
        launcher.addCategory(Intent.CATEGORY_LAUNCHER);
        collect(pm,launcher,found,seen);
        Collections.sort(found,new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a,ResolveInfo b) {
                return String.valueOf(a.loadLabel(pm)).compareToIgnoreCase(String.valueOf(b.loadLabel(pm)));
            }
        });
        int count=0;
        for(ResolveInfo info:found) {
            if(info.activityInfo==null || info.activityInfo.packageName.equals(getPackageName())) continue;
            addAppTile(info); count++;
        }
        countView.setText(count+(count==1?" APP":" APPS"));
        if(count==0) {
            TextView empty=new TextView(this);
            empty.setText("No launchable apps found.");
            empty.setTextColor(MUTED); empty.setTextSize(16);
            empty.setPadding(dp(12),dp(20),dp(12),dp(20));
            appGrid.addView(empty);
        }
        if(count>0) appGrid.post(new Runnable() {
            @Override public void run() {
                if(getCurrentFocus()==null) {
                    View first=appGrid.getChildAt(0);
                    if(first!=null && first.isFocusable()) first.requestFocus();
                }
            }
        });
    }

    private void collect(PackageManager pm,Intent query,List<ResolveInfo> out,Set<String> seen) {
        List<ResolveInfo> results;
        if(android.os.Build.VERSION.SDK_INT>=33)
            results=pm.queryIntentActivities(query,PackageManager.ResolveInfoFlags.of(0));
        else { //noinspection deprecation
            results=pm.queryIntentActivities(query,0);
        }
        for(ResolveInfo r:results) {
            if(r.activityInfo==null) continue;
            String key=r.activityInfo.packageName+"/"+r.activityInfo.name;
            if(seen.add(key)) out.add(r);
        }
    }

    private void addAppTile(final ResolveInfo info) {
        final LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER);
        tile.setFocusable(true); tile.setFocusableInTouchMode(false); tile.setClickable(true);
        tile.setBackgroundResource(R.drawable.tile_background);
        tile.setContentDescription(info.loadLabel(getPackageManager()));
        tile.setPadding(dp(10),dp(12),dp(10),dp(10));
        GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
        lp.width=0; lp.height=dp(112);
        lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
        lp.setMargins(dp(7),dp(7),dp(7),dp(7)); tile.setLayoutParams(lp);
        ImageView icon=new ImageView(this);
        Drawable drawable;
        try { drawable=info.loadIcon(getPackageManager()); }
        catch(Exception e) { drawable=getDrawable(android.R.drawable.sym_def_app_icon); }
        icon.setImageDrawable(drawable); icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconLp=new LinearLayout.LayoutParams(dp(44),dp(44));
        iconLp.bottomMargin=dp(9); tile.addView(icon,iconLp);
        TextView label=new TextView(this);
        label.setText(info.loadLabel(getPackageManager())); label.setTextColor(TEXT);
        label.setTextSize(12); label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        label.setGravity(Gravity.CENTER); label.setMaxLines(2);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tile.addView(label,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(34)));
        tile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent launch=new Intent(Intent.ACTION_MAIN);
                    launch.setClassName(info.activityInfo.packageName,info.activityInfo.name);
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch);
                } catch(ActivityNotFoundException|SecurityException e) {
                    Toast.makeText(MainActivity.this,"Unable to open this app",Toast.LENGTH_SHORT).show();
                }
            }
        });
        installFocusHandling(tile);
        appGrid.addView(tile);
    }

    private void buildQuickActions() {
        quickActions.removeAllViews();
        addAction("Settings",android.R.drawable.ic_menu_preferences,new Runnable() {
            @Override public void run() {
                try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
                catch(Exception e) { Toast.makeText(MainActivity.this,"Settings unavailable",Toast.LENGTH_SHORT).show(); }
            }
        });
        addAction("Refresh apps",android.R.drawable.ic_popup_sync,new Runnable() {
            @Override public void run() { loadApps(); }
        });
        addAction("Search",android.R.drawable.ic_menu_search,new Runnable() {
            @Override public void run() {
                try {
                    Intent intent=new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY,""); startActivity(intent);
                } catch(Exception e) {
                    Toast.makeText(MainActivity.this,"Search isn't available on this device",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addAction(String title,int iconRes,final Runnable action) {
        final LinearLayout item=new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL); item.setGravity(Gravity.CENTER_VERTICAL);
        item.setFocusable(true); item.setFocusableInTouchMode(false); item.setClickable(true);
        item.setBackgroundResource(R.drawable.tile_background);
        item.setPadding(dp(15),dp(12),dp(18),dp(12)); item.setContentDescription(title);
        LinearLayout.LayoutParams itemLp=new LinearLayout.LayoutParams(dp(190),dp(60));
        itemLp.rightMargin=dp(12); quickActions.addView(item,itemLp);
        ImageView icon=new ImageView(this); icon.setImageResource(iconRes); icon.setColorFilter(GREEN);
        item.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView label=new TextView(this); label.setText(title); label.setTextColor(TEXT);
        label.setTextSize(13); label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        LinearLayout.LayoutParams textLp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        textLp.leftMargin=dp(12); item.addView(label,textLp);
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        installFocusHandling(item);
    }

    private void installFocusHandling(final View view) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v,boolean focused) {
                v.setSelected(focused);
                v.refreshDrawableState();
                if(focused) v.post(new Runnable() {
                    @Override public void run() {
                        Rect rect=new Rect(0,0,v.getWidth(),v.getHeight());
                        v.requestRectangleOnScreen(rect,false);
                    }
                });
            }
        });
    }

    @Override public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    private int dp(float value) {
        return (int)(value*getResources().getDisplayMetrics().density+0.5f);
    }
}
