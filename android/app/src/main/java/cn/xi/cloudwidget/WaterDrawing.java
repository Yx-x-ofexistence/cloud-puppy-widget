package cn.xi.cloudwidget;
import android.graphics.*;
import java.time.LocalDate;

/** Logical-frame vector cup; the same drawing is used in editor and RemoteViews. */
final class WaterDrawing {
    private static float fitted(String text,float width,float desired,Typeface face){Paint measure=new Paint(Paint.ANTI_ALIAS_FLAG);measure.setTypeface(face);measure.setTextSize(desired);return Math.min(desired,desired*width/Math.max(width,measure.measureText(text)));}
    static float fraction(Card card,LocalDate day){return Math.max(0,Math.min(1,card.waterTotal(day)/(float)Math.max(100,card.waterGoalMl)));}
    static void draw(Canvas canvas,Card card,float width,Typeface face,LocalDate day){
        boolean wide=card.shape==1;float cx=wide?100:width/2,top=wide?42:45,bottom=wide?157:125,cupWidth=wide?78:62;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);float left=cx-cupWidth/2,right=cx+cupWidth/2;
        Path cup=new Path();cup.moveTo(left,top);cup.cubicTo(left-2,top-4,right+2,top-4,right,top);cup.lineTo(right-7,bottom-8);cup.quadTo(right-8,bottom,left+10,bottom);cup.quadTo(left+7,bottom,left+6,bottom-8);cup.close();
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.3f);p.setColor(0xff7195a5);Path handle=new Path();handle.moveTo(right-1,top+14);handle.cubicTo(right+26,top+6,right+28,bottom-12,right-5,bottom-18);canvas.drawPath(handle,p);
        p.setStyle(Paint.Style.FILL);p.setColor(0x22b7d6e4);canvas.drawPath(cup,p);float fill=fraction(card,day),level=bottom-5-(bottom-top-9)*fill;
        if(fill>0){canvas.save();canvas.clipPath(cup);p.setShader(new LinearGradient(0,top,0,bottom,0xffb4dce9,0xff78aec6,Shader.TileMode.CLAMP));Path water=new Path();water.moveTo(left-3,level);water.cubicTo(cx-8,level-3,cx+8,level+3,right+3,level);water.lineTo(right+3,bottom+1);water.lineTo(left-3,bottom+1);water.close();canvas.drawPath(water,p);p.setShader(null);canvas.restore();}
        p.setStyle(Paint.Style.STROKE);p.setColor(0xff7195a5);p.setStrokeWidth(2.3f);canvas.drawPath(cup,p);p.setColor(0xcfffffff);p.setStrokeWidth(3);canvas.drawLine(left+10,top+13,left+14,bottom-17,p);
        p.setStrokeWidth(1);p.setColor(0x5581a5b5);for(int n=1;n<4;n++){float y=bottom-(bottom-top)*n/4;canvas.drawLine(right-15,y,right-7,y,p);}p.setStyle(Paint.Style.FILL);
        if(!card.title.isEmpty())Renderer.text(canvas,card.title,16,12,width-32,30,Math.min(card.size,20),face,card.ink,1);
        String total=card.waterTotal(day)+" / "+card.waterGoalMl+" mL",progress="今日 "+Math.round(fill*100)+"%";
        if(wide){Renderer.text(canvas,total,174,66,width-190,38,fitted(total,width-190,Math.min(30,card.size+7),face),face,card.ink,1);Renderer.text(canvas,progress+(fill>=1?" · 已完成":""),174,107,width-190,27,Math.max(12,Math.min(22,card.size-1)),face,card.ink,1);Renderer.text(canvas,"点一下，记录一杯",174,144,width-190,23,Math.max(11,Math.min(14,card.size-6)),face,card.ink,1);}
        else {Renderer.text(canvas,total,16,134,width-32,28,fitted(total,width-32,Math.min(22,card.size+1),face),face,card.ink,1);Renderer.text(canvas,progress+(fill>=1?" · 已完成":""),16,164,width-32,23,Math.max(11,Math.min(16,card.size-5)),face,card.ink,1);}
        if(fill>=1){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xff5e937c);Path tick=new Path();tick.moveTo(cx-8,top+(bottom-top)/2);tick.lineTo(cx-2,top+(bottom-top)/2+6);tick.lineTo(cx+10,top+(bottom-top)/2-8);canvas.drawPath(tick,p);}
    }
}
