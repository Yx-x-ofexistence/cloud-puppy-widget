"""Explicitly requested deterministic sheet splitting; originals are never changed."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
import json, zipfile, hashlib
from collections import deque
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'android/app/src/main/assets/library'
OUT.mkdir(parents=True,exist_ok=True)
cream=[('read','读书狗狗','dog'),('heart','抱心狗狗','dog'),('memo','举牌狗狗','dog'),('wave','挥手狗狗','dog'),('sleep','枕头睡狗','dog'),('cheer','加油狗狗','dog'),('books','粉色书堆','accessory'),('paws','粉色爪印','accessory'),('pencil','粉色铅笔','accessory'),('paper','花边便签','paper'),('flowers','花束蝴蝶结','accessory'),('stars','粉色星星','accessory'),('clip','爱心回形针','accessory'),('bow','粉色蝴蝶结','accessory'),('cup','热饮杯','accessory'),('label','花边标签','paper')]
blue=[('read','蓝色读书狗','dog'),('books-dog','靠书狗狗','dog'),('write','写字狗狗','dog'),('memo','蓝色举牌狗','dog'),('backpack','背包狗狗','dog'),('sleep','趴书睡狗','dog'),('book','摊开的书','accessory'),('lamp','阅读台灯','accessory'),('notebook','蓝色笔记本','paper'),('tape','格纹胶带','accessory'),('stars','蓝色星星','accessory'),('paws','蓝色爪印','accessory'),('cup','蓝色茶杯','accessory'),('sprig','蓝色小枝叶','accessory'),('corner','折角纸','paper'),('label','蓝色花边标签','paper')]
regions_c=[(0,0,334,350),(334,0,640,350),(640,0,956,350),(956,0,1254,350),(0,350,350,670),(350,350,650,670),(650,350,1000,670),(1000,350,1254,670),(0,670,315,960),(315,670,640,960),(640,670,965,960),(965,670,1254,960),(0,960,300,1254),(300,960,620,1254),(620,940,950,1254),(950,960,1254,1254)]
regions_b=[(0,0,315,354),(315,0,630,354),(630,0,960,354),(960,0,1254,354),(0,354,315,710),(315,354,640,710),(640,354,965,710),(965,354,1254,710),(0,710,320,995),(320,710,640,995),(640,710,965,995),(965,710,1254,995),(0,995,320,1254),(320,965,640,1254),(640,995,960,1254),(960,995,1254,1254)]
items=[];audit=[]
for palette,source,entries,regions in [('cream','cream-puppy-stickers.png',cream,regions_c),('blue','blue-puppy-reading.png',blue,regions_b)]:
    image=Image.open(ROOT/'design/dog-concepts-v1'/source).convert('RGBA')
    # Assign whole connected silhouettes rather than cutting through a uniform grid.
    # This retains overhanging paws, book corners and branches that cross cell gutters.
    mask=image.getchannel('A').point(lambda x:255 if x>70 else 0).filter(ImageFilter.MaxFilter(7))
    pixels=bytearray(mask.tobytes());ww,hh=image.size;groups=[Image.new('L',image.size,0) for _ in entries]
    centers=[((r[0]+r[2])*ww/2508,(r[1]+r[3])*hh/2508) for r in regions]
    for start in range(len(pixels)):
        if not pixels[start]:continue
        queue=[start];pixels[start]=0;component=[]
        while queue:
            pos=queue.pop();component.append(pos);xx=pos%ww;yy=pos//ww
            for near in ((pos-1 if xx else -1),(pos+1 if xx+1<ww else -1),pos-ww,pos+ww):
                if 0<=near<len(pixels) and pixels[near]:pixels[near]=0;queue.append(near)
        if len(component)<55:continue
        cx=sum(p%ww for p in component)/len(component);cy=sum(p//ww for p in component)/len(component)
        idx=min(range(16),key=lambda k:(cx-centers[k][0])**2+(cy-centers[k][1])**2)
        pp=groups[idx].load()
        for pos in component:pp[pos%ww,pos//ww]=255
    from PIL import ImageChops
    for index,(entry,region) in enumerate(zip(entries,regions)):
        ident,name,category=entry
        box=tuple(round(v*image.width/1254) for v in region)
        full=image.copy();full.putalpha(ImageChops.multiply(full.getchannel('A'),groups[index]));bounds=full.getchannel('A').getbbox()
        tile=full.crop(bounds);alpha=tile.getchannel('A');box=bounds
        # Remove only nearly invisible gutter noise. Preserve real antialiased edges.
        alpha=alpha.point(lambda x:0 if x<12 else x);tile.putalpha(alpha)
        bbox=alpha.point(lambda x:255 if x>70 else 0).getbbox()
        if not bbox:raise ValueError(name)
        solid=alpha.point(lambda x:255 if x>100 else 0)
        edge=sum(solid.getpixel((x,0))>0 or solid.getpixel((x,tile.height-1))>0 for x in range(tile.width))+sum(solid.getpixel((0,y))>0 or solid.getpixel((tile.width-1,y))>0 for y in range(tile.height))
        bbox=(max(0,bbox[0]-3),max(0,bbox[1]-3),min(tile.width,bbox[2]+3),min(tile.height,bbox[3]+3))
        tile=tile.crop(bbox);tile.thumbnail((600,600),Image.Resampling.LANCZOS)
        file=f'{palette}-{ident}.png';tile.save(OUT/file)
        items.append(dict(id=f'{palette}-{ident}',name=name,category=category,file=file))
        audit.append(dict(id=f'{palette}-{ident}',source=source,sourceBox=box,size=tile.size,edgePixels=edge,status='待目视检查' if edge>3 else '边缘通过'))
# Preserve the two previously selected concept dogs as explicit optional independent assets.
sheet=Image.open(ROOT/'android/app/src/main/assets/art/puppies.png').convert('RGBA')
for ident,name,box in [('peek','粉色探头狗',(60,120,951,650)),('sleep','蓝色趴睡狗',(951,140,2172,655))]:
    tile=sheet.crop(box);a=tile.getchannel('A').point(lambda x:0 if x<12 else x);tile.putalpha(a);bbox=a.point(lambda x:255 if x>70 else 0).getbbox();tile=tile.crop(bbox);tile.thumbnail((700,500));file='concept-'+ident+'.png';tile.save(OUT/file);items.append(dict(id='concept-'+ident,name=name,category='dog',file=file))
manifest=dict(schema=1,id='cloud-originals',name='云朵原创贴纸',author='熙 · 内置 ImageGen',version='1.0.0',assets=items)
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
(ROOT/'artifacts/v0.2.0/asset-audit.json').write_text(json.dumps(audit,ensure_ascii=False,indent=2),encoding='utf-8')
# QA board, not an app background. IDs allow each extracted item to be inspected.
board=Image.new('RGB',(8*160,5*180),'#f3f0e9');draw=ImageDraw.Draw(board)
for n,item in enumerate(items):
    im=Image.open(OUT/item['file']);im.thumbnail((140,140));x=(n%8)*160;y=(n//8)*180;board.paste(im,(x+(160-im.width)//2,y+(145-im.height)//2),im);draw.text((x+4,y+148),item['id'],fill='#564d47')
board.save(ROOT/'artifacts/v0.2.0/asset-contact-sheet.png')
print('Prepared',len(items),'assets. Boundary flags:',[(a['id'],a['edgePixels']) for a in audit if a['edgePixels']>3])
