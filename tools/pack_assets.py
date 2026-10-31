"""Build an offline PNG-only pack; never executes files in the image directory."""
import argparse, hashlib, json, re, zipfile
from pathlib import Path
from PIL import Image
p=argparse.ArgumentParser(description='把一组 PNG 打包成云朵小卡离线素材包')
p.add_argument('--images',required=True,type=Path);p.add_argument('--out',required=True,type=Path)
p.add_argument('--id',required=True);p.add_argument('--name',required=True);p.add_argument('--author',required=True)
p.add_argument('--version',default='1.0.0');p.add_argument('--category',choices=['dog','accessory','paper','background'],default='accessory')
a=p.parse_args()
for value in [a.id,a.version]:
    if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,79}',value):p.error('ID/版本仅用字母、数字、点、短横线与下划线，最多80字符')
images=sorted(a.images.glob('*.png'))
if not 1<=len(images)<=64:p.error('每包需要 1–64 张 PNG')
assets=[];payload=[];total=0
for image in images:
    data=image.read_bytes();total+=len(data)
    if len(data)>8*1024*1024 or total>48*1024*1024:p.error('图片大小超过 App 导入限制')
    with Image.open(image) as im:
        if im.format!='PNG' or max(im.size)>4096 or im.width*im.height>8_000_000:p.error('PNG 格式/尺寸无效：'+image.name)
        im.verify()
    ident=image.stem if re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]{0,79}',image.stem) else 'image-'+hashlib.sha256(image.name.encode()).hexdigest()[:16]
    file='images/'+ident+'.png';assets.append(dict(id=ident,name=image.stem,category=a.category,file=file));payload.append((file,data))
manifest=dict(schema=1,id=a.id,name=a.name,author=a.author,version=a.version,assets=assets)
a.out.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(a.out,'w',zipfile.ZIP_DEFLATED) as z:
    z.writestr('manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2))
    for file,data in payload:z.writestr(file,data)
if a.out.stat().st_size>24*1024*1024:a.out.unlink();p.error('ZIP 压缩包超过24MiB')
print(f'{a.out}: {len(assets)} images, sha256={hashlib.sha256(a.out.read_bytes()).hexdigest()}')
