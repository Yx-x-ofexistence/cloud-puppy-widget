package cn.xi.cloudwidget;
import android.content.Context;
import android.graphics.*;
import android.view.View;

final class NavIcon extends View {
    final int type;final boolean selected;NavIcon(Context c,int type,boolean selected){super(c);this.type=type;this.selected=selected;}
    @Override protected void onDraw(Canvas canvas){canvas.save();canvas.scale(getWidth()/28f,getHeight()/28f);Paint p=new Paint(3);p.setColor(selected?Controls.ACCENT:Controls.MUTED);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
        if(type==0){Path path=new Path();path.moveTo(3,12);path.lineTo(14,3);path.lineTo(25,12);path.moveTo(6,11);path.lineTo(6,25);path.lineTo(22,25);path.lineTo(22,11);path.moveTo(11,25);path.lineTo(11,17);path.lineTo(17,17);path.lineTo(17,25);canvas.drawPath(path,p);}
        else if(type==1){canvas.drawRoundRect(new RectF(3,3,12,12),2,2,p);canvas.drawRoundRect(new RectF(16,3,25,12),2,2,p);canvas.drawRoundRect(new RectF(3,16,12,25),2,2,p);canvas.drawRoundRect(new RectF(16,16,25,25),2,2,p);}
        else if(type==2){canvas.drawRoundRect(new RectF(3,5,25,24),3,3,p);canvas.drawCircle(10,11,2,p);Path path=new Path();path.moveTo(4,22);path.lineTo(12,15);path.lineTo(16,19);path.lineTo(21,13);path.lineTo(25,18);canvas.drawPath(path,p);}
        else {canvas.drawCircle(14,14,8,p);canvas.drawCircle(14,14,3,p);for(int n=0;n<8;n++){double a=n*Math.PI/4;canvas.drawLine(14+(float)Math.cos(a)*10,14+(float)Math.sin(a)*10,14+(float)Math.cos(a)*12,14+(float)Math.sin(a)*12,p);}}
        canvas.restore();
    }
}
