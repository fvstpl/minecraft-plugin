package dev.xkotelek;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.util.List;

public class PurchaseManager {

    private static final FVST fvst = (FVST) Bukkit.getPluginManager().getPlugin("FVST");
    private static boolean debug = fvst.getConfig().getBoolean("debug");
    private static String shopId = fvst.getConfig().getString("shopId");
    private static String serverId = fvst.getConfig().getString("serverId");
    private static String apiKey = fvst.getConfig().getString("apiKey");
    private static List<String> boughtMessages = fvst.getConfig().getStringList("boughtMessage");
    private static Gson gson = new Gson();

    public static boolean checkConfigValues() {
        if (shopId == null || shopId.isEmpty() || serverId == null || serverId.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            if (debug) {
                System.out.println("[FVST] Configuration file is incorrect!");
            }
            fvst.checkAndFixConfig();
            return false;
        }
        return true;
    }

    public static void checkForPurchases() {
        if (!checkConfigValues()) {
            return;
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String url = "http://zkww0skckco0ko8gsg4ckww4.207.127.94.150.sslip.io/shops/" + shopId + "/orders?serverId=" + serverId + "&delivered=false";
            HttpGet httpget = new HttpGet(url);
            httpget.addHeader("x-api-key", apiKey);
            HttpEntity entity = httpclient.execute(httpget).getEntity();

            if (entity != null) {
                String result = EntityUtils.toString(entity);
                JsonArray jsonArray = gson.fromJson(result, JsonArray.class);

                for (JsonElement element : jsonArray) {
                    JsonObject order = element.getAsJsonObject();
                    String orderId = order.get("id").getAsString();
                    String playerName = "nieznany gracz";

                    JsonArray fields = order.getAsJsonArray("fields");
                    for (JsonElement fieldElement : fields) {
                        JsonObject field = fieldElement.getAsJsonObject();
                        if (field.get("name").getAsString().equalsIgnoreCase("nickname")) {
                            playerName = field.get("value").getAsString();
                            break;
                        }
                    }

                    String itemName = order.has("productName") ? order.get("productName").getAsString() : "nieznany przedmiot";
                    String command = "NONE";

                    JsonArray actions = order.getAsJsonArray("actions");
                    for (JsonElement actionElement : actions) {
                        JsonObject action = actionElement.getAsJsonObject();
                        if (action.get("type").getAsString().equalsIgnoreCase("command")) {
                            command = action.get("command").getAsString().replace("{{player}}", playerName);
                            break;
                        }
                    }

                    if (debug) {
                        System.out.println("[FVST] Order - Player: " + playerName + ", Item: " + itemName + ", Command: " + command);
                    }

                    markAsDelivered(orderId);
                    broadcastMessages(playerName, itemName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void markAsDelivered(String orderId) {
        String markAsDeliveredUrl = "http://zkww0skckco0ko8gsg4ckww4.207.127.94.150.sslip.io/shops/" + shopId + "/orders/" + orderId + "/complete";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("delivered", true);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost(markAsDeliveredUrl);
            httppost.addHeader("x-api-key", apiKey);
            httppost.addHeader("Content-Type", "application/json");
            httppost.setEntity(new StringEntity(gson.toJson(jsonBody)));

            HttpEntity postEntity = httpclient.execute(httppost).getEntity();
            if (postEntity != null) {
                String postResult = EntityUtils.toString(postEntity);
                JsonObject jsonResponse = gson.fromJson(postResult, JsonObject.class);
                if (jsonResponse.has("message")) {
                    String message = jsonResponse.get("message").getAsString();
                    if ("Order completed".equals(message) && debug) {
                        System.out.println("[FVST] Order with id " + orderId + " has been completed.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastMessages(String playerName, String itemName) {
        for (String message : boughtMessages) {
            if (message != null && !message.isEmpty()) {
                message = message.replace("%player%", playerName).replace("%item%", itemName);
                message = ChatColor.translateAlternateColorCodes('&', message);
                Bukkit.broadcastMessage(message);
            }
        }
    }
}
