package com.example.alothmanysender;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AppState {
    private static final String PREFS = "alothmany_state_v410";
    private final SharedPreferences p;
    public AppState(Context c){ p=c.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    public String selectedPackage(){ return p.getString("pkg",""); }
    public void selectedPackage(String v){ p.edit().putString("pkg",v==null?"":v).apply(); }
    public int index(){ return p.getInt("index",0); }
    public void index(int v){ p.edit().putInt("index",Math.max(0,v)).apply(); }
    public boolean running(){ return p.getBoolean("running",false); }
    public void running(boolean v){ p.edit().putBoolean("running",v).apply(); }
    public boolean paused(){ return p.getBoolean("paused",false); }
    public void paused(boolean v){ p.edit().putBoolean("paused",v).apply(); }
    public boolean publishEnabled(){ return p.getBoolean("publish",false); }
    public void publishEnabled(boolean v){ p.edit().putBoolean("publish",v).apply(); }
    public String message(){ return p.getString("message",""); }
    public void message(String v){ p.edit().putString("message",v==null?"":v).apply(); }
    public int attempts(String link){ return p.getInt("attempt_"+linkKey(link),0); }
    public void attempts(String link,int n){ p.edit().putInt("attempt_"+linkKey(link),n).apply(); }
    public void clearAttempts(String link){ p.edit().remove("attempt_"+linkKey(link)).apply(); }
    public Set<String> processed(){ return new LinkedHashSet<>(p.getStringSet("processed",new LinkedHashSet<>())); }
    public boolean isProcessed(String link){ return processed().contains(link); }
    public void markProcessed(String link){ Set<String>s=processed();s.add(link);p.edit().putStringSet("processed",s).apply(); }
    public Set<String> failed(){ return new LinkedHashSet<>(p.getStringSet("failed",new LinkedHashSet<>())); }
    public void markFailed(String link){ Set<String>s=failed();s.add(link);p.edit().putStringSet("failed",s).apply(); }
    public void clearFailed(){ p.edit().remove("failed").apply(); }
    public List<String> links(){ return new ArrayList<>(p.getStringSet("links",new LinkedHashSet<>())); }
    public void links(List<String> xs){ p.edit().putStringSet("links",new LinkedHashSet<>(xs)).apply(); }
    public void resetRun(){ p.edit().remove("processed").remove("failed").putInt("index",0).putBoolean("running",false).putBoolean("paused",false).apply(); }
}
