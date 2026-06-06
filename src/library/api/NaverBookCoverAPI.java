package library.api;

import org.json.JSONArray;
import org.json.JSONObject;

import library.config.EnvConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NaverBookCoverAPI {

    private static final String CLIENT_ID =  EnvConfig.get("NAVER_CLIENT_ID", "");
    private static final String CLIENT_SECRET = EnvConfig.get("NAVER_CLIENT_SECRET", "");

    public String findCover(String isbn) {

        try {

            isbn = isbn.replace("-", "");

            String query =
                    URLEncoder.encode(isbn, "UTF-8");

            String apiURL =
                    "https://openapi.naver.com/v1/search/book.json?query="
                    + query;

            HttpURLConnection con =
                    (HttpURLConnection)new URL(apiURL)
                            .openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty(
                    "X-Naver-Client-Id",
                    CLIENT_ID);

            con.setRequestProperty(
                    "X-Naver-Client-Secret",
                    CLIENT_SECRET);

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    con.getInputStream()
                            )
                    );

            StringBuilder sb =
                    new StringBuilder();

            String line;

            while((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject json =
                    new JSONObject(sb.toString());

            JSONArray items =
                    json.getJSONArray("items");

            if(items.length() == 0) {
                return null;
            }

            return items
                    .getJSONObject(0)
                    .getString("image");

        } catch(Exception e) {

            e.printStackTrace();

            return null;
        }
    }
}