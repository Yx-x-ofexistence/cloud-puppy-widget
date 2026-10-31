package cn.xi.cloudwidget;
import android.appwidget.*;
import android.content.*;
import java.util.UUID;

/** Private callback from the user's system pin confirmation. */
public class PinReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        int widget=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);AppWidgetManager manager=AppWidgetManager.getInstance(context);AppWidgetProviderInfo info=manager.getAppWidgetInfo(widget);
        if(info==null||!info.provider.getPackageName().equals(context.getPackageName()))return;
        Store store=new Store(context);store.clearPin();if(!store.binding(widget).isEmpty()){WidgetProvider.update(context,widget);return;}Card source=store.get(intent.getStringExtra("card"));if(source==null)return;
        if(source.type==5&&WidgetProvider.shape(info)!=2){store.bind(widget,source.id);WidgetProvider.update(context,widget);return;}
        Card copy=Card.from(source.json().toString());copy.id=UUID.randomUUID().toString();copy.shape=WidgetProvider.shape(info);store.save(copy);store.bind(widget,copy.id);WidgetProvider.update(context,widget);
    }
}
