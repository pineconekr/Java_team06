package library.api;

import library.config.EnvConfig;
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

/**
 * 국립중앙도서관 Open API로 도서 데이터 수집.
 * 의존성: org.json (lib/json-20240303.jar)
 * API 키: .env 의 NL_API_KEY 에서 로드
 */
public class LibraryAPICollector {

    private static final String API_KEY = EnvConfig.get("NL_API_KEY", "9c06cbb36626064469f6a6c10a387d8f1b64d1ee22498feafc2a3063cf6bb3d9");
    private static final String API_URL = "https://www.nl.go.kr/NL/search/openApi/search.do";

    public List<Book> collect(String keyword) {
        List<Book> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return result;
        }
        if (API_KEY.isEmpty()) {
            System.err.println("[LibraryAPICollector] NL_API_KEY 가 설정되지 않았습니다. .env 확인 필요.");
            return result;
        }

        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String urlStr = API_URL + "?key=" + API_KEY + "&kwd=" + encoded + "&apiType=json";

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                System.err.println("[LibraryAPICollector] API 응답 코드: " + conn.getResponseCode());
                return result;
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            conn.disconnect();

            JSONObject root = new JSONObject(sb.toString());
            if (!root.has("result")) {
                return result;
            }

            JSONArray data = root.getJSONArray("result");
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                // 국립중앙도서관 API 실제 필드명: titleInfo / authorInfo / kdcName1s
                Book book = new Book(
                        obj.optString("isbn"),
                        stripHtml(obj.optString("titleInfo")),
                        stripHtml(obj.optString("authorInfo")),
                        obj.optString("kdcName1s")
                );
                book.setStatus(BookStatus.AVAILABLE);
                result.add(book);
            }
        } catch (Exception e) {
            System.err.println("[LibraryAPICollector] 통신/파싱 예외: " + e.getMessage());
        }
        return result;
    }

    /** API 응답 값에 섞인 HTML 태그(<span> 등)를 제거한다. */
    private String stripHtml(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("<[^>]*>", "").trim();
    }
}
