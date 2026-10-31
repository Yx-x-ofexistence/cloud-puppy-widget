package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/** Real RemoteViews at the same dimensions as each adjacent preview. */
public class ReviewHostActivity extends Activity {
    AppWidgetHost host;
    @Override public void onCreate(Bundle b){super.onCreate(b);host=new AppWidgetHost(this,803);host.deleteHost();LinearLayout body=Ui.column(this);Ui.root(this,body);body.setBackgroundColor(0xffe7e3de);body.addView(Controls.heading(this,"0.3.1 · 遮挡顺序回归"));body.addView(Controls.hint(this,"时间 / 日期在贴纸上方，正文在贴纸下方"));
        try{for(int n=0;n<2;n++){
            Card card=ReviewFixtures.overlap(this,n);new Store(this).save(card);int width=n==0?146:310,height=n==0?180:150;body.addView(Controls.hint(this,n==0?"2×2 · 左预览 / 右真实 Widget":"4×2 · 上预览 / 下真实 Widget"));LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);if(n==1)row.setOrientation(LinearLayout.VERTICAL);CardPreview preview=new CardPreview(this,card);preview.targetWidth=width;preview.targetHeight=height;row.addView(preview,new LinearLayout.LayoutParams(Ui.dp(this,width),Ui.dp(this,height)));
            AppWidgetManager manager=AppWidgetManager.getInstance(this);int widget=host.allocateAppWidgetId();if(!manager.bindAppWidgetIdIfAllowed(widget,new ComponentName(this,n==0?SmallWidget.class:WideWidget.class)))throw new IllegalStateException("QA host binding unavailable");new Store(this).bind(widget,card.id);Bundle options=new Bundle();options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,width);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,width);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,height);options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,height);manager.updateAppWidgetOptions(widget,options);AppWidgetHostView actual=host.createView(this,widget,manager.getAppWidgetInfo(widget));actual.setPadding(0,0,0,0);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,width),Ui.dp(this,height));if(n==0)lp.leftMargin=Ui.dp(this,10);else lp.topMargin=Ui.dp(this,8);row.addView(actual,lp);body.addView(row);WidgetProvider.update(this,widget);Ui.gap(this,body,12);
        }host.startListening();}catch(Exception e){throw new IllegalStateException(e);}
    }
    @Override public void onDestroy(){host.stopListening();super.onDestroy();}
}
