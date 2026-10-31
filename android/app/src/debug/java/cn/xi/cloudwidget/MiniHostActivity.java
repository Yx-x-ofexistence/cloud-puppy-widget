package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** QA-only native 1x1 hosts with a wallpaper-like contrasting parent. */
public class MiniHostActivity extends Activity {
    AppWidgetHost host;final ArrayList<Integer> ids=new ArrayList<>();final ArrayList<AppWidgetHostView> actual=new ArrayList<>();
    @Override public void onCreate(Bundle state){super.onCreate(state);host=new AppWidgetHost(this,804);host.deleteHost();LinearLayout body=Ui.column(this);Ui.root(this,body);body.setBackgroundColor(0xffa4bac7);body.addView(Controls.heading(this,"1×1 素材 · 真实 Widget"));body.addView(Controls.hint(this,"每格 58×66 dp · 左预览 / 右桌面组件"));
        for(int n=0;n<2;n++){Card card=Card.mini();card.id="qa034-mini-"+n;card.addSticker(n==0?"builtin:blue-books-dog":"builtin:cream-flowers");new Store(this).save(card);CardPreview preview=new CardPreview(this,card);preview.targetWidth=58;preview.targetHeight=66;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);row.addView(preview,new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,66)));AppWidgetManager manager=AppWidgetManager.getInstance(this);int id=host.allocateAppWidgetId();if(!manager.bindAppWidgetIdIfAllowed(id,new ComponentName(this,MiniWidget.class)))throw new IllegalStateException("QA host binding unavailable");ids.add(id);new Store(this).bind(id,card.id);Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,58);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,58);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,66);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,66);manager.updateAppWidgetOptions(id,options);AppWidgetHostView view=host.createView(this,id,manager.getAppWidgetInfo(id));view.setPadding(0,0,0,0);actual.add(view);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,66));lp.leftMargin=Ui.dp(this,24);row.addView(view,lp);body.addView(row);Ui.gap(this,body,24);WidgetProvider.update(this,id);}host.startListening();
    }
    @Override public void onDestroy(){host.stopListening();super.onDestroy();}
}
