package util;

import com.google.gson.Gson;

public class Convert {

    public static <T> T convertJsonToObject(String jsonString, Class<T> type) {
        try {
            return new Gson().fromJson(jsonString, type);
        } catch (Exception e) {
            return null;
        }

    }

    public static int convertHexToInt(char... hex) {
        return Integer.parseInt("" + hex[0] + hex[1] + hex[2] + hex[3], 16);
    }
}
