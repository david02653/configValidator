package soselab.msdobot.aggregatebot.configvalidator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class ConfigValidatorApplicationTests {

    @Test
    void contextLoads() {
    }

    private boolean validJsonViaGson(String raw){
//        Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
//            gson.fromJson(raw, JsonObject.class);
            System.out.println(gson.toJson(gson.fromJson(raw, JsonObject.class)));
        }catch (Exception e){
            e.printStackTrace();
            try{
//                gson.fromJson(raw, JsonArray.class);
                System.out.println(gson.toJson(gson.fromJson(raw, JsonArray.class)));
            }catch (Exception ae){
                ae.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String escape(String json){
        Pattern pattern = Pattern.compile("%\\{([a-zA-Z0-9-/]+)\\}");
        Matcher matcher = pattern.matcher(json);
        while(matcher.find()){
            String content = matcher.group(1);
            System.out.println("current content: " + matcher.group(1));
            json = json.replaceAll("%\\{" + content + "\\}", "\"" + content + "\"");
        }
        System.out.println("esc: " + json);
        return json;
    }

    @Test
    void jsonTest(){
        String test = "{name: %{jenkins/username}, test: %{jenkins/not}}";
//        System.out.println(isValidJsonString(test));
        System.out.println(validJsonViaGson(escape(test)));
    }
}
