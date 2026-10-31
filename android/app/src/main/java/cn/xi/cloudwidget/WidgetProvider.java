package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.content.res.Configuration;
import android.os.*;
import android.util.SizeF;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.util.*;

public class WidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,id);RefreshReceiver.schedule(c);}
    @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle b){update(c,id);}
    @Override public void onDeleted(Context c,int[] ids){Store store=new Store(c);for(int id:ids){store.unbind(id);WidgetGeometry.remove(c,id);}}
    static int[] ids(Context c,Class<?> type){return AppWidgetManager.getInstance(c).getAppWidgetIds(new ComponentName(c,type));}
    static int shape(AppWidgetProviderInfo info){return info!=null&&info.provider.getClassName().endsWith("MiniWidget")?2:info!=null&&info.provider.getClassName().endsWith("WideWidget")?1:0;}
    static Class<?> provider(int shape){return shape==2?MiniWidget.class:shape==1?WideWidget.class:SmallWidget.class;}
    public static void all(Context c){for(int id:ids(c,MiniWidget.class))update(c,id);for(int id:ids(c,SmallWidget.class))update(c,id);for(int id:ids(c,WideWidget.class))update(c,id);RefreshReceiver.schedule(c);}
    static PendingIntent edit(Context c,int id,String card){return PendingIntent.getActivity(c,id,new Intent(c,EditorActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("card",card).setData(android.net.Uri.parse("cloudwidget://edit/"+id)),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static PendingIntent measuredEdit(Context c,int id,String card,SizeF reported){
        Intent intent=new Intent(c,EditorActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("card",card).putExtra("measureWidget",true).putExtra("reportedWidth",reported.getWidth()).putExtra("reportedHeight",reported.getHeight()).putExtra("geometryStamp",WidgetGeometry.stamp(c,id)).putExtra("geometryDensity",c.getResources().getDisplayMetrics().density).setData(android.net.Uri.parse("cloudwidget://edit/"+id+"/root/"+WidgetGeometry.key(reported)));
        // Mutable only for the framework-provided source bounds; component and data are explicit.
        return PendingIntent.getActivity(c,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));
    }
    static PendingIntent water(Context c,int id,String card,SizeF reported){return waterForDay(c,id,card,reported,LocalDate.now());}
    static PendingIntent waterForDay(Context c,int id,String card,SizeF reported,LocalDate date){String day=date.toString();Intent intent=new Intent(c,WaterAddActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("card",card).putExtra("day",day).putExtra("measureWidget",true).putExtra("reportedWidth",reported.getWidth()).putExtra("reportedHeight",reported.getHeight()).putExtra("geometryStamp",WidgetGeometry.stamp(c,id)).putExtra("geometryDensity",c.getResources().getDisplayMetrics().density).setData(android.net.Uri.parse("cloudwidget://water/"+id+"/"+day+"/root/"+WidgetGeometry.key(reported)));return PendingIntent.getActivity(c,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));}
    static void updateCard(Context c,String cardId){Store store=new Store(c);for(Class<?> kind:new Class<?>[]{MiniWidget.class,SmallWidget.class,WideWidget.class})for(int id:ids(c,kind))if(store.binding(id).equals(cardId))update(c,id);RefreshReceiver.schedule(c);}
    public static java.util.List<SizeF> sizes(Bundle options,boolean wide,boolean landscape){return sizes(options,wide?1:0,landscape);}
    public static java.util.List<SizeF> sizes(Bundle options,int shape,boolean landscape){boolean wide=shape==1;
        java.util.List<SizeF> sizes=new ArrayList<>();Object value=options.get("appWidgetSizes");if(value instanceof ArrayList<?>)for(Object item:(ArrayList<?>)value)if(item instanceof SizeF){SizeF size=(SizeF)item;if(size.getWidth()>=24&&size.getHeight()>=24&&size.getWidth()<=1000&&size.getHeight()<=1000&&!sizes.contains(size)&&sizes.size()<4)sizes.add(size);}
        if(sizes.isEmpty()){int width=options.getInt(landscape?AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH:AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,shape==2?58:wide?310:155);int height=options.getInt(landscape?AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT:AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,shape==2?58:155);sizes.add(new SizeF(Math.max(24,Math.min(1000,width)),Math.max(24,Math.min(1000,height))));}return sizes;
    }
    public static SizeF currentSize(Bundle options,boolean wide,boolean landscape){return currentSize(options,wide?1:0,landscape);}
    public static SizeF currentSize(Bundle options,int shape,boolean landscape){boolean wide=shape==1;
        java.util.List<SizeF> candidates=sizes(options,shape,landscape);
        float width=options.getInt(landscape?AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH:AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,shape==2?58:wide?310:155);
        float height=options.getInt(landscape?AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT:AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,shape==2?58:155);
        SizeF closest=candidates.get(0);double distance=Double.MAX_VALUE;
        for(SizeF size:candidates){double delta=Math.pow(size.getWidth()-width,2)+Math.pow(size.getHeight()-height,2);if(delta<distance){distance=delta;closest=size;}}
        return closest;
    }
    public static void update(Context c,int id){
        AppWidgetManager manager=AppWidgetManager.getInstance(c);Store store=new Store(c);Card card=store.get(store.binding(id));if(card==null){card=new Card();card.type=3;card.title="云朵小卡";card.body="点击选择或创建卡片";}
        AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);int shape=shape(info);card.shape=shape;java.util.List<SizeF> sizes=sizes(manager.getAppWidgetOptions(id),shape,c.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE);
        RemoteViews output;if(Build.VERSION.SDK_INT>=31&&sizes.size()>1){Map<SizeF,RemoteViews> variants=new LinkedHashMap<>();for(SizeF size:sizes)variants.put(size,build(c,id,card,size,sizes.size()));output=new RemoteViews(variants);}else output=build(c,id,card,sizes.get(0),1);manager.updateAppWidget(id,output);
    }
    private static void hit(Context c,RemoteViews parent,float dp,PendingIntent action,String description){RemoteViews child=new RemoteViews(c.getPackageName(),R.layout.hit);child.setInt(R.id.hit,"setHeight",Math.max(0,Ui.dp(c,dp)));child.setContentDescription(R.id.hit,description);if(action!=null)child.setOnClickPendingIntent(R.id.hit,action);parent.addView(R.id.hits,child);}
    private static RemoteViews build(Context c,int id,Card card,SizeF size,int variants){
        SizeF effective=WidgetGeometry.resolve(c,id,size);float width=effective.getWidth(),height=effective.getHeight();CardLayout model=new CardLayout(card,width,height);float scale=model.scale,offset=model.top/scale;RemoteViews views=new RemoteViews(c.getPackageName(),R.layout.widget);
        float budget=500000f/variants;float factor=Math.min(2f,Math.min((float)Math.sqrt(budget/(width*height)),Math.min(900f/width,560f/height)));
        views.setImageViewBitmap(R.id.card,Renderer.render(c,card,Math.round(width*factor),Math.round(height*factor),false));views.setViewVisibility(R.id.clock_group,card.type==0?View.VISIBLE:View.GONE);views.setTextColor(R.id.clock,card.ink);views.setTextColor(R.id.date,card.ink);float pad=model.pad;
        views.setViewPadding(R.id.clock_group,Ui.dp(c,model.left+pad*scale),Ui.dp(c,model.top+model.clockTop()*scale),Ui.dp(c,model.left+pad*scale),0);
        views.setTextViewTextSize(R.id.clock,android.util.TypedValue.COMPLEX_UNIT_DIP,model.clockSize()*scale);
        views.setTextViewTextSize(R.id.date,android.util.TypedValue.COMPLEX_UNIT_DIP,model.dateSize()*scale);
        // Wrap-content TextViews use the same supported height setter on Android 10 and newer.
        views.setInt(R.id.clock,"setHeight",Ui.dp(c,model.clockLine()*scale));views.setInt(R.id.date,"setHeight",Ui.dp(c,model.dateLine()*scale));
        views.setContentDescription(R.id.card,card.title+"，"+Card.TYPES[card.type]+(card.type==2?"，"+Card.countdown(card.date,LocalDate.now()):card.type==3?"，"+card.body:card.type==5?"，"+card.waterTotal(LocalDate.now())+" / "+card.waterGoalMl+" mL，点击记录饮水":""));views.removeAllViews(R.id.hits);views.setOnClickPendingIntent(R.id.hits,card.type==5&&card.shape!=2?water(c,id,card.id,size):measuredEdit(c,id,card.id,size));
        if(card.type!=4)return views;
        float header=(offset+48)*scale,row=30*scale;hit(c,views,header,null,"编辑组件");for(int n=0;n<3;n++){PendingIntent action=null;String description="编辑组件";if(!card.items[n].trim().isEmpty()){Intent toggle=new Intent(c,RefreshReceiver.class).setAction("cn.xi.cloudwidget.TOGGLE").setData(android.net.Uri.parse("cloudwidget://toggle/"+id+"/"+n)).putExtra("widget",id).putExtra("index",n).putExtra("day",LocalDate.now().toString());action=PendingIntent.getBroadcast(c,id*10+n,toggle,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);description=card.items[n]+(card.done(n,LocalDate.now())?"，已完成，点击撤销":"，未完成，点击打卡");}hit(c,views,row,action,description);}hit(c,views,Math.max(0,height-header-3*row),null,"编辑组件");return views;
    }
}
