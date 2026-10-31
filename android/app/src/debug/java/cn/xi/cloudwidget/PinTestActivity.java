package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.widget.*;
/** Invokes the existing launcher only; never changes the default launcher. Debug only. */
public class PinTestActivity extends Activity {
    @Override public void onCreate(Bundle b){super.onCreate(b);LinearLayout root=Ui.column(this);Ui.root(this,root);root.addView(Ui.text(this,"系统桌面验证 · Debug",22));for(Class<?> provider:new Class<?>[]{SmallWidget.class,WideWidget.class})for(int widgetId:WidgetProvider.ids(this,provider)){android.os.Bundle options=AppWidgetManager.getInstance(this).getAppWidgetOptions(widgetId);options.keySet();android.util.Log.i("CloudWidgetGeometry",widgetId+" "+options);root.addView(Ui.text(this,widgetId+" "+options,12));}for(int n=0;n<2;n++){final boolean wide=n==1;root.addView(Ui.button(this,wide?"请求添加 4×2":"请求添加 2×2",()->{AppWidgetManager m=AppWidgetManager.getInstance(this);if(!m.isRequestPinAppWidgetSupported()){root.addView(Ui.text(this,"当前桌面不支持请求添加",16));return;}Intent callback=new Intent(this,PinTestReceiver.class).putExtra("wide",wide);PendingIntent pi=PendingIntent.getBroadcast(this,wide?7712:7711,callback,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);boolean sent=m.requestPinAppWidget(new ComponentName(this,wide?WideWidget.class:SmallWidget.class),null,pi);root.addView(Ui.text(this,"请求结果："+sent,14));}));}}
}
