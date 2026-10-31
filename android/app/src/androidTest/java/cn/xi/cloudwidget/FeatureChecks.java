package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import java.io.*;

public class FeatureChecks extends MiniChecks {
    String bitmapHash(Bitmap bitmap)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.PNG,100,out);return AssetStore.hash(out.toByteArray());}
    @Override public void onStart(){if(!"features".equals(args.getString("phase"))){super.onStart();return;}Bundle result=new Bundle();try{c=getTargetContext();
        StretchHostActivity host=(StretchHostActivity)startActivitySync(new Intent(c,StretchHostActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
        for(int id=4;id<7;id++){final int font=id;EditorActivity editor=(EditorActivity)startActivitySync(new Intent(c,PortraitEditorActivity.class).putExtra("type",3).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();Card card=(Card)field("card").get(editor);main(()->{try{field("tab").setInt(editor,2);editor.showPanel();}catch(Exception error){throw new RuntimeException(error);}});View tile=find(editor.getWindow().getDecorView(),"字体："+Card.FONTS[id]);check(tile!=null,"actual selector exposes added font "+id);main(tile::performClick);check(card.font==id,"native font tile updates draft "+id);card.id="qa034-font-"+id;card.title=Card.FONTS[id];card.body="今天也要好好生活\n0123456789，。！？\n长句与缺字回退：𠮷 ☁️";main(editor::save);waitForIdleSync();EditorActivity reopened=(EditorActivity)startActivitySync(new Intent(c,PortraitEditorActivity.class).putExtra("card",card.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));check(((Card)field("card").get(reopened)).font==id,"saved font survives reopening "+id);main(reopened::finish);waitForIdleSync();
            for(int n=0;n<2;n++){int widget=host.ids.get(n);Card bound=Card.from(card.json().toString());bound.shape=n;bound.id="qa034-native-font-"+id+"-"+n;new Store(c).save(bound);new Store(c).bind(widget,bound.id);WidgetProvider.update(c,widget);long deadline=SystemClock.uptimeMillis()+5000;boolean matched=false;while(SystemClock.uptimeMillis()<deadline){android.widget.ImageView image=ready(host.actual.get(n));Bitmap nativeBitmap=((BitmapDrawable)image.getDrawable()).getBitmap();Bitmap expected=Renderer.render(c,bound,nativeBitmap.getWidth(),nativeBitmap.getHeight(),false);matched=bitmapHash(expected).equals(bitmapHash(nativeBitmap));expected.recycle();if(matched)break;SystemClock.sleep(100);}check(matched,"real native widget bitmap uses selected font "+id+" shape "+n);}
        }
        IconQAActivity icons=(IconQAActivity)startActivitySync(new Intent(c,IconQAActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();check(getTargetContext().getApplicationInfo().icon==R.mipmap.ic_launcher,"installed application uses the new adaptive icon");for(int mask=1;mask<=2;mask++){Bitmap image=icons.icon(108,mask);check(Color.alpha(image.getPixel(0,0))==0&&Color.alpha(image.getPixel(54,54))==255,"icon mask keeps transparent corners and readable central artwork "+mask);image.recycle();}
        result.putString("stream",report+"Checks passed this run: "+count+"\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){result.putString("stream",report+"FAIL "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
}
