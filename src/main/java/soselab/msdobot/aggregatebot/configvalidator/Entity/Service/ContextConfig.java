package soselab.msdobot.aggregatebot.configvalidator.Entity.Service;

import com.google.gson.GsonBuilder;

public class ContextConfig {

    public String name;
    public String value;

    public ContextConfig(){
    }

    @Override
    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
