"""Convert the user's 2026-10-30 SVG to native vectors; keep the source untouched.
Only supported vector primitives, user-space linear gradients and rotations are accepted.
Adaptive foreground uses a documented 0.80 safe-area scale; no raster is embedded.
"""
from pathlib import Path
import hashlib, json, math, re, xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'design/golden-cloud-icon/xu-golden-cloud.svg'
RES=ROOT/'android/app/src/main/res'
OUT=ROOT/'artifacts/v0.3.4'
ANDROID='http://schemas.android.com/apk/res/android'
AAPT='http://schemas.android.com/aapt'
ET.register_namespace('android',ANDROID);ET.register_namespace('aapt',AAPT)
doc=ET.parse(SOURCE).getroot()
assert doc.get('viewBox')=='0 0 512 512'
tag=lambda node:node.tag.split('}')[-1]
def attr(node,key,value):node.set('{'+ANDROID+'}'+key,str(value))
def number(value):return f'{float(value):.6f}'.rstrip('0').rstrip('.') or '0'
def oval(x,y,rx,ry):
    x,y,rx,ry=map(float,(x,y,rx,ry));k=.5522847498307936
    return 'M'+number(x-rx)+','+number(y)+' C'+','.join(number(a) for a in [x-rx,y-ry*k,x-rx*k,y-ry,x,y-ry])+' C'+','.join(number(a) for a in [x+rx*k,y-ry,x+rx,y-ry*k,x+rx,y])+' C'+','.join(number(a) for a in [x+rx,y+ry*k,x+rx*k,y+ry,x,y+ry])+' C'+','.join(number(a) for a in [x-rx*k,y+ry,x-rx,y+ry*k,x-rx,y])+'Z'
def shape(node):
    a=node.attrib;t=tag(node)
    if t=='path':return a['d']
    if t in ['ellipse','circle']:return oval(a['cx'],a['cy'],a.get('rx',a.get('r')),a.get('ry',a.get('r')))
    if t=='rect':
        x,y,w,h=map(float,[a.get('x',0),a.get('y',0),a['width'],a['height']]);rx=min(float(a.get('rx',0)),w/2);ry=min(float(a.get('ry',rx)),h/2)
        if not rx or not ry:return f'M{x},{y}h{w}v{h}h{-w}Z'
        return f'M{x+rx},{y}H{x+w-rx}A{rx},{ry} 0 0 1 {x+w},{y+ry}V{y+h-ry}A{rx},{ry} 0 0 1 {x+w-rx},{y+h}H{x+rx}A{rx},{ry} 0 0 1 {x},{y+h-ry}V{y+ry}A{rx},{ry} 0 0 1 {x+rx},{y}Z'
    raise ValueError('Unsupported SVG shape: '+t)
gradients={n.get('id'):n for n in doc.iter() if tag(n)=='linearGradient'}
def color(value):
    if value=='none':return '#00000000'
    if value=='white':return '#FFFFFF'
    if re.fullmatch(r'#[0-9a-fA-F]{6,8}',value):return value
    raise ValueError('Unsupported SVG color: '+value)
def paint(path,name,value):
    ref=re.fullmatch(r'url\(#([^)]+)\)',value)
    if not ref:attr(path,name,color(value));return
    source=gradients[ref.group(1)];assert source.get('gradientUnits')=='userSpaceOnUse'
    holder=ET.SubElement(path,'{'+AAPT+'}attr',{'name':'android:'+name});g=ET.SubElement(holder,'gradient')
    for key,svg in [('startX','x1'),('startY','y1'),('endX','x2'),('endY','y2')]:attr(g,key,source.get(svg,'0'))
    attr(g,'type','linear')
    for stop in source:
        item=ET.SubElement(g,'item');attr(item,'offset',stop.get('offset','0'));attr(item,'color',color(stop.get('stop-color')))
def vector():
    root=ET.Element('vector')
    for key,value in [('width','108dp'),('height','108dp'),('viewportWidth',512),('viewportHeight',512)]:attr(root,key,value)
    return root
