package cn.xi.cloudwidget;
import android.content.Context;
import android.graphics.Typeface;
import java.io.*;
import java.util.*;

/** Append-only IDs: never reinterpret the font stored by an older card. */
final class FontCatalog {
    static final String[] FILES={"","wenkai.ttf","sans.otf","","ChillRoundFRegular.ttf","XiaolaiSC-Regular.ttf","LXGWMarkerGothic-Regular.ttf"};
    static final int[] VISIBLE_IDS={0,1,2,4,5,6};
    private static final Map<Integer,Typeface> loaded=new HashMap<>();
    private static final Map<Integer,String> hashes=new HashMap<>();
    static synchronized Typeface load(Context c,int id){
        if(id<=0||id==3||id>=FILES.length)return Typeface.DEFAULT;
        if(!loaded.containsKey(id)){
            try{loaded.put(id,Typeface.createFromAsset(c.getAssets(),"fonts/"+FILES[id]));}
            catch(RuntimeException|OutOfMemoryError error){loaded.put(id,Typeface.DEFAULT);}
        }
        return loaded.get(id);
    }
    static synchronized String fingerprint(Context c,int id)throws IOException{
        if(id<0||id>=FILES.length)throw new IOException("备份使用了本版本没有的字体，请更新应用");
        if(id==0||id==3)return "system-default";
        if(!hashes.containsKey(id))try(InputStream in=c.getAssets().open("fonts/"+FILES[id])){hashes.put(id,Backup.hash(in));}
        return hashes.get(id);
    }
}
