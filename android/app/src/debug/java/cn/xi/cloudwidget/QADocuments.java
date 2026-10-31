package cn.xi.cloudwidget;
import android.database.*;
import android.os.*;
import android.provider.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Debug-only SAF document backend, with controlled provider disconnection. */
public class QADocuments extends DocumentsProvider {
    static final AtomicInteger deletions=new AtomicInteger();
    File file(String id){if(!id.matches("qa034-[A-Za-z0-9-]+"))throw new IllegalArgumentException("QA document ID");File root=new File(getContext().getFilesDir(),"qa034-documents");root.mkdirs();return new File(root,id+".zip");}
    @Override public boolean onCreate(){return true;}
    @Override public String getDocumentType(String id){return "application/zip";}
    @Override public Cursor queryRoots(String[] projection){return new MatrixCursor(projection==null?new String[]{DocumentsContract.Root.COLUMN_ROOT_ID}:projection);}
    @Override public Cursor queryDocument(String id,String[] projection){String[] columns=projection==null?new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS}:projection;MatrixCursor cursor=new MatrixCursor(columns);MatrixCursor.RowBuilder row=cursor.newRow();for(String col:columns)row.add(col,col.equals(DocumentsContract.Document.COLUMN_DOCUMENT_ID)?id:col.equals(DocumentsContract.Document.COLUMN_DISPLAY_NAME)?id+".zip":col.equals(DocumentsContract.Document.COLUMN_MIME_TYPE)?"application/zip":col.equals(DocumentsContract.Document.COLUMN_FLAGS)?DocumentsContract.Document.FLAG_SUPPORTS_WRITE|DocumentsContract.Document.FLAG_SUPPORTS_DELETE:null);return cursor;}
    @Override public Cursor queryChildDocuments(String parent,String[] columns,String sort){return new MatrixCursor(columns==null?new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID}:columns);}
    @Override public ParcelFileDescriptor openDocument(String id,String mode,CancellationSignal signal)throws FileNotFoundException{File file=file(id);if(mode.contains("w")&&id.contains("fail"))throw new FileNotFoundException("injected provider write failure");if(mode.contains("w")&&id.contains("short")){try{ParcelFileDescriptor[] pipe=ParcelFileDescriptor.createReliablePipe();new Thread(()->{try(InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]);OutputStream out=new FileOutputStream(file)){byte[] bytes=new byte[1024];int total=0,n;while((n=in.read(bytes))!=-1){out.write(bytes,0,n);total+=n;if(total>=4096){pipe[0].closeWithError("injected provider disconnection");break;}}}catch(IOException ignored){}},"qa-document-disconnect").start();return pipe[1];}catch(IOException error){throw new FileNotFoundException(error.getMessage());}}
        return ParcelFileDescriptor.open(file,mode.contains("w")?ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_CREATE|ParcelFileDescriptor.MODE_TRUNCATE:ParcelFileDescriptor.MODE_READ_ONLY);}
    @Override public void deleteDocument(String id){file(id).delete();deletions.incrementAndGet();}
}
