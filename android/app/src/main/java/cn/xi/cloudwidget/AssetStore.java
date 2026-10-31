package cn.xi.cloudwidget;

import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** Immutable local image packs. No scripts, URLs, fonts or executable payloads. */
public class AssetStore {
    private static final Object IMPORT_LOCK=DataLock.LOCK;
    public static class Asset {
        public String ref,id,name,category,author,version,pack; File file; String builtin;
        InputStream open(Context c)throws IOException{return builtin!=null?c.getAssets().open(builtin):new FileInputStream(file);}
    }
    public static class Pack {
        public JSONObject manifest; public File stage; public String digest; public List<Asset> assets=new ArrayList<>();
        public String title(){return manifest.optString("name");}
        public String details(){return manifest.optString("author")+" · "+manifest.optString("version")+" · "+assets.size()+" 张图片";}
    }
    private final Context c;
    private static final long MAX_FILE=8L*1024*1024,MAX_TOTAL=48L*1024*1024;
    public AssetStore(Context c){this.c=c.getApplicationContext();}
    static String text(JSONObject j,String key,int max)throws IOException{String v=j.optString(key,"");if(v.trim().isEmpty()||v.length()>max)throw new IOException("素材包字段无效："+key);return v;}
    static String id(JSONObject j,String key)throws IOException{String v=text(j,key,80);if(!v.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,79}"))throw new IOException("ID / 版本格式无效");return v;}
    static String safePath(String path)throws IOException{if(path.length()>160||!path.matches("[A-Za-z0-9][A-Za-z0-9._/-]*")||path.contains("//"))throw new IOException("素材包路径无效");for(String part:path.split("/"))if(part.equals("..")||part.equals("."))throw new IOException("素材包不能包含相对越界路径");return path;}
    static byte[] limited(InputStream in,long max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[16384];long count=0;int n;while((n=in.read(b))!=-1){count+=n;if(count>max)throw new IOException("文件超过大小限制");out.write(b,0,n);}return out.toByteArray();}
    static String hash(byte[] data){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(data);StringBuilder s=new StringBuilder();for(byte b:d)s.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return s.toString();}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    static void validatePng(byte[] data)throws IOException{
        byte[] signature={(byte)137,80,78,71,13,10,26,10};if(data.length<32||!Arrays.equals(Arrays.copyOf(data,8),signature))throw new IOException("只接受真正的 PNG 图片");
        BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,opts);
        if(opts.outWidth<1||opts.outHeight<1||opts.outWidth>4096||opts.outHeight>4096||(long)opts.outWidth*opts.outHeight>8_000_000)throw new IOException("PNG 尺寸应在 4096 边长、800 万像素内");
        opts.inJustDecodeBounds=false;opts.inSampleSize=Math.max(1,Math.max(opts.outWidth,opts.outHeight)/256);Bitmap test=BitmapFactory.decodeByteArray(data,0,data.length,opts);if(test==null)throw new IOException("PNG 内容已损坏");test.recycle();
    }
    public List<Asset> all(){synchronized(DataLock.LOCK){return allLocked();}}
    private List<Asset> allLocked(){
        List<Asset> out=new ArrayList<>();
        try(InputStream in=c.getAssets().open("library/manifest.json")){out.addAll(readManifest(new JSONObject(new String(Io.read(in),java.nio.charset.StandardCharsets.UTF_8)),null,true));}catch(Exception e){throw new IllegalStateException("内置素材库无法读取",e);}
        File[] dirs=new File(c.getFilesDir(),"assetpacks").listFiles();if(dirs!=null)for(File dir:dirs){if(Backup.hidden(new File(dir,"manifest.json"))||!dir.isDirectory()||!dir.getName().matches("[a-f0-9]{64}"))continue;try(InputStream in=new FileInputStream(new File(dir,"manifest.json"))){out.addAll(readManifest(new JSONObject(new String(Io.read(in),java.nio.charset.StandardCharsets.UTF_8)),dir,false));}catch(Exception ignored){}}
        File[] singles=new File(c.getFilesDir(),"stickers").listFiles();if(singles!=null)for(File file:singles)if(!Backup.hidden(file)&&file.getName().matches("[a-f0-9]{64}\\.png")){Asset a=new Asset();a.id=file.getName().substring(0,64);a.ref="png:"+a.id;a.name="自选贴纸 "+a.id.substring(0,6);a.category="imported";a.author="本机导入";a.version="1";a.pack="单张 PNG";a.file=file;out.add(a);}
        return out;
    }
    List<Asset> readManifest(JSONObject m,File dir,boolean builtin)throws IOException,JSONException{
        if(m.optInt("schema")!=1)throw new IOException("不支持的素材包格式版本");String packId=id(m,"id"),name=text(m,"name",60),author=text(m,"author",80),version=id(m,"version");
        JSONArray list=m.getJSONArray("assets");if(list.length()<1||list.length()>64)throw new IOException("每包应有 1–64 张图片");List<Asset> out=new ArrayList<>();Set<String> ids=new HashSet<>(),paths=new HashSet<>();
        for(int n=0;n<list.length();n++){JSONObject j=list.getJSONObject(n);Asset a=new Asset();a.id=id(j,"id");a.name=text(j,"name",60);a.category=j.optString("category","accessory");if(!Arrays.asList("dog","accessory","paper","background").contains(a.category))throw new IOException("未知图片分类");String path=safePath(text(j,"file",160));if(!path.endsWith(".png")||!ids.add(a.id)||!paths.add(path))throw new IOException("图片 ID / 路径重复或不是 PNG");a.ref=builtin?"builtin:"+a.id:"pack:"+packId+"@"+version+":"+a.id;a.pack=name;a.author=author;a.version=version;if(builtin)a.builtin="library/"+path;else a.file=new File(dir,path);out.add(a);}
        return out;
    }
    public Asset get(String ref){for(Asset a:all())if(a.ref.equals(ref))return a;return null;}
    public Bitmap bitmap(String ref,int limit){Asset a=get(ref);return a==null?null:bitmap(a,limit);}
    public Bitmap bitmap(Asset a,int limit){try{BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;try(InputStream in=a.open(c)){BitmapFactory.decodeStream(in,null,o);}o.inSampleSize=1;while(o.outWidth/o.inSampleSize>limit*2||o.outHeight/o.inSampleSize>limit*2)o.inSampleSize*=2;o.inJustDecodeBounds=false;try(InputStream in=a.open(c)){return BitmapFactory.decodeStream(in,null,o);}}catch(Exception e){return null;}}
    public Pack prepareZip(InputStream source)throws IOException{
        File root=new File(c.getCacheDir(),"asset-import");if(!root.exists()&&!root.mkdirs())throw new IOException("无法创建临时目录");Pack pack=new Pack();pack.stage=new File(root,UUID.randomUUID().toString());if(!pack.stage.mkdir())throw new IOException("无法创建临时目录");
        try{
            // Buffer only the compressed file to enforce a strict compressed-size cap, too.
            byte[] archive=limited(source,24L*1024*1024);Map<String,byte[]> files=new TreeMap<>();long total=0;int count=0;
            try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(archive))){ZipEntry entry;while((entry=zip.getNextEntry())!=null){if(++count>100)throw new IOException("素材包文件太多");String path=safePath(entry.getName());if(entry.isDirectory())continue;if(!path.equals("manifest.json")&&!path.endsWith(".png"))throw new IOException("素材包只能包含 manifest.json 和 PNG");if(files.containsKey(path))throw new IOException("ZIP 内路径重复");byte[] data=limited(zip,path.equals("manifest.json")?128*1024:MAX_FILE);total+=data.length;if(total>MAX_TOTAL)throw new IOException("解压后超过 48 MiB 限制");files.put(path,data);}}
            if(!files.containsKey("manifest.json"))throw new IOException("缺少 manifest.json");pack.manifest=new JSONObject(new String(files.get("manifest.json"),java.nio.charset.StandardCharsets.UTF_8));pack.assets=readManifest(pack.manifest,pack.stage,false);
            Set<String> expected=new HashSet<>();expected.add("manifest.json");StringBuilder canonical=new StringBuilder(pack.manifest.toString());
            for(Asset a:pack.assets){String path=pack.stage.toPath().relativize(a.file.toPath()).toString().replace('\\','/');expected.add(path);byte[] data=files.get(path);if(data==null)throw new IOException("缺少图片："+path);validatePng(data);canonical.append(a.id).append(hash(data));}
            if(!expected.equals(files.keySet()))throw new IOException("素材包包含未登记的图片");pack.digest=hash(canonical.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for(Map.Entry<String,byte[]> f:files.entrySet()){File target=new File(pack.stage,f.getKey());if(!target.getCanonicalPath().startsWith(pack.stage.getCanonicalPath()+File.separator))throw new IOException("解压路径越界");File parent=target.getParentFile();if(!parent.exists()&&!parent.mkdirs())throw new IOException("无法创建素材目录");try(OutputStream out=new FileOutputStream(target)){out.write(f.getValue());}}
            return pack;
        }catch(IOException e){discard(pack);throw e;}catch(Exception|OutOfMemoryError e){discard(pack);throw new IOException("素材包内容损坏或无法读取",e);}
    }
    public void commit(Pack pack)throws IOException{synchronized(IMPORT_LOCK){commitLocked(pack);}}
    private void commitLocked(Pack pack)throws IOException{
        File root=new File(c.getFilesDir(),"assetpacks");if(!root.exists()&&!root.mkdirs())throw new IOException("无法创建素材库");File[] dirs=root.listFiles();if(dirs!=null)for(File dir:dirs){File mf=new File(dir,"manifest.json");if(!mf.isFile())continue;try(InputStream in=new FileInputStream(mf)){JSONObject existing=new JSONObject(new String(Io.read(in),java.nio.charset.StandardCharsets.UTF_8));if(existing.optString("id").equals(pack.manifest.optString("id"))&&existing.optString("version").equals(pack.manifest.optString("version")))throw new IOException(dir.getName().equals(pack.digest)?"这个素材包已导入，无需重复导入":"同 ID 与版本的包内容不同，请使用新版本号，已有素材不会覆盖");}catch(JSONException ignored){}}
        File target=new File(root,pack.digest);if(target.exists()||!pack.stage.renameTo(target))throw new IOException("素材包保存失败，原有素材保持不变");
    }
    public Asset importPng(InputStream source)throws IOException{synchronized(DataLock.LOCK){return importPngLocked(source);}}
    private Asset importPngLocked(InputStream source)throws IOException{byte[] bytes=limited(source,MAX_FILE);validatePng(bytes);String digest=hash(bytes);File root=new File(c.getFilesDir(),"stickers");if(!root.exists()&&!root.mkdirs())throw new IOException("无法创建贴纸目录");File target=new File(root,digest+".png");if(!target.exists()){File staging=new File(root,digest+".tmp");try(OutputStream out=new FileOutputStream(staging)){out.write(bytes);}if(!staging.renameTo(target))throw new IOException("贴纸保存失败");}return get("png:"+digest);}
    public void discard(Pack pack){if(pack!=null&&pack.stage!=null)removeStage(pack.stage);}
    private void removeStage(File file){if(!file.getAbsolutePath().startsWith(new File(c.getCacheDir(),"asset-import").getAbsolutePath()+File.separator))return;File[] children=file.listFiles();if(children!=null)for(File child:children)removeStage(child);file.delete();}
}
