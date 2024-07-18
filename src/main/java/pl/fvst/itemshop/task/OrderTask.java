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
        HttpGet httpGet = new HttpGet("https://api.fvst.pl/shops/" + config.getShopIdentifier() + "orders?serverId=" + config.getServerIdentifier() );

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

            JsonNode ordersNode = root.path("orders");
            if (ordersNode.isArray()) {
                ordersNode.forEach(orderNode -> {
                    String productId = orderNode.path("productId").asText();

                    String command = products.get(productId);
                    if (command != null) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                        HttpPost httpPost = new HttpPost("https://api.fvst.pl/shop/"+config.getShopIdentifier() + "/orders/"+ config.getServerIdentifier()+" /completed" );
                        getLogger().info("Executing command for product ID " + productId + ": " + command);
                    }
                });
            }

        } catch (Exception e) {
            getLogger().warning("Error fetching orders: " + e.getMessage());
        }

    }
}
