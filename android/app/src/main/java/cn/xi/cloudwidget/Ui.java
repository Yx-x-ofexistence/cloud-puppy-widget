package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

class Ui {
    static int dp(Context c,float v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable paper(int color,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius( dpStatic(18));if(stroke!=0)d.setStroke(1,stroke);return d;}
    private static int dpStatic(int n){return n*3;}
    static LinearLayout column(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(c,20),dp(c,12),dp(c,20),dp(c,24));return l;}
    static TextView text(Context c,String s,int size){TextView t=new TextView(c);t.setText(s);t.setTextSize(size);t.setTextColor(0xff483e3b);t.setPadding(0,dp(c,6),0,dp(c,6));return t;}
    static Button button(Context c,String s,Runnable action){Button b=new Button(c);b.setText(s);b.setAllCaps(false);b.setTextColor(0xff483e3b);b.setTextSize(15);b.setMinHeight(dp(c,48));b.setPadding(dp(c,12),dp(c,10),dp(c,12),dp(c,10));GradientDrawable face=new GradientDrawable();face.setColor(s.startsWith("保存")?0xfff3d9dc:0xfffffcf7);face.setCornerRadius(dp(c,14));face.setStroke(dp(c,1),0xffe5c8c2);b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x30c7959e),face,null));b.setElevation(0);b.setStateListAnimator(null);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(c,5);lp.bottomMargin=dp(c,5);b.setLayoutParams(lp);b.setOnClickListener(v->action.run());return b;}
    static void gap(Context c,LinearLayout l,int n){View v=new View(c);l.addView(v,new LinearLayout.LayoutParams(1,dp(c,n)));}
    static void root(Activity a,LinearLayout body){ScrollView s=new ScrollView(a);s.setFillViewport(true);s.setBackgroundColor(0xfffff7f0);s.addView(body);s.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});a.setContentView(s);}
}
