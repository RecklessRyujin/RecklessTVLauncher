package dev.reckless.tvlauncher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;

public class ManageAppsActivity extends Activity {
    private static final String PREFS="reckless_launcher_prefs";
    private LinearLayout list;
    private SharedPreferences prefs;
    private int accent=Color.rgb(26,174,102);
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences(PREFS,MODE_PRIVATE);accent=prefs.getInt("accent",accent);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(prefs.getInt("background",Color.rgb(16,21,28)));sc.setPadding(dp(34),dp(22),dp(34),dp(28));
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);sc.addView(list);setContentView(sc);render();}
    private void render(){list.removeAllViews();heading("MANAGE APPS","Hide apps from Reckless TV without uninstalling them. System apps and app data are not changed.");
        PackageManager pm=getPackageManager();List<ResolveInfo> apps=new ArrayList<>();Set<String> discovered=new HashSet<>();
        Intent leanback=new Intent(Intent.ACTION_MAIN);leanback.addCategory("android.intent.category.LEANBACK_LAUNCHER");collect(pm,leanback,apps,discovered);
        Intent launcher=new Intent(Intent.ACTION_MAIN);launcher.addCategory(Intent.CATEGORY_LAUNCHER);collect(pm,launcher,apps,discovered);
        Collections.sort(apps,(a,b)->String.valueOf(a.loadLabel(pm)).compareToIgnoreCase(String.valueOf(b.loadLabel(pm))));
        Set<String> hidden=new HashSet<>(prefs.getStringSet("hidden_apps",new HashSet<String>()));Set<String> seen=new HashSet<>();int count=0;
        for(ResolveInfo r:apps){if(r.activityInfo==null||r.activityInfo.packageName.equals(getPackageName()))continue;String key=r.activityInfo.packageName+"/"+r.activityInfo.name;if(!seen.add(key))continue;count++;final boolean isHidden=hidden.contains(key);
            addCard(String.valueOf(r.loadLabel(pm)),r.activityInfo.packageName,isHidden?"RESTORE TO HOME":"HIDE FROM HOME",()->{Set<String> next=new HashSet<>(prefs.getStringSet("hidden_apps",new HashSet<String>()));if(isHidden)next.remove(key);else next.add(key);prefs.edit().putStringSet("hidden_apps",next).apply();render();});}
        if(count==0)heading("NO APPS FOUND","No launchable apps were returned by Android.");
        addCard("Restore all apps","Show every app in the launcher again","RESTORE ALL",()->{prefs.edit().remove("hidden_apps").apply();render();Toast.makeText(this,"All apps restored",Toast.LENGTH_SHORT).show();});
        addCard("Back","Return to launcher settings","DONE",()->finish());}
    private void collect(PackageManager pm,Intent query,List<ResolveInfo> out,Set<String> seen){List<ResolveInfo> results;if(android.os.Build.VERSION.SDK_INT>=33)results=pm.queryIntentActivities(query,PackageManager.ResolveInfoFlags.of(0));else results=pm.queryIntentActivities(query,0);for(ResolveInfo r:results){if(r.activityInfo==null)continue;String k=r.activityInfo.packageName+"/"+r.activityInfo.name;if(seen.add(k))out.add(r);}}
    private void heading(String a,String b){TextView h=new TextView(this);h.setText(a);h.setTextColor(accent);h.setTextSize(13);h.setTypeface(null,Typeface.BOLD);h.setLetterSpacing(.12f);list.addView(h);TextView d=new TextView(this);d.setText(b);d.setTextColor(Color.WHITE);d.setTextSize(20);d.setPadding(0,dp(8),0,dp(18));list.addView(d);}
    private void addCard(String title,String detail,String action,Runnable run){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(15),dp(10),dp(15),dp(10));c.setFocusable(true);c.setClickable(true);GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(29,38,50));bg.setCornerRadius(dp(10));bg.setStroke(dp(1),Color.rgb(48,59,73));c.setBackground(bg);
        TextView t=new TextView(this);t.setText(title);t.setTextColor(Color.WHITE);t.setTextSize(15);t.setTypeface(null,Typeface.BOLD);c.addView(t);TextView d=new TextView(this);d.setText(detail+"  •  "+action);d.setTextColor(Color.rgb(150,163,178));d.setTextSize(11);c.addView(d);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(62));lp.bottomMargin=dp(7);list.addView(c,lp);c.setOnFocusChangeListener((v,f)->{GradientDrawable x=new GradientDrawable();x.setColor(Color.rgb(29,38,50));x.setCornerRadius(dp(10));x.setStroke(dp(f?3:1),f?accent:Color.rgb(48,59,73));v.setBackground(x);});c.setOnClickListener(v->run.run());}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
