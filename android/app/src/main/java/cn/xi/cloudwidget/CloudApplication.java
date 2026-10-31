package cn.xi.cloudwidget;
import android.app.Application;
import java.io.*;
public class CloudApplication extends Application {
    @Override public void onCreate(){super.onCreate();try{Backup.recover(this);getSharedPreferences("backup-health",0).edit().remove("error").apply();}catch(IOException error){getSharedPreferences("backup-health",0).edit().putString("error",error.getMessage()).apply();}
        File[] abandoned=new File(getCacheDir(),"backups").listFiles();new Thread(()->{try{Backup.recoverExport(this);}catch(IOException error){getSharedPreferences("backup-health",0).edit().putString("error",error.getMessage()).apply();}if(abandoned!=null)for(File old:abandoned)Backup.remove(old);},"old-backup-cleanup").start();
    }
}
