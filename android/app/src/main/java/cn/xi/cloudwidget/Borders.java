package cn.xi.cloudwidget;
import android.graphics.*;

public final class Borders {
    // Old values 1 / 2 retain their meaning and saved appearance.
    public static final String[] NAMES={"无边框","极细线","手账缝线","普通线","粗实线","双线","短虚线","长虚线","点线","照片留白"};
    public static void draw(Canvas canvas,Card c,RectF bounds,float scale){
        if(c.border<=0)return;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(c.borderInk);p.setStyle(Paint.Style.STROKE);
        float thickness=(c.border==1?.7f:c.border==4?3.5f:c.border==9?7:1.5f)*c.borderWidth*scale;
        RectF r=new RectF(bounds);float inset=(c.border==9?6:3)*scale+thickness/2;r.inset(inset,inset);
        float radius=Math.max(0,c.corner*scale-inset);p.setStrokeWidth(thickness);
        float[] dash=c.border==2?new float[]{3*scale,3*scale}:c.border==6?new float[]{4*scale,3*scale}:c.border==7?new float[]{11*scale,5*scale}:c.border==8?new float[]{.1f*scale,4*scale}:null;
        if(dash!=null){p.setPathEffect(new DashPathEffect(dash,0));if(c.border==8)p.setStrokeCap(Paint.Cap.ROUND);}
        canvas.drawRoundRect(r,radius,radius,p);
        if(c.border==5||c.border==2){p.setPathEffect(c.border==2?new DashPathEffect(new float[]{3*scale,3*scale},1):null);p.setStrokeWidth(Math.max(.6f*scale,thickness*.55f));r.inset(4*scale,4*scale);canvas.drawRoundRect(r,Math.max(0,radius-4*scale),Math.max(0,radius-4*scale),p);}
        if(c.border==9){p.setPathEffect(null);p.setStrokeWidth(12*scale);canvas.drawLine(r.left+2*scale,r.bottom-2*scale,r.right-2*scale,r.bottom-2*scale,p);}
    }
}
