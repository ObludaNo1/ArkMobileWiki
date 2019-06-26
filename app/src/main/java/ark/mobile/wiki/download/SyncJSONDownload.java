package ark.mobile.wiki.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import ark.mobile.wiki.util.output.Out;

public class SyncJSONDownload {

    public static final String html = "text";
    public static final String version = "revid";

    public String download(String page, String filter) {

        HttpURLConnection con = null;
        try {
            Out.println(this, "Starting download");
            String pageURL = "https://ark.gamepedia.com/api.php?page=" + page + "&action=parse&format=json" + ((filter != null) ? ("&prop=" + filter) : (""));

            long beginTime = System.currentTimeMillis();

            URL obj = new URL(pageURL);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = con.getResponseCode();
            if (responseCode >= 400)
                throw new RuntimeException("The following response code - " + responseCode + " - was returned after requesting content from page " + pageURL);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();

            long downloadTime = System.currentTimeMillis();
            Out.println(this, "Download time: " + (downloadTime - beginTime) + " ms");
            return response.toString();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null)
                con.disconnect();
        }

        return null;
//        throw new RuntimeException("Couldn't download requested page. Reason unknown!");

    }

}
