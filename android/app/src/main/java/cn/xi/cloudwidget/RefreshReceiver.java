package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import java.time.*;

public class RefreshReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){
        if("cn.xi.cloudwidget.TOGGLE".equals(i.getAction())){
            Store store=new Store(c);Card card=store.get(store.binding(i.getIntExtra("widget",-1)));
            // An old launcher view may survive midnight. Refresh it, never apply its tap to yesterday or a new day.
            if(card!=null&&LocalDate.now().toString().equals(i.getStringExtra("day"))){card.toggle(i.getIntExtra("index",-1),LocalDate.now());store.save(card);}
        }
        WidgetProvider.all(c);
    }
    static void schedule(Context c){
        if(WidgetProvider.ids(c,MiniWidget.class).length+WidgetProvider.ids(c,SmallWidget.class).length+WidgetProvider.ids(c,WideWidget.class).length==0)return;
        AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        long at=LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        PendingIntent pi=PendingIntent.getBroadcast(c,9000,new Intent(c,RefreshReceiver.class).setAction("cn.xi.cloudwidget.DAY"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        a.setAndAllowWhileIdle(AlarmManager.RTC,at,pi);
    }
}
