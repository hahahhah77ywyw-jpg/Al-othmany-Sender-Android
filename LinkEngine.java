package com.example.alothmanysender;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Single-flight persistent queue. A link is advanced only after the accessibility bridge
 * returns a terminal result. Ambiguous results are retried, then deferred once, then failed.
 */
public final class LinkEngine {
    public interface Listener { void onUpdate(Status s); }
    public static final class Status {
        public final int total,done,failed,deferred,index,attempt;
        public final String current,phase; public final boolean running,paused;
        Status(int t,int d,int f,int x,int i,int a,String c,String ph,boolean r,boolean p){total=t;done=d;failed=f;deferred=x;index=i;attempt=a;current=c;phase=ph;running=r;paused=p;}
    }
    private final AppState state; private final Handler main=new Handler(Looper.getMainLooper());
    private Listener listener; private boolean busy; private int deferred;
    public LinkEngine(AppState s){state=s;}
    public void setListener(Listener l){listener=l;emit("جاهز","");}
    public void start(List<String> input){
        if(busy||state.running())return;
        ArrayList<String> clean=new ArrayList<>();Set<String> seen=new LinkedHashSet<>();
        for(String raw:input){String u=raw==null?"":raw.trim();if(u.startsWith("https://chat.whatsapp.com/")&&seen.add(u))clean.add(u);}
        state.links(clean);state.resetRun();state.running(true);state.paused(false);busy=true;deferred=0;emit("بدء","");processNext();
    }
    public void pause(){state.paused(true);emit("متوقف مؤقتاً",state.index()<state.links().size()?state.links().get(state.index()):"");}
    public void resume(){if(!state.running())return;state.paused(false);if(!busy){busy=true;processNext();}else emit("استئناف","");}
    public void stop(){state.running(false);state.paused(false);busy=false;emit("متوقف","");}
    private void processNext(){
        if(!state.running()){busy=false;emit("متوقف","");return;}
        if(state.paused()){busy=false;emit("متوقف مؤقتاً","");return;}
        List<String> q=state.links();int i=state.index();while(i<q.size()&&state.isProcessed(q.get(i)))i++;
        if(i>=q.size()){state.index(i);state.running(false);busy=false;emit("اكتمل","");return;}
        state.index(i);String link=q.get(i);int a=state.attempts(link)+1;state.attempts(link,a);emit("فتح الرابط",link);
        SenderAccessibilityService svc=SenderAccessibilityService.get();
        if(svc==null){retryOrDefer(link,"خدمة الوصول غير مفعلة");return;}
        svc.processInvite(link,state.selectedPackage(),state.publishEnabled()?state.message():"",(result,detail)->main.post(()->terminal(result,detail)));
    }
    private void retryOrDefer(String link,String detail){
        int a=state.attempts(link);
        if(a<3){emit("إعادة محاولة",detail);main.postDelayed(this::processNext,900);return;}
        if(deferred<state.links().size()){
            deferred++;ArrayList<String> q=new ArrayList<>(state.links());int i=state.index();if(i<q.size()){String x=q.remove(i);q.add(x);state.links(q);state.index(i);}emit("تأجيل ذكي",detail);main.postDelayed(this::processNext,450);return;
        }
        state.markFailed(link);state.markProcessed(link);state.clearAttempts(link);state.index(state.index()+1);emit("فشل",detail);main.postDelayed(this::processNext,450);
    }
    private void terminal(boolean success,String detail){
        List<String> q=state.links();int i=state.index();if(i>=q.size()){busy=false;return;}String link=q.get(i);
        if(success){state.markProcessed(link);state.clearAttempts(link);state.index(i+1);emit("تم",detail);main.postDelayed(this::processNext,450);}
        else retryOrDefer(link,detail);
    }
    private void emit(String phase,String current){if(listener==null)return;List<String>q=state.links();int d=state.processed().size();int f=state.failed().size();listener.onUpdate(new Status(q.size(),d,f,deferred,state.index(),state.index()<q.size()?state.attempts(q.get(state.index())):0,current,phase,state.running(),state.paused()));}
}
