package library.api;

import library.model.Book;
import library.model.BookStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// 국립중앙도서관 Open API로 도서 데이터 수집
// 의존성: org.json (build 설정 시 추가 필요)
public class LibraryAPICollector {

    private static final String API_KEY = "여기에_API_KEY";
    private static final String API_URL = "https://www.nl.go.kr/NL/search/openApi/search.do";

    public List<Book> collect(String keyword) {
        List<Book> result = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String urlStr = API_URL + "?key=" + API_KEY + "&kwd=" + encoded + "&apiType=json";

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != 200) return result;

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            conn.disconnect();

            JSONArray data = new JSONObject(sb.toString()).getJSONArray("result");
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                Book book = new Book(
                        obj.optString("isbn"),
                        obj.optString("title"),
                        obj.optString("author"),
                        obj.optString("subject")
                );
                book.setStatus(BookStatus.AVAILABLE);
                result.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
