package soselab.msdobot.aggregatebot.configvalidator.Entity.Capability;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class Capability {
    public String name;
    public String accessLevel;
    public String order;
    public String description;
    public String method;
    public String atomicIntent;
    public String apiEndpoint;
    public ArrayList<String> input;
    public CapabilityOutput output;

    public Capability(){}

    @Override
    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
