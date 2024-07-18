package pl.fvst.itemshop.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import pl.fvst.itemshop.config.Config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getLogger;

public class OrderTask extends BukkitRunnable {

    private final Config config;
    private final ConcurrentHashMap<String, String> products;

    public OrderTask(Config config, ConcurrentHashMap<String, String> products) {
        this.config = config;
        this.products = products;
    }

    @Override
    public void run() {
        HttpClient httpClient = HttpClients.createDefault();
        // Assuming {{baseurl}} is "https://api.fvst.pl" - replace it with the actual base URL if different
        String baseUrl = "https://api.fvst.pl";
        String shopId = config.getShopIdentifier();
        String serverId = config.getServerIdentifier();
        boolean delivered = false;

        String url = String.format("%s/shops/%s/orders?serverId=%s&delivered=%s", baseUrl, shopId, serverId, delivered);
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("x-api-key", config.getApiKey());

        try {
            String jsonResponse = httpClient.execute(httpGet, response -> {
                int status = response.getStatusLine().getStatusCode();
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                getLogger().info(url);
                getLogger().info("Response: " + responseString);

                if (status >= 200 && status < 300) {
                    return responseString;
                } else {
                    throw new RuntimeException("API request failed with status code: " + status);
                }
            });

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode ordersNode = root.path("orders");

            String nickname = "";
            String command = "";



            if (ordersNode.isArray()) {
                for (JsonNode orderNode : root) {
                    JsonNode fieldsNode = orderNode.path("fields");
                    for (JsonNode fieldNode : fieldsNode) {
                        if ("nickname".equals(fieldNode.path("name").asText())) {
                            nickname = fieldNode.path("value").asText();
                            break; // Assuming only one nickname per order
                        }
                    }
                    String productId = orderNode.path("productId").asText();

                    // Extracting command from actions
                    JsonNode actionsNode = orderNode.path("actions");
                    for (JsonNode actionNode : actionsNode) {
                        if ("command".equals(actionNode.path("type").asText())) {
                            command = actionNode.path("command").asText();
                            break; // Assuming only one command per order
                        }
                    }



                    if (command != null) {
                        try {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);

                            String completedUrl = "https://api.fvst.pl/shops/" + config.getShopIdentifier() + "/orders/" + config.getServerIdentifier() + "/completed";
                            HttpPost httpPost = new HttpPost(completedUrl);
                            getLogger().info("Executing command for product ID " + productId + ": " + command);
                        } catch (Exception e) {
                            getLogger().warning("Error executing command for product ID " + productId + ": " + e.getMessage());
                        }
                    } else {
                        getLogger().warning("No command found for product ID " + productId);
                    }
                }
            }

        } catch (Exception e) {
            getLogger().warning("Error fetching orders: " + e.getMessage());
        }
    }
}
