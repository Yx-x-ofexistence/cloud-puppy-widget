package cn.xi.cloudwidget;

import org.json.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Card {
    public String id=UUID.randomUUID().toString(), title="", body="", date=LocalDate.now().plusDays(30).toString(), photo="",backgroundPhoto="",backgroundAsset="";
    public int type=0, skin=0, font=1, size=18, ink=0xff674e45, background=0xfffff2e3, opacity=100, border=0, padding=0, crop=0;
    public int schema=4,bgMode=0,borderInk=0xffdca7a2,corner=20,shape=0;
    public int waterGoalMl=2000,waterQuickMl=250;
    public JSONObject waterHistory=new JSONObject();
    public float borderWidth=1;
    public boolean grid=false;
    public JSONArray stickers=new JSONArray();
    public static Card mini(){Card card=new Card();card.shape=2;card.type=1;card.bgMode=1;card.corner=0;return card;}
    public static Card water(){Card card=new Card();card.type=5;card.title="今日喝水";return card;}
    public void addSticker(String ref){if(shape==2){try{stickers=new JSONArray().put(new JSONObject().put("asset",ref).put("x",.5).put("y",.5).put("width",.90));return;}catch(JSONException e){throw new IllegalStateException(e);}}if(stickers.length()>=3)throw new IllegalArgumentException("每张卡片暂限 3 个贴纸，可替换或移除已有贴纸");try{stickers.put(new JSONObject().put("asset",ref).put("x",.5).put("y",.82).put("width",.38));}catch(JSONException e){throw new IllegalStateException(e);}}
    public static Card preset(int skin,int type){Card c=new Card();c.type=type;c.skin=skin;c.title=type==1?"":"给今天一点温柔";c.body="慢慢来，也在向前走。";c.background=skin==0?0xfffff2e3:0xffedf3f6;c.ink=skin==0?0xff674e45:0xff465c6d;c.borderInk=skin==0?0xffdca7a2:0xff9ab3c4;c.border=1;c.grid=skin==1;c.addSticker("builtin:concept-"+(skin==0?"peek":"sleep"));try{c.stickers.getJSONObject(0).put("width",skin==0?.53:.70).put("y",.85).put("x",skin==0?.5:.63);}catch(JSONException e){throw new IllegalStateException(e);}return c;}
    public String[] items={"读书十分钟","喝一杯水","好好休息"};
    public String[] itemIds={UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString()};
    public JSONObject history=new JSONObject();
    public static final String[] TYPES={"日期 / 时间","照片 / 插画","倒数日","便签 / 鼓励语","今日事项 / 打卡","今日喝水 / 水杯"};
    public static final String[] FONTS={"系统默认","霞鹜文楷","思源黑体","思源宋体","寒蝉全圆体","小赖字体","霞鹜漫黑"};
    public JSONObject json() {
        JSONObject j=new JSONObject();
        try {
            j.put("schema",4).put("waterGoalMl",waterGoalMl).put("waterQuickMl",waterQuickMl).put("waterHistory",waterHistory).put("borderWidth",borderWidth).put("stickers",stickers).put("bgMode",bgMode).put("backgroundPhoto",backgroundPhoto).put("backgroundAsset",backgroundAsset).put("borderInk",borderInk).put("corner",corner).put("shape",shape).put("grid",grid)
             .put("id",id).put("title",title).put("body",body).put("date",date).put("photo",photo)
             .put("type",type).put("skin",skin).put("font",font).put("size",size).put("ink",ink).put("background",background)
             .put("opacity",opacity).put("border",border).put("padding",padding).put("crop",crop)
             .put("items",new JSONArray(Arrays.asList(items))).put("itemIds",new JSONArray(Arrays.asList(itemIds))).put("history",history);
        } catch(JSONException e) { throw new IllegalStateException(e); }
        return j;
    }
    public static Card from(String raw) {
        Card c=new Card();
        try {
            JSONObject j=new JSONObject(raw); c.id=j.getString("id"); c.title=j.optString("title",c.title); c.body=j.optString("body",c.body); c.date=j.optString("date",c.date); c.photo=j.optString("photo","");
            c.type=j.optInt("type"); c.skin=j.optInt("skin"); c.font=j.optInt("font",1); c.size=j.optInt("size",18); c.ink=j.optInt("ink",c.ink); c.background=j.optInt("background",c.background);
            c.opacity=j.optInt("opacity",100); c.border=j.optInt("border",1); c.padding=j.optInt("padding",12); c.crop=j.optInt("crop");c.borderWidth=(float)Math.max(.5,Math.min(3,j.optDouble("borderWidth",1)));
            JSONArray a=j.optJSONArray("items"), ids=j.optJSONArray("itemIds");
            for(int i=0;i<3;i++){ c.items[i]=a==null?c.items[i]:a.optString(i,""); c.itemIds[i]=ids==null?c.itemIds[i]:ids.optString(i,c.itemIds[i]); }
            c.history=j.optJSONObject("history"); if(c.history==null)c.history=new JSONObject();
            if(j.optInt("schema",1)>=4){c.waterGoalMl=j.optInt("waterGoalMl",2000);if(c.waterGoalMl<100||c.waterGoalMl>10000)c.waterGoalMl=2000;c.waterQuickMl=j.optInt("waterQuickMl",250);if(c.waterQuickMl<1||c.waterQuickMl>3000)c.waterQuickMl=250;c.waterHistory=j.optJSONObject("waterHistory");if(c.waterHistory==null)c.waterHistory=new JSONObject();}
            if(j.optInt("schema",1)<2){
                // In-memory migration only; Store.save persists it after a normal edit.
                // The original ID, photo filename, item IDs and history remain untouched.
                c.addSticker("builtin:concept-"+(c.skin==0?"peek":"sleep"));
                try{c.stickers.getJSONObject(0).put("width",c.skin==0?.53:.70);}catch(JSONException ignored){}
                c.grid=c.skin==1;c.borderInk=c.skin==0?0xffdca7a2:0xff9ab3c4;
            }else{c.stickers=j.optJSONArray("stickers");if(c.stickers==null)c.stickers=new JSONArray();c.bgMode=j.optInt("bgMode");c.backgroundPhoto=j.optString("backgroundPhoto");c.backgroundAsset=j.optString("backgroundAsset");c.borderInk=j.optInt("borderInk",c.borderInk);c.corner=j.optInt("corner",20);c.shape=j.optInt("shape");c.grid=j.optBoolean("grid");}
        } catch(JSONException e) { throw new IllegalArgumentException("组件数据无法读取",e); }
        return c;
    }
    public boolean done(int index, LocalDate day) { JSONObject d=history.optJSONObject(day.toString()); return d!=null && d.optBoolean(itemIds[index]); }
    public void toggle(int index,LocalDate day) {
        if(index<0||index>=3||items[index].trim().isEmpty())return;
        try { JSONObject d=history.optJSONObject(day.toString()); if(d==null)d=new JSONObject(); d.put(itemIds[index],!done(index,day)); history.put(day.toString(),d); }
        catch(JSONException e){throw new IllegalStateException(e);}
    }
    public static String countdown(String date,LocalDate today) {
        long days=ChronoUnit.DAYS.between(today,LocalDate.parse(date));
        return days==0?"就是今天":days>0?"还有 "+days+" 天":"已过 "+(-days)+" 天";
    }
    public int waterTotal(LocalDate day){JSONArray entries=waterHistory.optJSONArray(day.toString());int total=0;if(entries!=null)for(int n=0;n<Math.min(entries.length(),1000);n++){JSONObject entry=entries.optJSONObject(n);int ml=entry==null?0:entry.optInt("ml");if(ml>=1&&ml<=3000)total+=ml;}return total;}
    public boolean hasWater(LocalDate day){JSONArray entries=waterHistory.optJSONArray(day.toString());return entries!=null&&entries.length()>0;}
    public void addWater(int ml,LocalDate day,long at){if(ml<1||ml>3000||at<0||at>253402300799999L)throw new IllegalArgumentException("请输入 1–3000 的整数 mL");try{JSONArray entries=waterHistory.optJSONArray(day.toString());if(entries==null)entries=new JSONArray();if(entries.length()>=1000)throw new IllegalArgumentException("今日记录已达上限");entries.put(new JSONObject().put("ml",ml).put("at",at));waterHistory.put(day.toString(),entries);}catch(JSONException error){throw new IllegalStateException(error);}}
    public boolean undoWater(LocalDate day){JSONArray entries=waterHistory.optJSONArray(day.toString());if(entries==null||entries.length()==0)return false;entries.remove(entries.length()-1);if(entries.length()==0)waterHistory.remove(day.toString());return true;}
}
