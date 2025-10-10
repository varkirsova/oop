import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.net.URI;
import java.awt.Desktop;
import java.util.Scanner;

public class WikiSearch {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
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

                // HTTP-запрос
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

                // Извлекаем названия статей
                String[] parts = json.split("\"title\":\"");
                if (parts.length <= 1) {
                    System.out.println("Ничего не найдено.");
                    continue;
                }

                // Ограничиваем список до 10 статей
                int count = Math.min(parts.length - 1, 10);
                String[] titles = new String[count];
                for (int i = 1; i <= count; i++) {
                    titles[i - 1] = parts[i].split("\"")[0];
                }

                // Цикл выбора статей
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
                        break; // возвращаемся к новому запросу
                    }

                    if (choice > 0 && choice <= titles.length) {
                        String article = titles[choice - 1].replace(" ", "_");
                        String articleUrl = "https://ru.wikipedia.org/wiki/" + URLEncoder.encode(article, "UTF-8");

                        System.out.println("Открываю статью: " + articleUrl);
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(new URI(articleUrl));
                        } else {
                            System.out.println("Открытие браузера не поддерживается.");
                        }

                        // После открытия спрашиваем действие
                        while (true) {
                            System.out.print("\nОстаться в этом поиске (1), сделать новый поиск (2), выйти (0): ");
                            String action = scanner.nextLine().trim();

                            if (action.equals("1")) {
                                break; // вернёмся к списку статей
                            } else if (action.equals("2")) {
                                stayInThisSearch = false; // выйти на новый поиск
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
        } catch (UnsupportedEncodingException e) {
            System.out.println("Ошибка кодировки: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка сети: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неожиданная ошибка: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}