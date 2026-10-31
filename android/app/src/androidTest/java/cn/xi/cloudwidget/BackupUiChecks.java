package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.DocumentsContract;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class BackupUiChecks extends BackupChecks {
    Object get(Object value,String name)throws Exception{Field field=BackupActivity.class.getDeclaredField(name);field.setAccessible(true);return field.get(value);}
    void main(Runnable action){runOnMainSync(action);}
    String status(BackupActivity activity)throws Exception{return ((TextView)get(activity,"status")).getText().toString();}
    void until(BackupActivity activity,String expected)throws Exception{long deadline=SystemClock.uptimeMillis()+40000;while(SystemClock.uptimeMillis()<deadline){waitForIdleSync();if(status(activity).contains(expected))return;SystemClock.sleep(100);}throw new AssertionError("Status did not reach "+expected+": "+status(activity));}
    Uri uri(String id){return DocumentsContract.buildDocumentUri("cn.xi.cloudwidget.qadocs",id);}
    @Override public void onStart(){target=getTargetContext();Bundle result=new Bundle();try{
        BackupActivity screen=(BackupActivity)startActivitySync(new Intent(target,BackupActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();String before=state(target);int originalCount=new Store(target).all().size();
        for(String mode:new String[]{"success","cancel","fail","short"}){
            Uri document=uri("qa034-"+mode);IntentFilter filter=new IntentFilter(Intent.ACTION_CREATE_DOCUMENT);filter.addDataType("application/zip");filter.addCategory(Intent.CATEGORY_OPENABLE);ActivityMonitor monitor=addMonitor(filter,new ActivityResult(mode.equals("cancel")?Activity.RESULT_CANCELED:Activity.RESULT_OK,new Intent().setData(document)),true);
            main(screen::beginExport);until(screen,mode.equals("success")?"保存成功":mode.equals("cancel")?"已取消":"未成功导出");check(monitor.getHits()==1,"native export requests a system ZIP document "+mode);removeMonitor(monitor);
            check(before.equals(state(target)),"export success/cancellation/provider errors preserve card preferences "+mode);
            if(mode.equals("success")){try(InputStream in=target.getContentResolver().openInputStream(document);Backup.Plan plan=Backup.prepare(target,in)){check(plan.cards.length()==new Store(target).all().size(),"actual export document passes full validation and covers existing saved cards");}}
            if(mode.equals("cancel"))check(get(screen,"generated")==null,"cancelled system document request cleans only the generated temporary export");
            if(mode.equals("fail")||mode.equals("short")){boolean exists=true;try(InputStream in=target.getContentResolver().openInputStream(document)){}catch(FileNotFoundException expected){exists=false;}check(!exists,"failed or disconnected provider document is removed "+mode);}
        }
        File temporary=Backup.export(target);for(String interrupted:new String[]{"export-first-write","export-after-verify"}){Uri doc=uri("qa034-interrupted-"+interrupted);try{Backup.exportDocument(target,temporary,doc,phase->{if(phase.equals(interrupted))throw new SimulatedCrash();});}catch(SimulatedCrash expected){}check(Backup.exportJournal(target).getBaseFile().exists(),"interrupted export keeps durable document journal "+interrupted);Backup.recoverExport(target);check(!Backup.exportJournal(target).getBaseFile().exists(),"export recovery clears journal "+interrupted);boolean exists=true;try(InputStream saved=target.getContentResolver().openInputStream(doc)){if(interrupted.equals("export-after-verify"))check(Backup.hash(saved).equals(Backup.hash(temporary)),"export recovery preserves a completed verified document");}catch(FileNotFoundException missing){exists=false;}check(exists==interrupted.equals("export-after-verify"),"export recovery removes only an incomplete new document "+interrupted);}Backup.remove(temporary.getParentFile());
        Isolated source=new Isolated(target);fixture(source);Store store=new Store(source);for(Card card:store.all()){String previous=card.id;card.id="qa034-ui-"+UUID.randomUUID();store.save(card);store.delete(previous);}File archive=Backup.export(source);Uri incoming=uri("qa034-incoming");try(OutputStream out=target.getContentResolver().openOutputStream(incoming,"w");InputStream in=new FileInputStream(archive)){Io.copy(in,out);}
        main(()->screen.onActivityResult(41,Activity.RESULT_OK,new Intent().setData(incoming)));until(screen,"等待确认");check(before.equals(state(target)),"native import validates a document before changing saved cards");AlertDialog confirmation=(AlertDialog)get(screen,"confirmation");check(confirmation.isShowing(),"native import displays explicit merge confirmation");final AlertDialog cancelled=confirmation;main(()->cancelled.getButton(AlertDialog.BUTTON_NEGATIVE).performClick());check(before.equals(state(target)),"native Cancel preserves all cards and bindings");
        main(()->screen.onActivityResult(41,Activity.RESULT_OK,new Intent().setData(incoming)));until(screen,"等待确认");confirmation=(AlertDialog)get(screen,"confirmation");final AlertDialog accepted=confirmation;main(()->accepted.getButton(AlertDialog.BUTTON_POSITIVE).performClick());until(screen,"恢复完成");check(new Store(target).all().size()==new Store(source).all().size()+originalCount,"native merge confirmation publishes all new cards");String after=state(target);main(()->screen.onActivityResult(41,Activity.RESULT_OK,new Intent().setData(incoming)));until(screen,"已经恢复过");check(after.equals(state(target)),"native repeated import clearly reports duplication without adding cards");Backup.remove(archive.getParentFile());main(screen::finish);
        result.putString("stream",report+"Checks passed this run: "+count+"\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){result.putString("stream",report+"FAIL "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
    int countCards(String ignored,Context context){int count=0;for(Card card:new Store(context).all())if(!card.id.startsWith("qa034-ui-"))count++;return count;}
}
