package org.example.api;

import org.example.dao.BookDAO;
import org.example.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

public class LibraryAPICollector {

    private static final String API_KEY =
            "여기에_API_KEY";

    private static final String API_URL =
            "https://www.nl.go.kr/NL/search/openApi/search.do";

    public void collectBooks(String keyword) {

        try {

            String encodedKeyword =
                    URLEncoder.encode(
                            keyword,
                            StandardCharsets.UTF_8
                    );

            String apiURL =
                    API_URL
                    + "?key=" + API_KEY
                    + "&kwd=" + encodedKeyword
                    + "&apiType=json";

            URL url = new URL(apiURL);

            HttpURLConnection connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setRequestMethod("GET");

            int responseCode =
                    connection.getResponseCode();

            if (responseCode != 200) {

                System.out.println(
                        "API 호출 실패 : "
                                + responseCode
                );

                return;
            }

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()
                            )
                    );

            StringBuilder response =
                    new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {

                response.append(line);
            }

            br.close();

            connection.disconnect();

            JSONObject jsonObject =
                    new JSONObject(response.toString());

            System.out.println(jsonObject);

            JSONArray dataArray =
                    jsonObject.getJSONArray("result");

            BookDAO bookDAO =
                    new BookDAO();

            for (int i = 0; i < dataArray.length(); i++) {

                JSONObject obj =
                        dataArray.getJSONObject(i);

                Book book = new Book();

                book.setIsbn(
                        obj.optString("isbn")
                );

                book.setTitle(
                        obj.optString("title")
                );

                book.setAuthor(
                        obj.optString("author")
                );

                book.setPublisher(
                        obj.optString("publisher")
                );

                book.setPublishYear(
                        obj.optString("pubyear")
                );

                book.setDescription(
                        obj.optString("description")
                );

                book.setStatus("AVAILABLE");

                bookDAO.insertBook(book);
            }

            System.out.println(
                    "데이터 저장 완료"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}