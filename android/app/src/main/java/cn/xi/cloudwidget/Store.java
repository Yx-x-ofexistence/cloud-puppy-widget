package cn.xi.cloudwidget;

import android.content.*;
import android.appwidget.*;
import java.io.*;
import java.util.*;

public class Store {
    private final SharedPreferences p;
    public Store(Context c){p=c.getSharedPreferences("cards-v1",Context.MODE_PRIVATE);}
    public Card get(String id){String s=p.getString("card:"+id,null); return s==null?null:Card.from(s);}
    public void save(Card c){synchronized(DataLock.LOCK){if(!p.edit().putString("card:"+c.id,c.json().toString()).commit())throw new IllegalStateException("保存失败，请检查存储空间");}}
    public List<Card> all(){List<Card> cards=new ArrayList<>(); for(String key:p.getAll().keySet())if(key.startsWith("card:")){try{cards.add(get(key.substring(5)));}catch(Exception ignored){}} cards.sort(Comparator.comparing(x->x.id)); return cards;}
    public void bind(int widget,String id){p.edit().putString("widget:"+widget,id).commit();}
    public String binding(int widget){return p.getString("widget:"+widget,"");}
    public void unbind(int widget){p.edit().remove("widget:"+widget).commit();}
    public void stagePin(Card card){p.edit().putString("pending-pin",card.id).putInt("pending-pin-shape",card.shape).putLong("pending-pin-at",System.currentTimeMillis()).commit();}
    public void clearPin(){p.edit().remove("pending-pin").remove("pending-pin-shape").remove("pending-pin-at").commit();}
    public String takePin(Context context,int widget){AppWidgetProviderInfo info=AppWidgetManager.getInstance(context).getAppWidgetInfo(widget);if(info==null)return "";int shape=WidgetProvider.shape(info);long age=System.currentTimeMillis()-p.getLong("pending-pin-at",0);if(age<0||age>120000||shape!=p.getInt("pending-pin-shape",-1))return "";String id=p.getString("pending-pin","");clearPin();return id;}
    public void delete(String id){synchronized(DataLock.LOCK){SharedPreferences.Editor e=p.edit().remove("card:"+id);for(String key:p.getAll().keySet())if(key.startsWith("widget:")&&id.equals(p.getString(key,"")))e.remove(key);e.commit();}}
    public void cleanPhotos(Context c){synchronized(DataLock.LOCK){Set<String> used=new HashSet<>();for(Card a:all()){used.add(a.photo);used.add(a.backgroundPhoto);}File dir=new File(c.getFilesDir(),"photos");File[] files=dir.listFiles();if(files!=null)for(File f:files)if(!used.contains(f.getName()))f.delete();}}
    public boolean waterChange(String id,String expectedDay,int ml,boolean undo){long at=System.currentTimeMillis();return waterChange(id,expectedDay,ml,undo,java.time.Instant.ofEpochMilli(at).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),at);}
    boolean waterChange(String id,String expectedDay,int ml,boolean undo,java.time.LocalDate today,long at){synchronized(DataLock.LOCK){if(!today.toString().equals(expectedDay))return false;Card card=get(id);if(card==null||card.type!=5||card.shape==2)return false;if(undo){if(!card.undoWater(today))return false;}else card.addWater(ml,today,at);save(card);return true;}}
}
