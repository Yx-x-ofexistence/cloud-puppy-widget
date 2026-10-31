package cn.xi.cloudwidget;
import android.appwidget.*;
import android.content.*;
import org.json.*;
public class PinTestReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent){int id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);if(id<0)return;Store store=new Store(c);if(!store.binding(id).isEmpty())return;Card card=new Card();card.id="launcher-qa-"+id;card.title="素材包 · 桌面打卡";card.type=4;card.background=intent.getBooleanExtra("wide",false)?0xffe5f0f8:0xfffff2e3;card.addSticker("pack:cloud-sample-pack@1.0.0:blue-book");try{card.stickers.getJSONObject(0).put("width",.22).put("x",.82).put("y",.85);}catch(JSONException e){throw new IllegalStateException(e);}store.save(card);store.bind(id,card.id);WidgetProvider.update(c,id);}
}
