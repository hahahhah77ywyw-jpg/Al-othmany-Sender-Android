package com.example.alothmanysender;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

/** Android-side WhatsApp UI bridge. It never uses fixed screen coordinates. */
public final class SenderAccessibilityService extends AccessibilityService {
    public interface Result { void done(boolean success,String detail); }
    private static SenderAccessibilityService instance;
    private final Handler h=new Handler(Looper.getMainLooper());
    private boolean operation;
    public static SenderAccessibilityService get(){return instance;}

    @Override public void onServiceConnected(){
        instance=this;AccessibilityServiceInfo i=new AccessibilityServiceInfo();
        i.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED|AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED|AccessibilityEvent.TYPE_VIEW_CLICKED;
        i.feedbackType=AccessibilityServiceInfo.FEEDBACK_GENERIC;i.flags=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;i.notificationTimeout=80;setServiceInfo(i);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){instance=null;operation=false;super.onDestroy();}

    public void processInvite(String url,String pkg,String message,Result cb){
        if(operation){cb.done(false,"عملية أخرى قيد التنفيذ");return;} operation=true;
        if(pkg==null||pkg.isEmpty()){finish(cb,false,"لم يتم اختيار واتساب");return;}
        try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url));i.setPackage(pkg);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(i);}catch(Exception e){finish(cb,false,"تعذر فتح رابط الدعوة");return;}
        pollInvite(pkg,message,cb,System.currentTimeMillis()+15000,false);
    }

    private void pollInvite(String pkg,String message,Result cb,long deadline,boolean clicked){
        if(System.currentTimeMillis()>deadline){finish(cb,false,"لم يتم تأكيد الانضمام داخل المهلة");return;}
        if(!pkg.equals(getRootPackage())){h.postDelayed(()->pollInvite(pkg,message,cb,deadline,clicked),350);return;}
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(root==null){h.postDelayed(()->pollInvite(pkg,message,cb,deadline,clicked),350);return;}
        if(!clicked){
            AccessibilityNodeInfo join=find(root,"انضمام إلى المجموعة","انضمام","Join group","Join");
            if(join!=null){if(click(join)){clicked=true;} }
            else if(hasComposer(root)){clicked=true;}
        }
        if(clicked && hasComposer(root)){
            if(message!=null&&!message.trim().isEmpty()){
                boolean sent=sendMessage(root,message);
                if(!sent){h.postDelayed(()->pollInvite(pkg,message,cb,deadline,true),400);return;}
                h.postDelayed(()->finish(cb,true,"تم الانضمام/الدخول وإرسال الرسالة"),650);return;
            }
            finish(cb,true,"تم الانضمام/الدخول إلى المجموعة");return;
        }
        // Existing membership can land directly in a group chat without the join button.
        if(!clicked && hasComposer(root)){
            if(message==null||message.trim().isEmpty()){finish(cb,true,"المجموعة مفتوحة بالفعل");return;}
            if(sendMessage(root,message)){h.postDelayed(()->finish(cb,true,"المجموعة مفتوحة وتم إرسال الرسالة"),650);return;}
        }
        h.postDelayed(()->pollInvite(pkg,message,cb,deadline,clicked),350);
    }

    private boolean sendMessage(AccessibilityNodeInfo root,String message){
        AccessibilityNodeInfo input=find(root,"اكتب رسالة","اكتب رسالة...","Type a message","Message");
        if(input==null)return false;
        Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,message);
        boolean set=input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b);
        if(!set){input.performAction(AccessibilityNodeInfo.ACTION_FOCUS);set=input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b);}
        if(!set)return false;
        h.postDelayed(()->{AccessibilityNodeInfo r=getRootInActiveWindow();if(r!=null){AccessibilityNodeInfo send=find(r,"إرسال","Send");if(send!=null)click(send);}},180);
        return true;
    }
    private boolean hasComposer(AccessibilityNodeInfo root){return find(root,"اكتب رسالة","Type a message","Message")!=null;}
    private boolean click(AccessibilityNodeInfo n){if(n==null)return false;if(n.isClickable()&&n.performAction(AccessibilityNodeInfo.ACTION_CLICK))return true;AccessibilityNodeInfo p=n.getParent();while(p!=null){if(p.isClickable()&&p.performAction(AccessibilityNodeInfo.ACTION_CLICK))return true;p=p.getParent();}return false;}
    private AccessibilityNodeInfo find(AccessibilityNodeInfo n,String...labels){
        if(n==null)return null;String t=n.getText()==null?"":n.getText().toString().toLowerCase(Locale.ROOT);String d=n.getContentDescription()==null?"":n.getContentDescription().toString().toLowerCase(Locale.ROOT);
        for(String x:labels){String q=x.toLowerCase(Locale.ROOT);if(t.contains(q)||d.contains(q))return n;}
        for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo r=find(n.getChild(i),labels);if(r!=null)return r;}return null;
    }
    private String getRootPackage(){AccessibilityNodeInfo r=getRootInActiveWindow();return r==null?"":String.valueOf(r.getPackageName());}
    private void finish(Result cb,boolean ok,String detail){operation=false;cb.done(ok,detail);}
}
