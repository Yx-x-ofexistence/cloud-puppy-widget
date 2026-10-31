package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.os.*;
import org.json.*;
import java.io.*;
import java.security.*;
import java.util.*;

/** Independent of target implementation, usable before and after an in-place upgrade. */
public class SnapshotChecks extends Instrumentation {
    Bundle args;StringBuilder report=new StringBuilder();int count;
    @Override public void onCreate(Bundle args){this.args=args;start();}
    String hash(File file)throws Exception{MessageDigest md=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(file)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)md.update(b,0,n);}StringBuilder out=new StringBuilder();for(byte b:md.digest())out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}
    void files(Context c,File file,JSONObject result)throws Exception{if(file.isDirectory()){File[] list=file.listFiles();if(list!=null)for(File child:list)files(c,child,result);}else if(file.isFile())result.put(c.getFilesDir().toPath().relativize(file.toPath()).toString().replace('\\','/'),hash(file));}
    JSONObject snapshot(Context c)throws Exception{JSONObject files=new JSONObject();for(String root:new String[]{"photos","stickers","assetpacks"})files(c,new File(c.getFilesDir(),root),files);return new JSONObject().put("cards",new JSONObject(c.getSharedPreferences("cards-v1",0).getAll())).put("geometry",new JSONObject(c.getSharedPreferences("widget-geometry-v1",0).getAll())).put("files",files).put("version",c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionName);}
    void check(boolean valid,String text){if(!valid)throw new AssertionError(text);count++;report.append("PASS ").append(text).append('\n');}
    @Override public void onStart(){Bundle result=new Bundle();try{Context c=getTargetContext();if("seed".equals(args.getString("phase"))){if(!c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionName.startsWith("0.3.3"))throw new AssertionError("Pre-upgrade fixtures require original v033");SharedPreferences pref=c.getSharedPreferences("cards-v1",0);String template=null;for(String key:pref.getAll().keySet())if(key.startsWith("card:")){template=pref.getString(key,null);break;}if(template==null)throw new AssertionError("Missing existing card template");for(int n=0;n<3;n++){JSONObject card=new JSONObject(template);card.put("schema",3).put("id","qa034-preupgrade-"+n).put("title",n==2?"Legacy serif":"Legacy border "+n).put("type",3).put("font",n==2?3:0).put("shape",0).put("border",n==2?0:1).put("borderWidth",.5).put("borderInk",n==0?0xffdce9f3:0xffdfe9dc);pref.edit().putString("card:"+card.getString("id"),card.toString()).commit();}}JSONObject actual=snapshot(c);if("save".equals(args.getString("phase"))||"seed".equals(args.getString("phase"))){File file=new File(c.getExternalFilesDir(null),"qa034-before.json");try(OutputStream out=new FileOutputStream(file)){out.write(actual.toString(2).getBytes("UTF-8"));}check(file.isFile(),"saved original preferences, geometry and file fingerprints: "+file);}
        else {JSONObject before;try(InputStream in=getContext().getAssets().open("v033-snapshot.json")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);before=new JSONObject(out.toString("UTF-8"));}for(String section:new String[]{"cards","geometry","files"}){JSONObject expected=before.getJSONObject(section),current=actual.getJSONObject(section);Iterator<String> keys=expected.keys();while(keys.hasNext()){String key=keys.next();if(section.equals("cards")&&key.equals("backup-transaction"))continue;check(current.has(key)&&expected.get(key).toString().equals(current.get(key).toString()),"v033 unchanged "+section+"/"+key);}}}
        result.putString("stream",report+"Checks passed this run: "+count+"\n");finish(Activity.RESULT_OK,result);
    }catch(Throwable error){result.putString("stream",report+"FAIL "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}}
}
