package cn.xi.cloudwidget;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import org.xmlpull.v1.*;
import java.io.*;

/** Runs against the signed release without depending on debug-only activities. */
public class ReleaseUpgradeChecks extends Instrumentation {
    @Override public void onCreate(Bundle args){start();}
    @Override public void onStart(){Bundle result=new Bundle();StringBuilder report=new StringBuilder();int count=0;try{
        Context target=getTargetContext();XmlPullParser parser=android.util.Xml.newPullParser();
        try(InputStream input=getContext().getAssets().open("v032-snapshot.xml")){
            parser.setInput(input,"UTF-8");int event;
            while((event=parser.next())!=XmlPullParser.END_DOCUMENT)if(event==XmlPullParser.START_TAG&&parser.getName().equals("string")){
                String key=parser.getAttributeValue(null,"name"),expected=parser.nextText();
                if(!expected.equals(target.getSharedPreferences("cards-v1",0).getString(key,null)))throw new AssertionError("Changed previous preference: "+key);
                report.append("PASS signed release preserves ").append(key).append('\n');count++;
            }
        }
        for(Class<?> type:new Class<?>[]{MiniWidget.class,SmallWidget.class,WideWidget.class}){
            boolean installed=false;for(AppWidgetProviderInfo info:AppWidgetManager.getInstance(target).getInstalledProviders())if(info.provider.equals(new ComponentName(target,type)))installed=true;
            if(!installed)throw new AssertionError("Missing release provider: "+type.getSimpleName());
            report.append("PASS signed release registers ").append(type.getSimpleName()).append('\n');count++;
        }
        result.putString("stream",report+"Checks passed this run: "+count+"\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){result.putString("stream",report+"FAIL "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
}
