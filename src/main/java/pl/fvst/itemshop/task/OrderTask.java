package pl.fvst.itemshop.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import pl.fvst.itemshop.config.Config;
import pl.fvst.itemshop.config.ConfigLoader;
import pl.fvst.itemshop.data.ResponseJson;
import pl.fvst.itemshop.data.sub.Action;
import pl.fvst.itemshop.data.sub.Field;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getLogger;

public class OrderTask extends BukkitRunnable {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

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

            String str = jsonResponse.substring(1, jsonResponse.length() - 1);
            var duzeJajco = gson.fromJson(str, ResponseJson.class);

            if (!duzeJajco.getStatus().equalsIgnoreCase("paid")) return;
            for (Action action : duzeJajco.getActions()) {
                for (Field field : duzeJajco.getFields()) {
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Fvst-Itemshop"), () -> {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), action.getCommand());

                    });
                    System.out.println("japierdole praca w fvst to nie przyjemnosc tylko katorga zabicjei mnie prosze<3");
                }
            }

            duzeJajco.setDelivered(true);

            //odeslac trzeba req
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
