package cn.xi.cloudwidget;

import android.appwidget.*;
import android.content.*;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.SizeF;
import org.json.*;
import java.util.*;

/** Per-widget measured geometry. Launcher reports are hints; a native root click
 * supplies the actual view bounds. Never stores desktop position or card data. */
final class WidgetGeometry {
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("widget-geometry-v1",Context.MODE_PRIVATE);}
    static String key(SizeF size){return String.format(Locale.ROOT,"%.2fx%.2f",size.getWidth(),size.getHeight());}
    static String stamp(Context c,int id){Bundle b=AppWidgetManager.getInstance(c).getAppWidgetOptions(id);Object raw=b.get("appWidgetSizes");ArrayList<String> sizes=new ArrayList<>();if(raw instanceof ArrayList<?>)for(Object item:(ArrayList<?>)raw)if(item instanceof SizeF)sizes.add(key((SizeF)item));Collections.sort(sizes);return b.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)+":"+b.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)+":"+b.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)+":"+b.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)+":"+sizes;}
    static SizeF measured(Context c,int id,SizeF reported){
        try{JSONObject all=new JSONObject(prefs(c).getString("widget:"+id,"{}"));JSONObject entry=all.optJSONObject(key(reported));if(entry==null||!stamp(c,id).equals(entry.optString("stamp")))return null;float w=(float)entry.getDouble("width"),h=(float)entry.getDouble("height");return valid(w,h)?new SizeF(w,h):null;}catch(JSONException e){return null;}
    }
    static SizeF resolve(Context c,int id,SizeF reported){SizeF value=measured(c,id,reported);return value==null?reported:value;}
    static boolean valid(float w,float h){return Float.isFinite(w)&&Float.isFinite(h)&&w>=24&&h>=24&&w<=1000&&h<=1000;}
    static boolean capture(Context c,Intent intent){
        if(!intent.getBooleanExtra("measureWidget",false))return false;
        int id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);AppWidgetProviderInfo info=AppWidgetManager.getInstance(c).getAppWidgetInfo(id);Rect bounds=intent.getSourceBounds();if(info==null||!info.provider.getPackageName().equals(c.getPackageName())||bounds==null)return false;
        SizeF reported=requested(intent);if(reported==null||!stamp(c,id).equals(intent.getStringExtra("geometryStamp")))return false;
        boolean allowed=false;int shape=WidgetProvider.shape(info);for(boolean landscape:new boolean[]{false,true})for(SizeF candidate:WidgetProvider.sizes(AppWidgetManager.getInstance(c).getAppWidgetOptions(id),shape,landscape))if(key(candidate).equals(key(reported)))allowed=true;if(!allowed)return false;
        float density=intent.getFloatExtra("geometryDensity",0);if(!Float.isFinite(density)||density<.5f||density>8)return false;float width=bounds.width()/density,height=bounds.height()/density;if(!valid(width,height))return false;
        SizeF previous=measured(c,id,reported);if(previous!=null&&Math.abs(previous.getWidth()-width)<.25f&&Math.abs(previous.getHeight()-height)<.25f)return false;
        try{JSONObject all=new JSONObject(prefs(c).getString("widget:"+id,"{}"));String current=stamp(c,id);ArrayList<String> stale=new ArrayList<>();Iterator<String> keys=all.keys();while(keys.hasNext()){String k=keys.next();JSONObject entry=all.optJSONObject(k);if(entry==null||!current.equals(entry.optString("stamp")))stale.add(k);}for(String k:stale)all.remove(k);all.put(key(reported),new JSONObject().put("stamp",stamp(c,id)).put("width",width).put("height",height));return prefs(c).edit().putString("widget:"+id,all.toString()).commit();}catch(JSONException e){return false;}
    }
    static SizeF requested(Intent intent){float w=intent.getFloatExtra("reportedWidth",0),h=intent.getFloatExtra("reportedHeight",0);return valid(w,h)?new SizeF(w,h):null;}
    static void remove(Context c,int id){prefs(c).edit().remove("widget:"+id).apply();}
}