STYLE=['fill','stroke','stroke-width','stroke-linecap','stroke-linejoin','fill-rule']
def convert(node,parent,inherited):
    t=tag(node)
    if t in ['defs','title','desc']:return
    style=dict(inherited)
    for key in STYLE:
        if key in node.attrib:style[key]=node.get(key)
    if t in ['g','svg']:
        assert node.get('opacity','1')=='1','Group opacity needs an isolated layer; unsupported by this converter'
        group=ET.SubElement(parent,'group')
        if node.get('id'):attr(group,'name',node.get('id'))
        if node.get('transform'):
            rotation=re.fullmatch(r'rotate\(\s*([-\d.]+)[ ,]+([-\d.]+)[ ,]+([-\d.]+)\s*\)',node.get('transform'));assert rotation
            for key,value in zip(['rotation','pivotX','pivotY'],rotation.groups()):attr(group,key,value)
        for child in node:convert(child,group,style)
        return
    path=ET.SubElement(parent,'path');attr(path,'pathData',shape(node));paint(path,'fillColor',style.get('fill','none'));paint(path,'strokeColor',style.get('stroke','none'))
    for key,source,default in [('strokeWidth','stroke-width','0'),('strokeLineCap','stroke-linecap','butt'),('strokeLineJoin','stroke-linejoin','miter')]:attr(path,key,style.get(source,default))
    alpha=float(node.get('opacity','1'))
    attr(path,'fillAlpha',number(alpha*float(node.get('fill-opacity','1'))));attr(path,'strokeAlpha',number(alpha*float(node.get('stroke-opacity','1'))))
    if style.get('fill-rule')=='evenodd':attr(path,'fillType','evenOdd')
    # Some framework renderers lose the solid stroke of a complex-color fill.
    # An identical, solid stroke-only path preserves the SVG paint order exactly.
    if style.get('fill','').startswith('url(') and style.get('stroke','none')!='none':
        outline=ET.SubElement(parent,'path')
        for key,value in path.attrib.items():outline.set(key,value)
        attr(outline,'fillColor','#00000000');attr(outline,'fillAlpha','0')
        attr(path,'strokeColor','#00000000');attr(path,'strokeWidth','0')
def save(root,path):ET.indent(root,space='  ');path.parent.mkdir(exist_ok=True,parents=True);ET.ElementTree(root).write(path,encoding='utf-8',xml_declaration=True)

flat=vector();convert(doc,flat,{})
save(flat,RES/'drawable/app_icon.xml');save(flat,RES/'mipmap-anydpi/ic_launcher.xml')
foreground=vector();safe=ET.SubElement(foreground,'group')
for k,v in [('scaleX','.80'),('scaleY','.80'),('pivotX','256'),('pivotY','256')]:attr(safe,k,v)
for layer in doc:
    if tag(layer) in ['defs','title','desc']:continue
    if layer.get('id')=='background-layer':
        for child in layer:
            if tag(child)!='rect':convert(child,safe,{})
    else:convert(layer,safe,{})
save(foreground,RES/'drawable/icon_foreground.xml')
background=vector();rect=ET.Element('path',{'d':'M0 0H512V512H0Z','fill':'url(#background)'})
convert(rect,background,{})
save(background,RES/'drawable/icon_background.xml')
(RES/'mipmap-anydpi-v26/ic_launcher.xml').write_text('<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@drawable/icon_background"/><foreground android:drawable="@drawable/icon_foreground"/></adaptive-icon>\n',encoding='utf-8')
OUT.mkdir(parents=True,exist_ok=True)
(OUT/'icon-conversion.json').write_text(json.dumps({'source':str(SOURCE.relative_to(ROOT)),'sourceSha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),'sourceUnchanged':True,'adaptiveForegroundScale':.80,'flatPaths':len(flat.findall('.//path')),'nativeLinearGradients':len(flat.findall('.//gradient'))},indent=2)+'\n',encoding='utf-8')
print('Converted latest source without changing its shapes, gradients or rotations')
