package pl.fvst.itemshop.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.FileReader;
import java.io.FileWriter;

@Log
public class ConfigLoader {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @SneakyThrows
    public static <T> T load(String filePath, Class<T> configClass) {
        @Cleanup FileReader reader = new FileReader(filePath);
        return gson.fromJson(reader, configClass);
    }

    @SneakyThrows
    public static void save(String filePath, Object config) {
        @Cleanup FileWriter writer = new FileWriter(filePath);
        gson.toJson(config, writer);
    }
}
