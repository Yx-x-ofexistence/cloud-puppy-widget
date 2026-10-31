package cn.xi.cloudwidget;
import android.content.*;
import android.graphics.*;
import java.io.*;
import org.json.*;

/** Deterministic QA images only. Never packaged in release. */
final class ReviewFixtures {
    static String opaque(Context c)throws Exception{
        Bitmap image=Bitmap.createBitmap(180,40,Bitmap.Config.ARGB_8888);image.eraseColor(0xffefb7c8);ByteArrayOutputStream bytes=new ByteArrayOutputStream();image.compress(Bitmap.CompressFormat.PNG,100,bytes);image.recycle();return new AssetStore(c).importPng(new ByteArrayInputStream(bytes.toByteArray())).ref;
    }
    static Card overlap(Context c,int shape)throws Exception{
        Card card=new Card();card.shape=shape;card.id="qa034-layer-"+shape;card.font=0;card.body="BODY UNDER STICKER";card.ink=0xff111111;card.background=0xfff7f2e9;String ref=opaque(c);
        for(double y:new double[]{.20,.36,.56}){card.addSticker(ref);card.stickers.getJSONObject(card.stickers.length()-1).put("width",.90).put("x",.49).put("y",y);}return card;
    }
}
