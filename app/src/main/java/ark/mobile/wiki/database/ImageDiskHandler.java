package ark.mobile.wiki.database;

import android.content.Context;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.util.output.Out;

public abstract class ImageDiskHandler {

    public static boolean saveFile(String fileName, byte[] data) {
        return saveFile(MainActivity.getContext(), fileName, data);
    }

    public static boolean saveFile(Context context, String fileName, byte[] data){
        try {
            FileOutputStream fos = context.openFileOutput(fileName,Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            if(data == null || data.length == 0) Out.println("Saving empty data");
            byte[] base64 = Base64.encode(data, Base64.DEFAULT);
            out.write(new String(base64));
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] load(String fileName) {
        return load(MainActivity.getContext(), fileName);
    }

    public static byte[] load(Context context, String fileName){
        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line;
            StringBuilder sb = new StringBuilder("");
            while((line = r.readLine())!= null){
                sb.append(line);
            }
            r.close();

            line = sb.toString();
            byte[] base64 = line.getBytes();
            byte[] data = Base64.decode(base64, Base64.DEFAULT);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
