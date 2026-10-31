package cn.xi.cloudwidget;
import android.content.*;
import android.graphics.*;
import android.view.*;
import org.json.*;

public class CardPreview extends View {
    public Card card;public boolean wide=false,editing=false;public Runnable onChange;public int selected=-1;public float targetWidth=0,targetHeight=0;
    private Bitmap image;private RectF bounds=new RectF();private Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG);
    private float lastX,lastY,lastDistance;
    public CardPreview(Context c,Card card){super(c);this.card=card;wide=card.shape==1;setContentDescription("组件预览，可拖动贴纸，双指缩放");}
    public void refresh(){if(image!=null)image.recycle();image=null;invalidate();}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);refresh();}
    @Override protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w);int height=MeasureSpec.getMode(h)==MeasureSpec.EXACTLY?MeasureSpec.getSize(h):Ui.dp(getContext(),190);setMeasuredDimension(width,height);}
    @Override protected void onDraw(Canvas canvas){float aspect=targetWidth>0&&targetHeight>0?targetWidth/targetHeight:wide?2:1;float w=Math.min(getWidth(),getHeight()*aspect),h=w/aspect;bounds.set((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);if(w<1||h<1)return;if(card.shape==2&&card.bgMode==1){canvas.save();canvas.clipRect(bounds);Paint checker=new Paint();int step=Ui.dp(getContext(),7);for(float yy=bounds.top;yy<bounds.bottom;yy+=step)for(float xx=bounds.left;xx<bounds.right;xx+=step){checker.setColor((((int)((xx-bounds.left)/step)+(int)((yy-bounds.top)/step))&1)==0?0xfff5f3f0:0xffdce3e8);canvas.drawRect(xx,yy,xx+step,yy+step,checker);}canvas.restore();}if(image==null)image=Renderer.render(getContext(),card,(int)w,(int)h,true);canvas.drawBitmap(image,null,bounds,paint);if(editing&&selected>=0&&selected<card.stickers.length()){RectF r=Renderer.stickerRect(getContext(),card,card.stickers.optJSONObject(selected),w,h);r.offset(bounds.left,bounds.top);Paint line=new Paint(Paint.ANTI_ALIAS_FLAG);line.setColor(0xffb77b88);line.setStyle(Paint.Style.STROKE);line.setStrokeWidth(Ui.dp(getContext(),1));line.setPathEffect(new DashPathEffect(new float[]{6,4},0));canvas.drawRect(r,line);}}
    @Override public boolean onTouchEvent(android.view.MotionEvent event){
        if(!editing)return super.onTouchEvent(event);
        float x=event.getX()-bounds.left,y=event.getY()-bounds.top;
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN){selected=-1;for(int n=card.stickers.length()-1;n>=0;n--)if(Renderer.stickerRect(getContext(),card,card.stickers.optJSONObject(n),bounds.width(),bounds.height()).contains(x,y)){selected=n;break;}if(selected<0)return super.onTouchEvent(event);lastX=x;lastY=y;getParent().requestDisallowInterceptTouchEvent(true);invalidate();return true;}
        if(selected<0)return false;
        if(event.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN){lastDistance=distance(event);return true;}
        if(event.getActionMasked()==MotionEvent.ACTION_POINTER_UP){lastDistance=0;int remaining=event.getActionIndex()==0?1:0;lastX=event.getX(remaining)-bounds.left;lastY=event.getY(remaining)-bounds.top;return true;}
        if(event.getActionMasked()==MotionEvent.ACTION_MOVE){JSONObject s=card.stickers.optJSONObject(selected);try{if(event.getPointerCount()>1){float d=distance(event);if(lastDistance>0)s.put("width",Math.max(.08,Math.min(.9,s.optDouble("width",.38)*d/lastDistance)));lastDistance=d;}else{s.put("x",Math.max(0,Math.min(1,s.optDouble("x",.5)+new CardLayout(card,bounds.width(),bounds.height()).dragX(x-lastX))));s.put("y",Math.max(0,Math.min(1,s.optDouble("y",.8)+new CardLayout(card,bounds.width(),bounds.height()).dragY(y-lastY))));lastX=x;lastY=y;}refresh();if(onChange!=null)onChange.run();}catch(JSONException e){throw new IllegalStateException(e);}return true;}
        if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL){lastDistance=0;getParent().requestDisallowInterceptTouchEvent(false);performClick();return true;}return true;
    }
    private float distance(MotionEvent e){return (float)Math.hypot(e.getX(0)-e.getX(1),e.getY(0)-e.getY(1));}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override protected void onDetachedFromWindow(){if(image!=null){image.recycle();image=null;}super.onDetachedFromWindow();}
}
