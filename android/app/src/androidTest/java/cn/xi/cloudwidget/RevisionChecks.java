package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import org.json.*;
import java.io.*;
import java.time.*;
public class RevisionChecks extends Instrumentation {
    Bundle args;int count;StringBuilder report=new StringBuilder();
    @Override public void onCreate(Bundle b){args=b;start();}
    void check(boolean ok,String name){if(!ok)throw new AssertionError(name);count++;report.append("PASS ").append(name).append('\n');}
    @Override public void onStart(){Bundle out=new Bundle();try{Context c=getTargetContext();Store store=new Store(c);File dir=new File(c.getFilesDir(),"revision-previews");dir.mkdirs();
        Card legacy=store.get("qa-persist");check(legacy!=null&&legacy.done(0,LocalDate.of(2026,10,27)),"upgrade: legacy ID and daily history retained");check(legacy.photo.equals("qa-copy.png")&&Renderer.photo(c,legacy.photo)!=null,"upgrade: private photo retained");check(legacy.stickers.length()==1&&legacy.stickers.getJSONObject(0).getString("asset").equals("builtin:concept-peek"),"upgrade: old skin migrated to optional sticker");
        check(new Card().stickers.length()==0&&!new Card().grid&&new Card().border==0,"blank card has no forced decoration");
        check(new AssetStore(c).all().size()>=34,"complete built-in library includes both original sheets");
        Card[] cards=new Card[6];
        cards[0]=new Card();cards[0].type=0;
        cards[1]=new Card();cards[1].type=1;cards[1].photo=legacy.photo;
        cards[2]=Card.preset(0,2);cards[2].addSticker("builtin:cream-stars");cards[2].stickers.getJSONObject(1).put("x",.86).put("y",.12).put("width",.14);
        cards[3]=Card.preset(1,3);cards[3].background=0xfffbe2e4;cards[3].ink=0xff6f4662;cards[3].grid=false;cards[3].body="蓝色狗狗，也可以搭配粉色纸张。";
        cards[4]=Card.preset(0,4);cards[4].background=0xffe1eff7;cards[4].borderInk=0xff7199b7;
        cards[5]=new Card();cards[5].type=3;cards[5].bgMode=1;cards[5].body="空白和透明，也都是完整的选择。";
        for(int n=0;n<6;n++)for(int shape=0;shape<2;shape++){Bitmap image=Renderer.render(c,cards[n],shape==0?440:880,440,true);check(image.getAllocationByteCount()<=2016000,"preview bitmap budget "+n+"/"+shape);try(FileOutputStream f=new FileOutputStream(new File(dir,n+"-"+shape+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,f);}image.recycle();}
        for(AssetStore.Asset asset:new AssetStore(c).all()){Bitmap b=new AssetStore(c).bitmap(asset,180);check(b!=null&&b.hasAlpha(),"asset decode and alpha: "+asset.id);b.recycle();}
        out.putString("stream",report+"Checks passed this run: "+count+"\n");finish(Activity.RESULT_OK,out);
    }catch(Throwable e){out.putString("stream",report+"FAIL "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,out);}}
}
