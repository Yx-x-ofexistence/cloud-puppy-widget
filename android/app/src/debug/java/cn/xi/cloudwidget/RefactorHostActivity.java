package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;

/** QA only: preview and real RemoteViews at identical non-standard host sizes. */
public class RefactorHostActivity extends Activity {
    AppWidgetHost host;
    @Override public void onCreate(Bundle b){super.onCreate(b);host=new AppWidgetHost(this,772);host.deleteHost();LinearLayout root=Ui.column(this);Ui.root(this,root);root.setBackgroundColor(0xffe8e4df);root.addView(Controls.heading(this,"布局回归 · MuMu / API 32"));root.addView(Controls.hint(this,"左：编辑预览    右：真实 Widget 宿主"));String mode=getIntent().getStringExtra("mode");
        for(int n=0;n<2;n++){
            Card card=Card.preset(1,0);card.shape=n;card.id="v030-host-"+mode+"-"+n;card.title="";card.body="慢慢来，也在向前走。";
            if("none".equals(mode)){card.stickers=new JSONArray();card.grid=false;card.border=0;card.body="";}
            else if("photo".equals(mode)){card.type=1;Card photo=new Store(this).get("v030-photo");card.photo=photo==null?"qa-copy.png":photo.photo;card.stickers=new JSONArray();card.grid=false;card.border=0;}
            else if("multi".equals(mode)){card.type=4;card.title=n==0?"今天的小计划":"另一张独立卡片";card.stickers=new JSONArray();card.addSticker("builtin:blue-book");try{card.stickers.getJSONObject(0).put("width",.20).put("x",.85).put("y",.86);}catch(JSONException e){throw new IllegalStateException(e);}}
            Store store=new Store(this);Card old=store.get(card.id);if(old!=null)card.history=old.history;store.save(card);
            int width=n==0?146:310,height=n==0?180:150;root.addView(Controls.hint(this,n==0?"2×2 · 146 : 180":"4×2 · 310 : 150（上预览 / 下真实 Widget）"));LinearLayout row=new LinearLayout(this);if(n==1)row.setOrientation(LinearLayout.VERTICAL);row.setGravity(Gravity.CENTER);CardPreview preview=new CardPreview(this,card);preview.targetWidth=width;preview.targetHeight=height;row.addView(preview,new LinearLayout.LayoutParams(Ui.dp(this,width),Ui.dp(this,height)));
            int id=host.allocateAppWidgetId();AppWidgetManager manager=AppWidgetManager.getInstance(this);ComponentName provider=new ComponentName(this,n==0?SmallWidget.class:WideWidget.class);if(!manager.bindAppWidgetIdIfAllowed(id,provider)){root.addView(Controls.hint(this,"宿主绑定权限不可用"));continue;}store.bind(id,card.id);Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,width);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,width);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,height);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,height);manager.updateAppWidgetOptions(id,options);AppWidgetHostView view=host.createView(this,id,manager.getAppWidgetInfo(id));view.setPadding(0,0,0,0);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,width),Ui.dp(this,height));if(n==0)lp.leftMargin=Ui.dp(this,10);else lp.topMargin=Ui.dp(this,8);row.addView(view,lp);root.addView(row);WidgetProvider.update(this,id);Ui.gap(this,root,12);
        }host.startListening();
    }
    @Override public void onDestroy(){host.stopListening();super.onDestroy();}
}
