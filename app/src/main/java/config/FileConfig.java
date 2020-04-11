package config;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileConfig {

    private static FileConfig instance;
    private Context context;

    private FileConfig(Context context) {
        this.context = context;
    }

    public static synchronized FileConfig getInstance(Context context) {
        if (instance == null)
            instance = new FileConfig(context);
        return instance;
    }

    public void saveFile(File src, File dst) throws IOException {
        FileUtils.copyFileToDirectory(src, dst);
    }

   /* public void deleteFile(File file) throws IOException {
        FileUtils.forceDelete(file);
        File outFIle = new File(new File(Config.WhatsAppSaveStatus), file.getName());
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFIle));
        context.sendBroadcast(intent);
        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
    }*/

}
