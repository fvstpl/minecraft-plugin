package dev.xkotelek;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class PurchaseManager {

    // FVST Minecraft Plugin 1.0
    // by xKotelek @ https://kotelek.dev
    // https://github.com/fvstpl/minecraft-plugin/

    private static final FVST fvst = (FVST) Bukkit.getPluginManager().getPlugin("FVST");

    private static boolean debug = fvst.getConfig().getBoolean("debug");
    private static String shopId = fvst.getConfig().getString("shopId");
    private static String serverId = fvst.getConfig().getString("serverId");
    private static String apiKey = fvst.getConfig().getString("apiKey");
    private static List<String> boughtMessages = fvst.getConfig().getStringList("boughtMessage");

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
//            String url = "https://api.fvst.pl/shops/" + shopId + "/orders?serverId=" + serverId + "&delivered=false";
            HttpGet httpget = new HttpGet(url);

            httpget.addHeader("x-api-key", apiKey);

            org.apache.http.HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String result = EntityUtils.toString(entity);

                if (isJSONArray(result)) {
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject order = jsonArray.getJSONObject(i);

                        String orderId = order.getString("id");
                        String playerName = "nieznany gracz";
                        JSONArray fields = order.getJSONArray("fields");
                        for (int j = 0; j < fields.length(); j++) {
                            JSONObject field = fields.getJSONObject(j);
                            if (field.getString("name").equalsIgnoreCase("nickname")) {
                                playerName = field.getString("value");
                                break;
                            }
                        }

                        String itemName = "nieznany przedmiot";
                        if (order.has("productName")) {
                            itemName = order.getString("productName");
                        }

                        String command = "NONE";
                        JSONArray actions = order.getJSONArray("actions");
                        for (int k = 0; k < actions.length(); k++) {
                            JSONObject action = actions.getJSONObject(k);
                            if (action.getString("type").equalsIgnoreCase("command")) {
                                command = action.getString("command");

                                if (command.contains("{{player}}")) {
                                    command = command.replace("{{player}}", playerName);
                                }
                                break;
                            }
                        }

                        if (debug) {
                            System.out.println("[FVST] Order " + i + " - Player: " + playerName + ", Item: " + itemName + ", Command: " + command);
                        }

                        String markAsDeliveredUrl = "http://zkww0skckco0ko8gsg4ckww4.207.127.94.150.sslip.io/shops/" + shopId + "/orders/" + orderId + "/complete";
//                        String markAsDeliveredUrl = "https://api.fvst.pl/shops/" + shopId + "/orders/" + orderId + "/complete";

                        JSONObject jsonBody = new JSONObject();
                        jsonBody.put("delivered", true);

                        HttpPost httppost = new HttpPost(markAsDeliveredUrl);
                        httppost.addHeader("x-api-key", apiKey);
                        httppost.addHeader("Content-Type", "application/json");
                        httppost.setEntity(new StringEntity(jsonBody.toString()));

                        org.apache.http.HttpResponse postResponse = httpclient.execute(httppost);
                        HttpEntity postEntity = postResponse.getEntity();

                        if (postEntity != null) {
                            String postResult = EntityUtils.toString(postEntity);

                            if (isJSONObject(postResult)) {
                                JSONObject jsonResponse = new JSONObject(postResult);
                                if (jsonResponse.has("message")) {
                                    String message = jsonResponse.getString("message");
                                    if (message == "Order completed" && debug) {
                                        System.out.println("[FVST] Order with id " + orderId + " has been completed.");
                                    }
                                }
                            } else if(debug) {
                                System.out.println("[FVST] Mark as delivered response: " + postResult);
                            }
                        }

                        for (String message : boughtMessages) {
                            if (message != null && !message.isEmpty()) {
                                message = message.replace("%player%", playerName).replace("%item%", itemName);
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                Bukkit.broadcastMessage(message);
                            } else {
                                Bukkit.broadcastMessage("");
                            }
                        }
                    }
                    return;
                } else if (isJSONObject(result)) {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.has("message")) {
                        String message = jsonResponse.getString("message");
                        if(debug) {
                            System.out.println("[FVST] " + message);
                        }
                        return;
                    }
                } else {
                    if(debug) {
                        System.out.println("[FVST] Response is not a valid JSON.");
                    }
                    return;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private static boolean isJSONObject(String response) {
        try {
            new JSONObject(response);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static boolean isJSONArray(String response) {
        try {
            new JSONArray(response);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
