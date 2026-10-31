package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.function.*;

/** Purpose-specific controls: text actions, segmented choices, swatches and real previews. */
final class Controls {
    static final int INK=0xff483e3b,MUTED=0xff8b817c,ACCENT=0xff9f6374,PAPER=0xfffffaf5;
    static GradientDrawable surface(Context c,int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(Ui.dp(c,radius));if(stroke!=0)d.setStroke(Ui.dp(c,1),stroke);return d;}
    static TextView action(Context c,String text,Runnable action){TextView v=Ui.text(c,text,14);v.setGravity(Gravity.CENTER);v.setTextColor(ACCENT);v.setPadding(Ui.dp(c,12),Ui.dp(c,10),Ui.dp(c,12),Ui.dp(c,10));v.setMinHeight(Ui.dp(c,44));v.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x229f6374),null,new android.graphics.drawable.ColorDrawable(-1)));v.setOnClickListener(w->action.run());return v;}
    static TextView primary(Context c,String text,Runnable action){TextView v=action(c,text,action);v.setTextColor(-1);v.setTypeface(null,Typeface.BOLD);v.setBackground(surface(c,ACCENT,12,0));return v;}
    static TextView heading(Context c,String text){TextView v=Ui.text(c,text,16);v.setTypeface(null,Typeface.BOLD);return v;}
    static TextView hint(Context c,String text){TextView v=Ui.text(c,text,12);v.setTextColor(MUTED);return v;}
    static void choices(Context c,LinearLayout parent,String[] names,int selected,IntConsumer change){
        HorizontalScrollView scroll=new HorizontalScrollView(c);scroll.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(c);scroll.addView(row);
        for(int i=0;i<names.length;i++){final int n=i;TextView item=action(c,names[i],()->change.accept(n));item.setTextColor(i==selected?ACCENT:MUTED);item.setTypeface(null,i==selected?Typeface.BOLD:Typeface.NORMAL);item.setBackground(surface(c,i==selected?0xfff3e4e7:0xfff4f0ed,9,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,Ui.dp(c,40));lp.setMargins(0,Ui.dp(c,4),Ui.dp(c,8),Ui.dp(c,8));row.addView(item,lp);}parent.addView(scroll);
    }
    static void fonts(Context c,LinearLayout parent,int selected,IntConsumer change){
        parent.addView(heading(c,"字体"));HorizontalScrollView scroll=new HorizontalScrollView(c);scroll.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(c);scroll.addView(row);
        for(int position=0;position<FontCatalog.VISIBLE_IDS.length;position++){final int i=FontCatalog.VISIBLE_IDS[position],n=i;LinearLayout tile=new LinearLayout(c);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setBackground(surface(c,i==(selected==3?0:selected)?0xfff5e7e9:0xfffffcf8,12,i==(selected==3?0:selected)?ACCENT:0xffece5df));TextView sample=Ui.text(c,"今天也要好好生活\n0123456789",16);sample.setTypeface(Renderer.font(c,i));sample.setGravity(Gravity.CENTER);sample.setIncludeFontPadding(false);sample.setPadding(0,0,0,0);tile.addView(sample,new LinearLayout.LayoutParams(-1,Ui.dp(c,62)));TextView label=Ui.text(c,Card.FONTS[i],11);label.setGravity(Gravity.CENTER);label.setPadding(0,0,0,0);tile.addView(label,new LinearLayout.LayoutParams(-1,Ui.dp(c,24)));tile.setContentDescription("字体："+Card.FONTS[i]);tile.setOnClickListener(v->change.accept(n));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(c,180),Ui.dp(c,104));lp.setMargins(0,4,Ui.dp(c,10),8);row.addView(tile,lp);}parent.addView(scroll);scroll.post(()->scroll.scrollTo(Ui.dp(c,visibleFontPosition(selected)*190),0));
    }
    private static int visibleFontPosition(int selected){for(int n=0;n<FontCatalog.VISIBLE_IDS.length;n++)if(FontCatalog.VISIBLE_IDS[n]==selected)return n;return 0;}
    static final int BORDER_BLUE=0xff9fbdd0,BORDER_GREEN=0xffa8c3a5;
    static void palette(Activity c,LinearLayout parent,String title,int selected,IntConsumer change){
        parent.addView(heading(c,title));HorizontalScrollView scroll=new HorizontalScrollView(c);scroll.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(c);scroll.addView(row);int[] colors={0xfffff2e3,0xfff4dfe3,title.equals("边框颜色")?BORDER_BLUE:0xffdce9f3,title.equals("边框颜色")?BORDER_GREEN:0xffdfe9dc,0xffffffff,0xff674e45,0xff465c6d,0xff242424,0xffa54a63};
        Swatch rainbow=new Swatch(c,0,true,false);rainbow.setContentDescription("自选"+title);rainbow.setOnClickListener(v->colorDialog(c,title,selected,change));row.addView(rainbow,new LinearLayout.LayoutParams(Ui.dp(c,48),Ui.dp(c,52)));
        for(int color:colors){Swatch dot=new Swatch(c,color,false,color==selected);dot.setContentDescription(title+String.format(" #%06X",color&0xffffff));dot.setOnClickListener(v->change.accept(color));row.addView(dot,new LinearLayout.LayoutParams(Ui.dp(c,48),Ui.dp(c,52)));}parent.addView(scroll);
    }
    static final class Swatch extends View {
        int color;boolean rainbow,selected;Paint p=new Paint(3);Swatch(Context c,int color,boolean rainbow,boolean selected){super(c);this.color=color;this.rainbow=rainbow;this.selected=selected;}
        @Override protected void onDraw(Canvas canvas){float x=getWidth()/2f,y=getHeight()/2f,r=Ui.dp(getContext(),16);p.setStyle(Paint.Style.FILL);if(rainbow)p.setShader(new SweepGradient(x,y,new int[]{Color.RED,Color.YELLOW,Color.GREEN,Color.CYAN,Color.BLUE,Color.MAGENTA,Color.RED},null));else p.setColor(color);canvas.drawCircle(x,y,r,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Ui.dp(getContext(),1));p.setColor(0xffd9d0c9);canvas.drawCircle(x,y,r,p);if(selected){p.setStrokeWidth(Ui.dp(getContext(),2));p.setColor(ACCENT);canvas.drawCircle(x,y,r+Ui.dp(getContext(),4),p);}if(rainbow){p.setStyle(Paint.Style.FILL);p.setColor(PAPER);canvas.drawCircle(x,y,r*.45f,p);}}
    }
    static AlertDialog colorDialog(Activity c,String title,int initial,IntConsumer change){
        LinearLayout body=Ui.column(c);float[] hsv=new float[3];Color.colorToHSV(initial,hsv);TextView sample=hint(c,"左右调色相，在色板上选择明暗与饱和度");body.addView(sample);ColorField field=new ColorField(c,hsv);body.addView(field,new LinearLayout.LayoutParams(-1,Ui.dp(c,180)));View swatch=new View(c);swatch.setBackgroundColor(initial);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(c,28));lp.setMargins(0,Ui.dp(c,12),0,Ui.dp(c,8));body.addView(swatch,lp);field.change=()->swatch.setBackgroundColor(Color.HSVToColor(hsv));TextView hue=hint(c,"色相");body.addView(hue);SeekBar bar=new SeekBar(c);bar.setMax(359);bar.setProgress((int)hsv[0]);bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int n,boolean user){if(user){hsv[0]=n;field.invalidate();swatch.setBackgroundColor(Color.HSVToColor(hsv));}}});body.addView(bar);
        return new AlertDialog.Builder(c).setTitle(title).setView(body).setNegativeButton("取消",null).setPositiveButton("使用这个颜色",(d,w)->change.accept(Color.HSVToColor(hsv))).show();
    }
    static final class ColorField extends View {
        final float[] hsv;Runnable change;Paint p=new Paint(3);ColorField(Context c,float[] hsv){super(c);this.hsv=hsv;setContentDescription("饱和度与明度色板");}
        @Override protected void onDraw(Canvas canvas){int pure=Color.HSVToColor(new float[]{hsv[0],1,1});p.setShader(new LinearGradient(0,0,getWidth(),0,-1,pure,Shader.TileMode.CLAMP));canvas.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(new LinearGradient(0,0,0,getHeight(),0x00000000,0xff000000,Shader.TileMode.CLAMP));canvas.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(-1);p.setStrokeWidth(Ui.dp(getContext(),2));canvas.drawCircle(hsv[1]*getWidth(),(1-hsv[2])*getHeight(),Ui.dp(getContext(),6),p);p.setStyle(Paint.Style.FILL);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN||e.getActionMasked()==MotionEvent.ACTION_MOVE){hsv[1]=Math.max(0,Math.min(1,e.getX()/getWidth()));hsv[2]=1-Math.max(0,Math.min(1,e.getY()/getHeight()));invalidate();if(change!=null)change.run();return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP){performClick();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
    static void borders(Context c,LinearLayout parent,Card card,IntConsumer change){
        HorizontalScrollView scroll=new HorizontalScrollView(c);scroll.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(c);scroll.addView(row);for(int i=0;i<Borders.NAMES.length;i++){final int n=i;LinearLayout tile=new LinearLayout(c);tile.setGravity(Gravity.CENTER);tile.setOrientation(LinearLayout.VERTICAL);BorderThumb thumb=new BorderThumb(c,card,n);tile.addView(thumb,new LinearLayout.LayoutParams(Ui.dp(c,72),Ui.dp(c,72)));TextView label=Ui.text(c,Borders.NAMES[i],11);label.setGravity(Gravity.CENTER);tile.addView(label);if(card.border==i)tile.setBackground(surface(c,0xfff5e7e9,12,0));tile.setContentDescription("边框："+Borders.NAMES[i]);tile.setOnClickListener(v->change.accept(n));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(c,88),-2);lp.rightMargin=Ui.dp(c,6);row.addView(tile,lp);}parent.addView(scroll);
    }
    static void refreshBorders(View root){if(root instanceof BorderThumb)root.invalidate();else if(root instanceof ViewGroup){ViewGroup group=(ViewGroup)root;for(int i=0;i<group.getChildCount();i++)refreshBorders(group.getChildAt(i));}}
    static double luminance(int color){double[] rgb={Color.red(color)/255d,Color.green(color)/255d,Color.blue(color)/255d};for(int i=0;i<3;i++)rgb[i]=rgb[i]<=.04045?rgb[i]/12.92:Math.pow((rgb[i]+.055)/1.055,2.4);return .2126*rgb[0]+.7152*rgb[1]+.0722*rgb[2];}
    static int borderBackdrop(int ink){int light=0xfffffdf9,dark=0xff456174;double l=luminance(ink),a=luminance(light),b=luminance(dark);double lightContrast=(Math.max(l,a)+.05)/(Math.min(l,a)+.05),darkContrast=(Math.max(l,b)+.05)/(Math.min(l,b)+.05);return lightContrast>=darkContrast?light:dark;}
    static final class BorderThumb extends View {
        final Card source;final int style;final Card card=new Card();BorderThumb(Context c,Card source,int style){super(c);this.source=source;this.style=style;}
        @Override protected void onDraw(Canvas canvas){card.border=style;card.borderWidth=source.borderWidth;card.corner=source.corner;card.borderInk=source.borderInk;float s=getWidth()/84f;Paint p=new Paint(3);p.setColor(card.border==0?0xfffffdf9:borderBackdrop(card.borderInk));canvas.drawRoundRect(new RectF(4,4,getWidth()-4,getHeight()-4),12*s,12*s,p);if(card.border==0){p.setColor(0xffbab0aa);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(s);canvas.drawCircle(getWidth()/2f,getHeight()/2f,12*s,p);canvas.drawLine(getWidth()/2f-9*s,getHeight()/2f-9*s,getWidth()/2f+9*s,getHeight()/2f+9*s,p);}else Borders.draw(canvas,card,new RectF(1,1,getWidth()-1,getHeight()-1),s);}
    }
}
