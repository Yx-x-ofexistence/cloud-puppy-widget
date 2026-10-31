package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.time.*;

/** Debug-only real RemoteViews host. Not packaged in the candidate release APK. */
public class WidgetHostActivity extends Activity {
    AppWidgetHost host;
    @Override public void onCreate(Bundle b){super.onCreate(b);host=new AppWidgetHost(this,771);host.deleteHost();LinearLayout root=Ui.column(this);root.setBackgroundColor(0xffe6d9cb);Ui.root(this,root);root.addView(Ui.text(this,"验证宿主 · 真实 AppWidget",23));
        String mode=getIntent().getStringExtra("mode");Store store=new Store(this);
        for(int n=0;n<2;n++){boolean wide=n==1;Card card=Card.preset(n,4);card.id="host-"+n;Card existing=store.get(card.id);if(existing!=null){card.history=existing.history;card.itemIds=existing.itemIds;}
            if("clock".equals(mode)){card.type=0;card.title="";card.body="";card.stickers=new org.json.JSONArray();card.grid=false;card.border=0;}else if("countdown".equals(mode)){card.type=2;card.date=LocalDate.now().plusDays(n==0?0:-2).toString();card.title=n==0?"期待的小日子":"已经走过的日子";}else if("note".equals(mode)){card.type=3;card.title="给努力的你";card.body="今天也有好好向前走。就算只是小小一步，也值得被温柔地记住。";}else if("pack".equals(mode)){card.type=3;card.title="离线素材包";card.body="导入后，可以自己组合。";card.stickers=new org.json.JSONArray();card.addSticker("pack:cloud-sample-pack@1.0.0:"+(n==0?"blue-book":"cream-flowers"));try{card.stickers.getJSONObject(0).put("width",n==0?.32:.18).put("x",.80).put("y",.86);}catch(org.json.JSONException e){throw new IllegalStateException(e);}}else if("photo".equals(mode)){card.type=1;card.title="";card.photo="qa-copy.png";card.stickers=new org.json.JSONArray();card.grid=false;card.border=0;}else{card.type=4;card.title=wide?"今天也要好好生活":"今日小计划";}
            card.skin=n;card.background=n==0?0xfffff2e3:0xffedf3f6;card.ink=n==0?0xff674e45:0xff465c6d;store.save(card);
            int id=host.allocateAppWidgetId();ComponentName provider=new ComponentName(this,wide?WideWidget.class:SmallWidget.class);AppWidgetManager manager=AppWidgetManager.getInstance(this);if(!manager.bindAppWidgetIdIfAllowed(id,provider)){root.addView(Ui.text(this,"需要 shell appwidget grantbind",16));continue;}
            store.bind(id,card.id);Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,wide?330:170);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,170);manager.updateAppWidgetOptions(id,options);
            AppWidgetHostView view=host.createView(this,id,manager.getAppWidgetInfo(id));view.setPadding(0,0,0,0);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,wide?330:170),Ui.dp(this,170));lp.gravity=android.view.Gravity.CENTER_HORIZONTAL;root.addView(view,lp);Ui.gap(this,root,20);WidgetProvider.update(this,id);
        }
        host.startListening();
    }
    @Override public void onDestroy(){host.stopListening();super.onDestroy();}
}
