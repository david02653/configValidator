package soselab.msdobot.aggregatebot.configvalidator.Entity.Service;

import java.util.ArrayList;

public class ServiceSystem {
    public String name;
    public String type;
    public String description;
    public Config config;
    public ArrayList<SubService> subService;

    public ServiceSystem(){}

    public int subSystemCount(){
        return subService.size();
    }

    public ArrayList<SubService> getSubService(){
        return this.subService;
    }
}
