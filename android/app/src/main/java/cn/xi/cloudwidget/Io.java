package cn.xi.cloudwidget;
import java.io.*;
public class Io {
    public static void copy(InputStream in,OutputStream out)throws IOException{byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
    public static byte[] read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();copy(in,out);return out.toByteArray();}
}
