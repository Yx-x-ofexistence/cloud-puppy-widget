package cn.xi.cloudwidget;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import java.io.*;
import java.time.*;

/** No third-party test libraries; runs on the emulator's Android framework. */
public class OfflineChecks extends Instrumentation {
    int checks=0;StringBuilder report=new StringBuilder();Bundle args;
    @Override public void onCreate(Bundle b){super.onCreate(b);args=b;start();}
    void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;report.append("PASS ").append(label).append('\n');}
    @Override public void onStart(){Bundle result=new Bundle();try{
        Context c=getTargetContext();Store store=new Store(c);
        if("ui".equals(args.getString("phase"))){
            EditorActivity editor=(EditorActivity)startActivitySync(new Intent(c,EditorActivity.class).putExtra("type",1).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            java.lang.reflect.Field field=EditorActivity.class.getDeclaredField("card");field.setAccessible(true);
            File source=new File(c.getCacheDir(),"editor-import.png");Bitmap image=Bitmap.createBitmap(200,120,Bitmap.Config.ARGB_8888);new Canvas(image).drawColor(0xffa5c1d4);try(FileOutputStream out=new FileOutputStream(source)){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();
            runOnMainSync(()->editor.onActivityResult(12,Activity.RESULT_OK,new Intent().setData(android.net.Uri.fromFile(source))));
            Card imported=(Card)field.get(editor);check(!imported.photo.isEmpty(),"actual editor image import creates private filename");String filename=imported.photo;
            runOnMainSync(()->editor.onActivityResult(12,Activity.RESULT_CANCELED,null));check(imported.photo.equals(filename),"cancel selection retains old image");
            source.delete();check(Renderer.photo(c,filename)!=null,"editor imported copy survives deletion of original");
            imported.title="实际编辑保存验证";runOnMainSync(editor::save);check(store.get(imported.id)!=null&&store.get(imported.id).title.equals(imported.title),"editor saves card configuration");
            Card concurrent=new Card();concurrent.id="qa-edit-merge";concurrent.type=4;store.save(concurrent);
            EditorActivity editing=(EditorActivity)startActivitySync(new Intent(c,EditorActivity.class).putExtra("card",concurrent.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            concurrent.toggle(0,LocalDate.now());store.save(concurrent);Card draft=(Card)field.get(editing);draft.title="编辑保留桌面最新打卡";runOnMainSync(editing::save);
            check(store.get(concurrent.id).done(0,LocalDate.now()),"editor does not overwrite newer widget history");
            check(store.get(concurrent.id).title.equals(draft.title),"editor persists text change");
        }
        else if("verify".equals(args.getString("phase"))){Card saved=store.get("qa-persist");check(saved!=null,"configuration survives process restart");check(saved.done(0,LocalDate.of(2026,10,27)),"history survives process restart");check(saved.photo.equals("qa-copy.png")&&Renderer.photo(c,saved.photo)!=null,"imported photo copy survives restart");}
        else {
            LocalDate day=LocalDate.of(2026,10,27);
            check(Card.countdown("2026-10-27",day).equals("就是今天"),"countdown today");check(Card.countdown("2026-10-26",day).equals("已过 1 天"),"countdown expired");check(Card.countdown("2026-10-28",day).equals("还有 1 天"),"countdown future");check(Card.countdown("2024-03-01",LocalDate.of(2024,2,28)).equals("还有 2 天"),"leap year calendar day arithmetic");
            Card a=new Card();a.id="qa-persist";a.toggle(0,day);check(a.done(0,day),"check-in complete");a.toggle(0,day);check(!a.done(0,day),"check-in undo");a.toggle(0,day);check(!a.done(0,day.plusDays(1)),"new day starts incomplete");a.toggle(1,day.plusDays(1));check(a.done(0,day)&&a.done(1,day.plusDays(1)),"new day preserves previous history");
            Card b=new Card();b.id="qa-second";b.title="另一张卡片";store.save(b);store.save(a);a.title="只改第一张";store.save(a);check(store.get(b.id).title.equals("另一张卡片"),"independent multi-card configuration");store.bind(88001,a.id);store.bind(88002,b.id);check(!store.binding(88001).equals(store.binding(88002)),"independent widget bindings");
            Card round=Card.from(a.json().toString());check(round.done(0,day)&&round.itemIds[0].equals(a.itemIds[0]),"JSON history and stable item IDs roundtrip");
            File dir=new File(c.getFilesDir(),"photos");dir.mkdirs();File original=new File(c.getCacheDir(),"qa-original.png"),copy=new File(dir,"qa-copy.png");Bitmap photo=Bitmap.createBitmap(160,100,Bitmap.Config.ARGB_8888);new Canvas(photo).drawColor(0xff8db6d4);try(FileOutputStream o=new FileOutputStream(original)){photo.compress(Bitmap.CompressFormat.PNG,100,o);}try(FileInputStream in=new FileInputStream(original);FileOutputStream out=new FileOutputStream(copy)){Io.copy(in,out);}photo.recycle();original.delete();a.photo=copy.getName();store.save(a);check(Renderer.photo(c,a.photo)!=null,"photo copy works after source deletion");
            File renders=new File(c.getFilesDir(),"qa-renders");renders.mkdirs();
            for(int type=0;type<5;type++)for(int skin=0;skin<2;skin++)for(int size=0;size<2;size++){
                Card v=Card.from(a.json().toString());v.type=type;v.skin=skin;v.background=skin==0?0xfffff2e3:0xffedf3f6;v.title="给今天的小小温柔";v.body="中文长句：把平凡的小日子过好，认真吃饭，慢慢前进。标点、数字 2026 与生僻字 龘。";v.date=LocalDate.now().toString();v.items[0]="读书十分钟，让今天多一点进步";
                Bitmap image=Renderer.render(c,v,size==0?420:840,420,true);check(image.getAllocationByteCount()<=2016000,"bitmap budget "+type+"/"+skin+"/"+size);try(FileOutputStream out=new FileOutputStream(new File(renders,type+"-"+skin+"-"+size+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}image.recycle();
            }
            for(int font=0;font<4;font++)check(Renderer.font(c,font)!=null,"offline font "+Card.FONTS[font]);
            Card transparent=new Card();transparent.type=3;transparent.opacity=0;transparent.border=0;transparent.skin=0;Bitmap zero=Renderer.render(c,transparent,400,400,true);check(Color.alpha(zero.getPixel(200,200))==0,"zero paper opacity stays transparent");int opaque=0;for(int y=270;y<395;y++)for(int x=180;x<390;x++)if(Color.alpha(zero.getPixel(x,y))>200)opaque++;check(opaque>100,"dog remains visible at zero paper opacity");zero.recycle();
            store.delete(b.id);check(store.get(a.id)!=null,"deleting another card preserves first");check(store.binding(88002).isEmpty(),"delete clears only deleted card binding");store.unbind(88001);
        }
        result.putString("stream",report+"\nChecks passed: "+checks+"\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable e){result.putString("stream",report+"\nFAIL "+e+"\n"+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}
