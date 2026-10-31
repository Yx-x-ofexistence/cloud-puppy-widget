package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.util.SizeF;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Three linked water views and an independent card; debug-only native host. */
public class WaterHostActivity extends Activity {
    AppWidgetHost host;final ArrayList<Integer> ids=new ArrayList<>();final ArrayList<AppWidgetHostView> actual=new ArrayList<>();final ArrayList<CardPreview> previews=new ArrayList<>();final ArrayList<SizeF> reported=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b);host=new AppWidgetHost(this,801);int[] old=host.getAppWidgetIds();Arrays.sort(old);boolean reuse=getIntent().getBooleanExtra("reuse",false)&&old.length==4;if(!reuse){host.deleteHost();Card linked=Card.water();linked.id="qa034-water-linked";new Store(this).save(linked);Card other=Card.water();other.id="qa034-water-independent";other.waterGoalMl=1500;new Store(this).save(other);}
        LinearLayout body=Ui.column(this);body.setPadding(Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,12));Ui.root(this,body);body.addView(Controls.heading(this,"今日喝水 · 左预览 / 右原生 Widget"));body.addView(Controls.hint(this,"前 3 个视图关联同一张卡；第 4 张独立。报告尺寸故意与实际不同。"));
        float[][] dimensions={{190,190,200,160},{160,200,155,180},{280,165,360,140},{180,180,160,200}};
        for(int n=0;n<4;n++){AppWidgetManager manager=AppWidgetManager.getInstance(this);int id;if(reuse)id=old[n];else{id=host.allocateAppWidgetId();if(!manager.bindAppWidgetIdIfAllowed(id,new ComponentName(this,n==2?WideWidget.class:SmallWidget.class)))throw new IllegalStateException("QA host binding denied");new Store(this).bind(id,n==3?"qa034-water-independent":"qa034-water-linked");Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,(int)dimensions[n][2]);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,(int)dimensions[n][2]);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,(int)dimensions[n][3]);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,(int)dimensions[n][3]);manager.updateAppWidgetOptions(id,options);}ids.add(id);reported.add(new SizeF(dimensions[n][2],dimensions[n][3]));
            Card card=new Store(this).get(new Store(this).binding(id));card.shape=n==2?1:0;CardPreview preview=new CardPreview(this,card);previews.add(preview);AppWidgetHostView view=host.createView(this,id,manager.getAppWidgetInfo(id));view.setPadding(0,0,0,0);actual.add(view);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);row.addView(preview,new LinearLayout.LayoutParams(Ui.dp(this,dimensions[n][0]),Ui.dp(this,dimensions[n][1])));LinearLayout.LayoutParams nativeSize=new LinearLayout.LayoutParams(Ui.dp(this,dimensions[n][0]),Ui.dp(this,dimensions[n][1]));nativeSize.leftMargin=Ui.dp(this,20);row.addView(view,nativeSize);body.addView(row);Ui.gap(this,body,12);WidgetProvider.update(this,id);
        }host.startListening();refreshPreviews();
    }
    void refreshPreviews(){for(int n=0;n<previews.size();n++){Card card=new Store(this).get(new Store(this).binding(ids.get(n)));card.shape=n==2?1:0;CardPreview view=previews.get(n);view.card=card;view.wide=n==2;SizeF effective=WidgetGeometry.resolve(this,ids.get(n),reported.get(n));view.targetWidth=effective.getWidth();view.targetHeight=effective.getHeight();view.refresh();}}
    @Override public void onResume(){super.onResume();if(!ids.isEmpty())refreshPreviews();}
    @Override public void onDestroy(){host.stopListening();super.onDestroy();}
}
