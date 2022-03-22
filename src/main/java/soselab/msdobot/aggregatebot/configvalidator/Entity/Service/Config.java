package soselab.msdobot.aggregatebot.configvalidator.Entity.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config {
    public String username;
    public String accessToken;
    public String endpoint;
    public String buildNumber;
    public String targetService;

    public Config(){}

    /**
     * simple config constructor for jenkins data access
     * @param username jenkins username
     * @param accessToken jenkins access token
     * @param endpoint jenkins endpoint
     */
    public Config(String username, String accessToken, String endpoint){
        this.username = username;
        this.accessToken = accessToken;
        this.endpoint = endpoint;
    }

    @Override
    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
