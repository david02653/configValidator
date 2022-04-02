package soselab.msdobot.aggregatebot.configvalidator.Entity.Service;

import java.util.ArrayList;
import java.util.HashMap;

public class Service {
    public String name;
    public String type;
    public String description;
    public ArrayList<ServiceConfig> config;
    public HashMap<String, String> configMap;

    public Service(){}
    public Service(String serviceName, String type, String description, ArrayList<ServiceConfig> config){
        this.name = serviceName;
        this.type = type;
        this.description = description;
        this.config = config;
    }


    public HashMap<String, String> getConfigMap() {
        return configMap;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
