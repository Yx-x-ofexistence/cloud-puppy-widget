package cn.xi.cloudwidget;
import android.app.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;

/** Shared Android vector at mask shapes and real 48dp size; QA only. */
public class IconQAActivity extends Activity {
    Bitmap icon(int side,int mask){Bitmap bitmap=Bitmap.createBitmap(side,side,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(bitmap);Path clip=new Path();if(mask==1)clip.addCircle(side/2f,side/2f,side/2f,Path.Direction.CW);else if(mask==2)clip.addRoundRect(0,0,side,side,side*.24f,side*.24f,Path.Direction.CW);else clip.addRect(0,0,side,side,Path.Direction.CW);canvas.clipPath(clip);Drawable art=getDrawable(R.drawable.app_icon);art.setBounds(0,0,side,side);art.draw(canvas);return bitmap;}
    void png(Bitmap bitmap,String name)throws Exception{File dir=new File(getFilesDir(),"review033");dir.mkdirs();try(OutputStream out=new FileOutputStream(new File(dir,name))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}}
    @Override public void onCreate(Bundle b){super.onCreate(b);LinearLayout body=Ui.column(this);Ui.root(this,body);body.setPadding(Ui.dp(this,24),Ui.dp(this,24),Ui.dp(this,24),0);body.addView(Controls.heading(this,"金毛抱云朵 · 矢量图标"));body.addView(Controls.hint(this,"圆形、圆角方形裁切 · 下排为实际 48dp 尺寸"));try{LinearLayout large=new LinearLayout(this),small=new LinearLayout(this);for(int mask=1;mask<=2;mask++){Bitmap image=icon(Ui.dp(this,150),mask);png(image,mask==1?"icon-circle.png":"icon-rounded-square.png");ImageView view=new ImageView(this);view.setImageBitmap(image);large.addView(view,new LinearLayout.LayoutParams(Ui.dp(this,168),Ui.dp(this,168)));Bitmap tiny=icon(Ui.dp(this,48),mask);png(tiny,mask==1?"icon-48dp-circle.png":"icon-48dp-square.png");ImageView sample=new ImageView(this);sample.setImageBitmap(tiny);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));lp.setMargins(Ui.dp(this,36),Ui.dp(this,20),Ui.dp(this,72),0);small.addView(sample,lp);}body.addView(large);body.addView(small);png(icon(512,0),"icon-vector-preview.png");}catch(Exception error){throw new IllegalStateException(error);}}
}
