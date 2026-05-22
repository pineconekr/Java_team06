package library.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 프로젝트 루트의 .env 파일을 읽어 키-값 설정을 제공한다.
 * .env 는 git에 올라가지 않으므로 각자 로컬에 두고 사용한다.
 */
public class EnvConfig {

    private static final Map<String, String> values = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        Path envPath = Paths.get(".env");
        if (!Files.exists(envPath)) {
            System.err.println("[EnvConfig] .env 파일을 찾을 수 없습니다. .env.example 참고하여 생성하세요.");
            return;
        }
        try {
            for (String line : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                values.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("[EnvConfig] .env 읽기 실패: " + e.getMessage());
        }
    }

    /** 키에 해당하는 값을 반환. 없으면 null. */
    public static String get(String key) {
        return values.get(key);
    }

    /** 키에 해당하는 값을 반환. 없으면 기본값. */
    public static String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }
}
