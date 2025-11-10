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

        try {
            Gson gson = new Gson();

            while (true) {
                System.out.print("\nВведите поисковый запрос (или 0 для выхода): ");
                String query = scanner.nextLine().trim();

                if (query.equals("0")) {
                    System.out.println("Выход.");
                    break;
                }

                if (query.isEmpty()) {
                    System.out.println("Пустой запрос. Попробуйте снова.");
                    continue;
                }

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
                    continue;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                String json = response.toString();

                WikiResponse wikiResponse = gson.fromJson(json, WikiResponse.class);

                if (wikiResponse.query == null || wikiResponse.query.search == null || wikiResponse.query.search.isEmpty()) {
                    System.out.println("Ничего не найдено.");
                    continue;
                }

                List<SearchResult> searchResults = wikiResponse.query.search;
                int count = Math.min(searchResults.size(), 10);
                String[] titles = new String[count];
                for (int i = 0; i < count; i++) {
                    titles[i] = searchResults.get(i).title;
                }

                boolean stayInThisSearch = true;
                while (stayInThisSearch) {
                    System.out.println("\nРезультаты поиска:\n");
                    for (int i = 0; i < titles.length; i++) {
                        System.out.println((i + 1) + ". " + titles[i]);
                    }

                    System.out.print("\nВведите номер статьи для открытия (0 - новый поиск): ");
                    String choiceStr = scanner.nextLine().trim();
                    int choice;
                    try {
                        choice = Integer.parseInt(choiceStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Некорректный ввод. Попробуйте снова.");
                        continue;
                    }

                    if (choice == 0) {
                        break;
                    }

                    if (choice > 0 && choice <= titles.length) {
                        String article = titles[choice - 1].replace(" ", "_");
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

                        while (true) {
                            System.out.print("\nОстаться в этом поиске (1), сделать новый поиск (2), выйти (0): ");
                            String action = scanner.nextLine().trim();

                            if (action.equals("1")) {
                                break;
                            } else if (action.equals("2")) {
                                stayInThisSearch = false;
                                break;
                            } else if (action.equals("0")) {
                                System.out.println("Выход.");
                                return;
                            } else {
                                System.out.println("Некорректный ввод. Попробуйте снова.");
                            }
                        }

                    } else {
                        System.out.println("Нет такой статьи. Попробуйте снова.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка сети: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}