package cn.xi.cloudwidget;
import android.content.*;
import android.graphics.*;
import android.text.*;
import org.json.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/** Modified Material Photo Widget v1.48.1 textToBitmap approach, Filipe Belatti,
 * Apache-2.0. v0.2: independent background, content, border and optional stickers. */
public class Renderer {
    public static Typeface font(Context c,int f){return FontCatalog.load(c,f);}
    public static Bitmap photo(Context c,String name){if(!name.matches("[A-Za-z0-9._-]+"))return null;File file=new File(new File(c.getFilesDir(),"photos"),name);if(!file.isFile())return null;BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeFile(file.toString(),o);o.inSampleSize=1;while(o.outWidth/o.inSampleSize>1600||o.outHeight/o.inSampleSize>1600)o.inSampleSize*=2;o.inJustDecodeBounds=false;return BitmapFactory.decodeFile(file.toString(),o);}
    public static float clockTop(Card c){return c.stickers.length()==0?(c.body.isEmpty()?65:38):20;}
    public static float unitScale(float width,float height){return Math.min(height/200f,width/180f);}
    public static float contentOffset(float width,float height){float scale=unitScale(width,height);return Math.max(0,(height/scale-200)/2);}
    public static float clockSize(Card c){return 38+(c.size-18)*.8f;}
    public static float dateSize(Card c){return 14+(c.size-18)*.25f;}
    public static float clockBodyTop(Card c){return clockTop(c)+clockSize(c)*1.16f+dateSize(c)*1.35f+10;}
    public static RectF stickerRect(Context context,Card card,JSONObject sticker,float w,float h){
        Bitmap b=new AssetStore(context).bitmap(sticker.optString("asset"),400);if(b==null)return new RectF();
        float aspect=(float)b.getWidth()/b.getHeight();b.recycle();return new CardLayout(card,w,h).sticker(sticker,aspect);
    }
    // Kept for old debug callers; production always supplies the card shape.
    public static RectF stickerRect(Context context,JSONObject sticker,float w,float h){return stickerRect(context,new Card(),sticker,w,h);}
    static void fit(Canvas canvas,Bitmap image,RectF box,int crop,Paint paint){canvas.save();canvas.clipRect(box);float f=crop==1?Math.min(box.width()/image.getWidth(),box.height()/image.getHeight()):Math.max(box.width()/image.getWidth(),box.height()/image.getHeight());float w=image.getWidth()*f,h=image.getHeight()*f;float top=crop==2?box.top:crop==3?box.bottom-h:box.centerY()-h/2;canvas.drawBitmap(image,null,new RectF(box.centerX()-w/2,top,box.centerX()+w/2,top+h),paint);canvas.restore();}
    public static Bitmap render(Context context,Card c,int width,int height,boolean preview){
        float bitmapFactor=Math.min(1,Math.min(900f/Math.max(1,width),560f/Math.max(1,height)));width=Math.max(1,Math.round(width*bitmapFactor));height=Math.max(1,Math.round(height*bitmapFactor));Bitmap output=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(output);
        CardLayout model=new CardLayout(c,width,height);float scale=model.scale,w=width/scale,h=height/scale,pad=16;
        canvas.save();canvas.scale(scale,scale);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);RectF outer=new RectF(2,2,w-2,h-2);Path path=new Path();path.addRoundRect(outer,c.corner,c.corner,Path.Direction.CW);canvas.clipPath(path);
        int alpha=Math.max(0,Math.min(100,c.opacity))*255/100;
        if(c.bgMode!=1){
            // Compose an opaque backing, image and grid first; opacity belongs to the group.
            int backgroundLayer=canvas.saveLayerAlpha(outer,alpha);
            paint.setColor(c.background|0xff000000);canvas.drawRect(outer,paint);
            Bitmap bg=c.bgMode==2?photo(context,c.backgroundPhoto):c.bgMode==3?new AssetStore(context).bitmap(c.backgroundAsset,900):null;
            if(bg!=null){fit(canvas,bg,outer,0,paint);bg.recycle();}
            if(c.grid){paint.setColor((c.borderInk&0xffffff)|(36<<24));paint.setStrokeWidth(.6f);for(int x=12;x<w;x+=18)canvas.drawLine(x,2,x,h-2,paint);for(int y=12;y<h;y+=18)canvas.drawLine(2,y,w-2,y,paint);}
            canvas.restoreToCount(backgroundLayer);
        }
        paint.setAlpha(255);Typeface face=font(context,c.font);
        canvas.save();if(c.type!=1){canvas.translate(model.left/scale,model.top/scale);w=model.baseWidth;h=200;}
        float tw=w-2*pad;
        if(c.type==0){if(!c.body.isEmpty())text(canvas,c.body,pad,model.bodyTop(),tw,Math.max(20,178-model.bodyTop()),Math.min(c.size,18),face,c.ink,2);}
        else if(c.type==1){Bitmap photo=photo(context,c.photo);float margin=c.padding;float bottom=c.title.isEmpty()?h-margin:h-32;RectF box=new RectF(2+margin,2+margin,w-2-margin,bottom);if(photo!=null){canvas.save();Path photoClip=new Path();photoClip.addRoundRect(box,Math.max(0,c.corner-margin/2),Math.max(0,c.corner-margin/2),Path.Direction.CW);canvas.clipPath(photoClip);fit(canvas,photo,box,c.crop,paint);canvas.restore();photo.recycle();}else if(preview&&c.stickers.length()==0)text(canvas,c.shape==2?"选择素材":"选择一张照片",pad,80,tw,36,18,face,c.ink,1);if(!c.title.isEmpty())text(canvas,c.title,pad,h-29,tw,25,c.size,face,c.ink,1);}
        else if(c.type==2){float top=c.stickers.length()==0?39:20;if(!c.title.isEmpty())text(canvas,c.title,pad,top,tw,36,c.size,face,c.ink,2);text(canvas,Card.countdown(c.date,LocalDate.now()),pad,top+43,tw,52,w>270?34:28,face,c.ink,1);text(canvas,c.date.replace('-','.'),pad,top+97,tw,24,14,face,c.ink,1);}
        else if(c.type==3){boolean title=!c.title.isEmpty();float top=title?21:28;if(title)text(canvas,c.title,pad,top,tw,32,c.size+1,face,c.ink,1);float bodyTop=title?59:28;float available=c.stickers.length()==0?175-bodyTop:127-bodyTop;text(canvas,c.body,pad,bodyTop,tw,available,c.size,face,c.ink,Math.max(1,(int)(available/(c.size*1.2f))));}
        else if(c.type==5){WaterDrawing.draw(canvas,c,w,face,LocalDate.now());}
        else {text(canvas,c.title,pad,15,tw,29,Math.min(c.size,20),face,c.ink,1);for(int n=0;n<3;n++){if(c.items[n].trim().isEmpty())continue;float top=48+n*30;boolean done=c.done(n,LocalDate.now());paint.setColor(done?c.borderInk:c.ink);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f);canvas.drawRoundRect(new RectF(pad,top+6,pad+13,top+19),4,4,paint);paint.setStyle(Paint.Style.FILL);if(done){paint.setStrokeWidth(1.7f);canvas.drawLine(pad+3,top+12,pad+6,top+16,paint);canvas.drawLine(pad+6,top+16,pad+11,top+9,paint);}text(canvas,c.items[n],pad+22,top+1,tw-22,26,Math.min(c.size,17),face,c.ink,1);}}
        canvas.restore();
        // Draw in physical bitmap coordinates, using the same frame as preview / gestures.
        canvas.restore();
        Borders.draw(canvas,c,new RectF(2*scale,2*scale,width-2*scale,height-2*scale),scale);
        AssetStore library=new AssetStore(context);for(int n=0;n<c.stickers.length();n++){
            JSONObject sticker=c.stickers.optJSONObject(n);if(sticker==null)continue;
            Bitmap image=library.bitmap(sticker.optString("asset"),500);if(image!=null){
                RectF rect=model.sticker(sticker,(float)image.getWidth()/image.getHeight());
                canvas.drawBitmap(image,null,rect,paint);image.recycle();
            }
        }
        // The widget's native TextClocks are above the sticker bitmap. Match that z-order.
        // Body text intentionally remains below stickers in both rendering paths.
        if(c.type==0&&preview){
            canvas.save();canvas.translate(model.left,model.top);canvas.scale(scale,scale);
            text(canvas,LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),pad,model.clockTop(),model.baseWidth-2*pad,model.clockLine(),model.clockSize(),Typeface.DEFAULT_BOLD,c.ink,1);
            text(canvas,LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 EEEE",Locale.CHINA)),pad,model.clockTop()+model.clockLine(),model.baseWidth-2*pad,model.dateLine(),model.dateSize(),Typeface.DEFAULT,c.ink,1);
            canvas.restore();
        }
        return output;
    }
    public static void text(Canvas canvas,String value,float x,float y,float width,float height,float size,Typeface face,int color,int lines){TextPaint p=new TextPaint(Paint.ANTI_ALIAS_FLAG);p.setTypeface(face);p.setTextSize(size);p.setColor(color);StaticLayout layout=StaticLayout.Builder.obtain(value,0,value.length(),p,Math.max(1,(int)width)).setIncludePad(false).setMaxLines(lines).setEllipsize(TextUtils.TruncateAt.END).build();canvas.save();canvas.clipRect(x,y,x+width,y+height);canvas.translate(x,y);layout.draw(canvas);canvas.restore();}
}
