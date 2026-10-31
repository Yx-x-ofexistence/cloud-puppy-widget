package cn.xi.cloudwidget;

import android.graphics.RectF;
import org.json.JSONObject;

/** One logical content frame for preview, bitmap, native clocks and hit regions.
 * The background fills the host; content is uniformly fitted, never stretched.
 * Sticker coordinates refer to this frame, not an arbitrary launcher canvas. */
public final class CardLayout {
    public final float width,height,baseWidth,scale,left,top,pad=16;
    public final Card card;
    public CardLayout(Card card,float width,float height){
        this.card=card;this.width=width;this.height=height;baseWidth=card.shape==1?400:200;
        scale=Math.min(width/baseWidth,height/200f);
        left=(width-baseWidth*scale)/2;top=(height-200*scale)/2;
    }
    public float clockTop(){return card.stickers.length()==0?(card.body.isEmpty()?65:38):20;}
    public float clockSize(){return 38+(card.size-18)*.8f;}
    public float dateSize(){return 14+(card.size-18)*.25f;}
    public float clockLine(){return clockSize()*1.16f;}
    public float dateLine(){return dateSize()*1.35f;}
    public float bodyTop(){return clockTop()+clockLine()+dateLine()+10;}
    public RectF sticker(JSONObject s,float aspect){
        float w=(float)Math.max(.08,Math.min(.9,s.optDouble("width",.38)))*baseWidth;
        float h=w/aspect;if(h>180){h=180;w=h*aspect;}
        float cx=(float)Math.max(0,Math.min(1,s.optDouble("x",.5)))*baseWidth;
        float cy=(float)Math.max(0,Math.min(1,s.optDouble("y",.82)))*200;
        float x=Math.max(6,Math.min(baseWidth-w-6,cx-w/2)),y=Math.max(6,Math.min(194-h,cy-h/2));
        return new RectF(left+x*scale,top+y*scale,left+(x+w)*scale,top+(y+h)*scale);
    }
    public float dragX(float pixels){return pixels/(scale*baseWidth);}
    public float dragY(float pixels){return pixels/(scale*200);}
}
