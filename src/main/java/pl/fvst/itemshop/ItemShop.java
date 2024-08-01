package pl.fvst.itemshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.fvst.itemshop.config.Config;
import pl.fvst.itemshop.config.ConfigLoader;
import pl.fvst.itemshop.task.OrderTask;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ItemShop extends JavaPlugin {

    private Config config;
    private ConcurrentHashMap<String, String> products = new ConcurrentHashMap<>(); // <ProductIdentifier, ProductAction>

    @Override
    public void onEnable() {
        String fileName = "config.json";
        String filePath = getDataFolder().getAbsolutePath() + "/" + fileName;

        if (!new File(filePath).exists()) {
            Config defaultConfig = new Config();
            defaultConfig.setApiKey("");
            defaultConfig.setShopIdentifier("");
            defaultConfig.setServerIdentifier("");
            defaultConfig.setDebug(false);

            ConfigLoader.save(filePath, defaultConfig);
            getLogger().info("Created new config file.");
        }

        config = ConfigLoader.load(filePath, Config.class);


        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            String endpoint = "https://api.fvst.pl/shops/" + config.getShopIdentifier() + "/products";

            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(endpoint);
            httpGet.setHeader("x-api-key", config.getApiKey());

            try {
                String jsonResponse = httpClient.execute(httpGet, response -> {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        return EntityUtils.toString(response.getEntity());
                    } else {
                        throw new RuntimeException("API request failed with status code: " + status);
                    }
                });

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(jsonResponse);

                JsonNode productsNode = root.path("products");
                if (productsNode.isArray()) {
                    productsNode.forEach(productNode -> {
                        String productId = productNode.path("id").asText();
                        JsonNode actionNode = productNode.path("actions");
                        if (actionNode.isObject() && "command".equals(actionNode.path("type").asText())) {
                            String command = actionNode.path("command").asText();
                            products.put(productId, command);
                            getLogger().info(products.toString());
                        }
                    });
                }

                // Print ConcurrentHashMap (for verification)
                products.forEach((productId, command) -> {
                    getLogger().info("Product ID: " + productId + ", Command: " + command);
                });

            } catch (Exception e) {
                getLogger().warning("Error fetching products: " + e.getMessage());
            }
        });

        new OrderTask(config, products).runTaskTimerAsynchronously(this, 0L, 200L);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}