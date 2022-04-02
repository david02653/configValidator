package soselab.msdobot.aggregatebot.configvalidator.Entity.Service;

import java.util.ArrayList;

public class ServiceSystem {
    public String name;
    public String type;
    public String description;
    public ArrayList<ServiceConfig> config;
    // todo: subService -> Service
    public ArrayList<Service> service;

    public ServiceSystem(){}

    public int subSystemCount(){
        return service.size();
    }

    public ArrayList<Service> getSubService(){
        return this.service;
    }
}
