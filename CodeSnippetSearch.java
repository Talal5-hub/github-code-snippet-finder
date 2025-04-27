import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;

public class CodeSnippetSearch extends Application {

    private static final String GITHUB_TOKEN = "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"; // 🔐 Insert your NEW GitHub token here

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Text title = new Text("GitHub Code Snippet Search");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setFill(Color.DARKSLATEBLUE);

        TextField searchField = new TextField();
        searchField.setPromptText("e.g., binary search tree, quicksort algorithm");
        searchField.setPrefHeight(40);
        searchField.setStyle("-fx-font-size: 14;");

        ComboBox<String> languageBox = new ComboBox<>();
        languageBox.getItems().addAll(
                "All Languages",
                "Java",
                "Python",
                "C++",
                "JavaScript",
                "C#",
                "Go",
                "Ruby",
                "Swift",
                "PHP",
                "Kotlin"
        );
        languageBox.setValue("All Languages");
        languageBox.setStyle("-fx-font-size: 14;");
        languageBox.setPrefHeight(40);

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14;");
        searchButton.setPrefHeight(40);
        searchButton.setDefaultButton(true);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setStyle("-fx-control-inner-background: #f0f4f7; -fx-font-family: 'Consolas'; -fx-font-size: 13;");
        resultArea.setPrefHeight(400);

        VBox layout = new VBox(15, title, searchField, languageBox, searchButton, resultArea);
        layout.setPadding(new Insets(20));
        layout.setBackground(new Background(new BackgroundFill(Color.LAVENDER, CornerRadii.EMPTY, Insets.EMPTY)));

        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            String language = languageBox.getValue();
            if (!query.isEmpty()) {
                resultArea.setText("Searching... Please wait.");
                String results = performSearch(query, language);
                resultArea.setText(results);
            } else {
                resultArea.setText("Please enter a search keyword.");
            }
        });

        Scene scene = new Scene(layout, 650, 580);
        primaryStage.setTitle("Professional GitHub Snippet Finder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String performSearch(String query, String language) {
        try {
            StringBuilder urlBuilder = new StringBuilder("https://api.github.com/search/code?q=");

            // Build full query with phrase search and optional language
            String fullQuery = "\"" + query + "\" in:file"; // "binary search tree" in:file
            if (!language.equals("All Languages")) {
                fullQuery += " language:" + language.toLowerCase();
            }

            // Encode the query properly
            String encodedQuery = URLEncoder.encode(fullQuery, StandardCharsets.UTF_8);
            urlBuilder.append(encodedQuery);

            URL obj = new URL(urlBuilder.toString());
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/vnd.github.v3+json");
            con.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray items = jsonObject.getAsJsonArray("items");

                if (items.size() == 0) {
                    return "❌ No results found for \"" + query + "\".";
                }

                StringBuilder resultText = new StringBuilder();
                resultText.append("✅ Found ").append(items.size()).append(" results:\n\n");

                for (JsonElement item : items) {
                    JsonObject file = item.getAsJsonObject();
                    String name = file.get("name").getAsString();
                    String repo = file.getAsJsonObject("repository").get("full_name").getAsString();
                    String htmlUrl = file.get("html_url").getAsString();

                    resultText.append("🔹 File: ").append(name).append("\n")
                            .append("📁 Repository: ").append(repo).append("\n")
                            .append("🔗 Link: ").append(htmlUrl).append("\n")
                            .append("--------------------------------------------------\n");
                }
                return resultText.toString();
            } else if (responseCode == 401 || responseCode == 403) {
                return "🔒 GitHub Token Error: Unauthorized or API rate limit exceeded.";
            } else {
                return "❌ GitHub API Error: " + responseCode;
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
