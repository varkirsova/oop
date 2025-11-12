import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.Desktop;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;

public class WikiSearch {

    // классы для парсинга JSON через Gson
    static class WikiResponse {
        Query query;
    }

    static class Query {
        List<SearchResult> search;
    }

    static class SearchResult {
        String title;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Gson gson = new Gson();

        try {
            runSearch(scanner, gson);
        } catch (IOException e) {
            System.out.println("Ошибка сети: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void runSearch(Scanner scanner, Gson gson) throws IOException {
        while (true) {
            String query = getSearchQuery(scanner);
            if (query == null) break;

            String jsonResponse = performSearchRequest(query);
            if (jsonResponse == null) continue;

            List<SearchResult> searchResults = parseResults(gson, jsonResponse);
            if (searchResults == null || searchResults.isEmpty()) {
                System.out.println("Ничего не найдено.");
                continue;
            }

            String[] titles = Titles(searchResults);
            handleResults(scanner, titles);
        }
    }

    private static String getSearchQuery(Scanner scanner) {
        System.out.print("\nВведите поисковый запрос (или 0 для выхода): ");
        String query = scanner.nextLine().trim();

        if (query.equals("0")) {
            System.out.println("Выход.");
            return null;
        }

        if (query.isEmpty()) {
            System.out.println("Пустой запрос. Попробуйте снова.");
            return "";
        }

        return query;
    }

    private static String performSearchRequest(String query) throws IOException {
        String apiUrl = "https://ru.wikipedia.org/w/api.php?action=query&list=search&srsearch="
                + URLEncoder.encode(query, "UTF-8")
                + "&utf8=&format=json";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "WikiSearchApp/1.0 (https://example.com/)");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Ошибка при подключении к Википедии. Код: " + responseCode);
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    private static List<SearchResult> parseResults(Gson gson, String json) {
        WikiResponse wikiResponse = gson.fromJson(json, WikiResponse.class);

        if (wikiResponse.query == null || wikiResponse.query.search == null) {
            return null;
        }

        return wikiResponse.query.search;
    }

    private static String[] Titles(List<SearchResult> searchResults) {
        int count = Math.min(searchResults.size(), 10);
        String[] titles = new String[count];
        for (int i = 0; i < count; i++) {
            titles[i] = searchResults.get(i).title;
        }
        return titles;
    }

    private static void handleResults(Scanner scanner, String[] titles) {
        boolean stayInThisSearch = true;

        while (stayInThisSearch) {
            displayResults(titles);

            int choice = getArticleChoice(scanner, titles.length);
            if (choice == 0) {
                break;
            }

            if (choice > 0) {
                openArticle(titles[choice - 1]);
                stayInThisSearch = afterArticleAction(scanner);
            }
        }
    }

    private static void displayResults(String[] titles) {
        System.out.println("\nРезультаты поиска:\n");
        for (int i = 0; i < titles.length; i++) {
            System.out.println((i + 1) + ". " + titles[i]);
        }
    }

    private static int getArticleChoice(Scanner scanner, int maxChoice) {
        System.out.print("\nВведите номер статьи для открытия (0 - новый поиск): ");
        String choiceStr = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(choiceStr);
            if (choice >= 0 && choice <= maxChoice) {
                return choice;
            } else {
                System.out.println("Нет такой статьи. Попробуйте снова.");
                return -1;
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод. Попробуйте снова.");
            return -1;
        }
    }

    private static void openArticle(String title) {
        try {
            String article = title.replace(" ", "_");
            String articleUrl = "https://ru.wikipedia.org/wiki/" + URLEncoder.encode(article, "UTF-8");

            System.out.println("Открываю статью: " + articleUrl);

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(articleUrl));
                } catch (URISyntaxException e) {
                    System.out.println("Некорректный URL: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Ошибка при открытии браузера: " + e.getMessage());
                }
            } else {
                System.out.println("Открытие браузера не поддерживается.");
            }
        } catch (IOException e) {
            System.out.println("Ошибка при кодировании URL: " + e.getMessage());
        }
    }

    private static boolean afterArticleAction(Scanner scanner) {
        while (true) {
            System.out.print("\nОстаться в этом поиске (1), сделать новый поиск (2), выйти (0): ");
            String action = scanner.nextLine().trim();

            if (action.equals("1")) {
                return true;
            } else if (action.equals("2")) {
                return false;
            } else if (action.equals("0")) {
                System.out.println("Выход.");
                System.exit(0);
            } else {
                System.out.println("Некорректный ввод. Попробуйте снова.");
            }
        }
    }
}
