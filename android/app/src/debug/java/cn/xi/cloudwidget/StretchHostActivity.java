package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.util.SizeF;
import android.view.*;
import android.widget.*;
import java.util.*;

/** QA deliberately reports dimensions that differ from the real host bounds. */
public class StretchHostActivity extends Activity {
    AppWidgetHost host;final ArrayList<Integer> ids=new ArrayList<>();final ArrayList<AppWidgetHostView> actual=new ArrayList<>();final ArrayList<CardPreview> previews=new ArrayList<>();final ArrayList<SizeF> reported=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b);host=new AppWidgetHost(this,802);boolean reuse=getIntent().getBooleanExtra("reuse",false);if(!reuse)host.deleteHost();LinearLayout body=Ui.column(this);Ui.root(this,body);body.setBackgroundColor(0xffe7e3de);body.addView(Controls.heading(this,"0.3.2 · 报告尺寸 ≠ 实际尺寸"));body.addView(Controls.hint(this,"左 / 上预览，右 / 下真实 Widget"));
        try{for(int n=0;n<2;n++){
            Card card=new Card();card.id="qa034-size-"+n;card.shape=n;card.title="蓝色小狗";card.body=":-)";card.background=0xffdceaf2;card.ink=0xff2d687b;card.grid=true;card.border=2;card.borderInk=-1;
            card.addSticker("builtin:blue-books-dog");card.stickers.getJSONObject(0).put("width",.50).put("x",.68).put("y",.73);card.addSticker("builtin:blue-tape");card.stickers.getJSONObject(1).put("width",.30).put("x",.77).put("y",.13);card.addSticker("builtin:blue-sprig");card.stickers.getJSONObject(2).put("width",.17).put("x",.20).put("y",.86);if(!reuse||new Store(this).get(card.id)==null)new Store(this).save(card);else card=new Store(this).get(card.id);
            int w=n==0?190:310,h=n==0?190:180;SizeF report=new SizeF(n==0?200:360,n==0?160:140);reported.add(report);body.addView(Controls.hint(this,n==0?"2×2 · 报告 200×160 / 实际 190×190":"4×2 · 报告 360×140 / 实际 310×180"));LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);if(n==1)row.setOrientation(LinearLayout.VERTICAL);
            // Two 190dp cards need a wider canvas than some small screens; use a horizontal viewport.
            CardPreview preview=new CardPreview(this,card);preview.targetWidth=report.getWidth();preview.targetHeight=report.getHeight();previews.add(preview);row.addView(preview,new LinearLayout.LayoutParams(Ui.dp(this,w),Ui.dp(this,h)));
            AppWidgetManager manager=AppWidgetManager.getInstance(this);int id=-1;if(reuse)for(int candidate:WidgetProvider.ids(this,n==0?SmallWidget.class:WideWidget.class))if(new Store(this).binding(candidate).equals(card.id)){id=candidate;break;}boolean existing=id>=0;if(!existing)id=host.allocateAppWidgetId();ids.add(id);if(!existing&&!manager.bindAppWidgetIdIfAllowed(id,new ComponentName(this,n==0?SmallWidget.class:WideWidget.class)))throw new IllegalStateException("QA host permission unavailable");new Store(this).bind(id,card.id);Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,(int)report.getWidth());options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,(int)report.getWidth());options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,(int)report.getHeight());options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,(int)report.getHeight());if(!existing)manager.updateAppWidgetOptions(id,options);AppWidgetHostView view=host.createView(this,id,manager.getAppWidgetInfo(id));view.setPadding(0,0,0,0);actual.add(view);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,w),Ui.dp(this,h));lp.leftMargin=n==0?Ui.dp(this,10):0;lp.topMargin=n==1?Ui.dp(this,8):0;row.addView(view,lp);
            if(n==0){HorizontalScrollView viewport=new HorizontalScrollView(this);viewport.addView(row);body.addView(viewport);}else body.addView(row);WidgetProvider.update(this,id);Ui.gap(this,body,12);
        }host.startListening();}catch(Exception e){throw new IllegalStateException(e);}
    }
    @Override public void onResume(){super.onResume();for(int n=0;n<previews.size();n++){SizeF effective=WidgetGeometry.resolve(this,ids.get(n),reported.get(n));CardPreview preview=previews.get(n);preview.targetWidth=effective.getWidth();preview.targetHeight=effective.getHeight();preview.refresh();}}
    @Override public void onDestroy(){host.stopListening();super.onDestroy();}
}
