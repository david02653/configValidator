package soselab.msdobot.aggregatebot.configvalidator.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import soselab.msdobot.aggregatebot.configvalidator.Entity.Capability.*;
import soselab.msdobot.aggregatebot.configvalidator.Entity.Service.*;
import soselab.msdobot.aggregatebot.configvalidator.Entity.UpperIntent.UpperIntent;
import soselab.msdobot.aggregatebot.configvalidator.Entity.UpperIntent.UpperIntentList;
import soselab.msdobot.aggregatebot.configvalidator.Entity.Vocabulary.*;
import soselab.msdobot.aggregatebot.configvalidator.Exception.IllegalConceptException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * load config files
 */
@org.springframework.stereotype.Service
public class ConfigLoader {

    private final YAMLFactory yamlFactory;
    private final ObjectMapper mapper;
    private YAMLParser parser;
    private final Gson gson;
    private final String serviceConfigPath;
    private final String capabilityConfigPath;
    private final String upperIntentConfigPath;
    private final String vocabularyConfigPath;

    public static ServiceList serviceList;
    public static ArrayList<Capability> capabilityList;
    public static UpperIntentList upperIntentList;
    public static Vocabulary vocabularyList;

    /**
     * load all config file and verify loaded config, vocabulary config must be loaded before service/capability/upperIntent config
     * @param env file path
     */
    @Autowired
    public ConfigLoader(Environment env){
        yamlFactory = new YAMLFactory();
        mapper = new ObjectMapper();
        gson = new GsonBuilder().setPrettyPrinting().create();
        serviceConfigPath = env.getProperty("bot.config.service");
        capabilityConfigPath = env.getProperty("bot.config.capability");
        upperIntentConfigPath = env.getProperty("bot.config.upperIntent");
        vocabularyConfigPath = env.getProperty("bot.config.vocabulary");

        loadVocabularyConfig();
        verifyContextProperties();
        loadCapabilityConfig();
        verifyCapabilityConfig();
        loadUpperIntentConfig();
        verifyUpperIntent();
        loadServiceConfig();
        verifyServiceConfig();
    }

    /**
     * load capability config from config file
     */
    public void loadCapabilityConfig(){
        try{
            System.out.println("> try to load skill config from " + capabilityConfigPath);
            parser = yamlFactory.createParser(new File(capabilityConfigPath));
            capabilityList = mapper.readValue(parser, new TypeReference<ArrayList<Capability>>(){});
            System.out.println(">>> " + capabilityList);
            System.out.println("---");
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("> [DEBUG] capability config file load failed.");
        }
    }

    /**
     * load upper intent config from config file
     */
    public void loadUpperIntentConfig(){
        try{
            System.out.println("> try to load upper intent config from " + upperIntentConfigPath);
            parser = yamlFactory.createParser(new File(upperIntentConfigPath));
            upperIntentList = mapper.readValue(parser, UpperIntentList.class);
            System.out.println(">>> " + upperIntentList);
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("> [DEBUG] upper intent config file load failed.");
        }
        System.out.println("---");
    }

    /**
     * verify upper intent config, illegal upper intent will be ignored
     */
    private void verifyUpperIntent(){
        System.out.println("> start to verify upper intent config");
        Iterator<UpperIntent> intentIterator = upperIntentList.crossCapabilityList.iterator();
        while(intentIterator.hasNext()){
            UpperIntent currentIntent = intentIterator.next();
            System.out.println("[DEBUG] checking upper intent '" + currentIntent.name + "'");
            for(Capability step: currentIntent.sequencedCapabilityList){
                if(capabilityList.stream().noneMatch(capability -> capability.name.equals(step.name))){
                    System.out.println("[Error] Error code: U01");
                    System.out.println("[WARNING] capability '" + step.name + "' at order " + step.order + " from upper intent '" + currentIntent.name + "' is not available !");
                    System.out.println("[WARNING] system will ignore upperIntent '" + currentIntent.name + "' from now on.");
                    intentIterator.remove();
                    break;
                }
            }
        }
        System.out.println(">>> " + upperIntentList);
        System.out.println("---");
    }

    /**
     * load service config from config file
     */
    public void loadServiceConfig(){
        try{
            System.out.println("> try to load service config from " + serviceConfigPath);
            parser = yamlFactory.createParser(new File(serviceConfigPath));
            serviceList = mapper.readValue(parser, ServiceList.class);
            System.out.println(">>> " + serviceList);
//            generateServiceMap();
            System.out.println("---");
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("> [DEBUG] service config file load failed.");
        }
    }

    /**
     * load vocabulary config from config file
     */
    public void loadVocabularyConfig(){
        try{
            System.out.println("> try to load vocabulary config from " + vocabularyConfigPath);
            parser = yamlFactory.createParser(new File(vocabularyConfigPath));
            vocabularyList = mapper.readValue(parser,  Vocabulary.class);
            vocabularyList.createConceptHashMap();
            printVocabularyConfig();
            System.out.println("---");
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("> [DEBUG] vocabulary config file load failed.");
        }
    }

    /**
     * print summary of loaded vocabulary config
     */
    public void printVocabularyConfig(){
        System.out.println(gson.toJson(vocabularyList));
    }

    /**
     * verify all listed vocabulary in capability specification file is legal
     */
    public void verifyCapabilityConfig(){
        System.out.println("> start to verify capability config");
        Iterator<Capability> capabilityIterator = capabilityList.iterator();
        while(capabilityIterator.hasNext()){
            Capability currentCapability = capabilityIterator.next();
            System.out.println("[DEBUG] checking capability '" + currentCapability.name + "'");
            /* check context */
            String context = currentCapability.context;
            if(!vocabularyList.isAvailableContext(context)){
                System.out.println("[Error] Error code: C01");
                System.out.println("[WARNING] context '" + context + "' found in capability '" + currentCapability.name + "' is illegal");
                System.out.println("[WARNING] this capability will be ignored by system from now on.");
                capabilityIterator.remove();
                continue;
            }
            /* check used mapping */
            final ArrayList<String> legalMappingList;
            try {
                if(currentCapability.usedMappingList != null)
                    legalMappingList = getLegalCustomMapping(currentCapability.usedMappingList);
                else
                    legalMappingList = new ArrayList<>();
            }catch (IllegalConceptException ic){
                System.out.println("[WARNING] verification failed when processing custom mapping, this capability will be ignored from now on.");
                capabilityIterator.remove();
                continue;
            }
            /* check input */
            if(currentCapability.input.stream().anyMatch(input -> isPropertyIllegal(input, legalMappingList))){
                System.out.println("[WARNING] verification failed when processing input properties, this capability will be ignored from now on.");
                capabilityIterator.remove();
                continue;
            }
            /* check output */
            final ArrayList<String> outputStoredDataLabelList;
            try{
                outputStoredDataLabelList = getOutputStoredDataList(currentCapability.output);
            }catch (IllegalConceptException ic){
                System.out.println("[Error] Error code: C05");
                System.out.println("[WARNING] verification failed when processing output config, this capability will be ignored from now on.");
                capabilityIterator.remove();
                continue;
            }
            /* check aggregate data */
            if(currentCapability.output != null && currentCapability.output.type.equals("aggregate")){
                // check aggregate detail: context, from
                if(!isAggregateDetailLegal(currentCapability, legalMappingList)){
                    System.out.println("[WARNING] verification failed when processing aggregate details, this capability will be ignored from now on.");
                    capabilityIterator.remove();
                    continue;
                }
            }
            /* check stored data */
            if(currentCapability.storedData != null) {
                // input
                if (!isStoredDataInputLegal(currentCapability.storedData.input, currentCapability.input)) {
                    System.out.println("[WARNING] verification failed when processing storedData input config, this capability will be ignored from now on.");
                    capabilityIterator.remove();
                    continue;
                }
                // output
                if (!isStoredDataOutputLegal(currentCapability.storedData.output, outputStoredDataLabelList)) {
                    System.out.println("[WARNING] illegal output label found in storedData, this capability will be ignored from now on.");
                    capabilityIterator.remove();
                }
            }
        }
        System.out.println(">>> " + gson.toJson(capabilityList));
        System.out.println("---");
    }

    /**
     * verify capability aggregate detail config
     * @param capability target capability
     * @param legalMappingList used custom mapping list
     * @return true if aggregate detail is legal, otherwise false
     */
    private boolean isAggregateDetailLegal(Capability capability, ArrayList<String> legalMappingList){
        boolean result = true;
        if(capability.output.aggregateDetail == null) return false;
        CapabilityOutput output = capability.output;
        ArrayList<String> input = capability.input;
        // check aggregate method
        if(vocabularyList.isIllegalConceptProperty("Aggregate", output.aggregateDetail.method)) {
            System.out.println("[Error] Error code: C10");
            System.out.println("[WARNING] illegal capability aggregate method found.");
            return false;
        }
        // check aggregate context/from
        for(AggregateSource source: output.aggregateDetail.dataSource){
            // check aggregate context is available
            if(!vocabularyList.isAvailableContext(source.context)){
                System.out.println("[Error] Error code: C11");
                System.out.println("[WARNING] illegal aggregate source context '" + source.context + "' found.");
                result = false;
            }
            // check aggregate source property is legal
            if(isPropertyIllegal(source.from, legalMappingList)){
                System.out.println("[Error] Error code: C12");
                System.out.println("[WARNING] illegal aggregate source property '" + source.from + "' found");
                result = false;
            }
            // todo: need to check given aggregate property exist in input list ?
            // check if given property is not mapping and enabled in context domain
            if(vocabularyList.isIllegalContextProperty(source.context, source.from) && !legalMappingList.contains(source.from)){
                System.out.println("[Error] Error code: C13");
                System.out.println("[WARNING] given aggregate property does not exist in given context");
                result = false;
            }
        }
        return result;
    }

    private boolean isStoredDataInputLegal(ArrayList<DataLabel> dataLabelList, ArrayList<String> inputList){
        for(DataLabel dataSet: dataLabelList){
            if(isPropertyIllegal(dataSet.to)) {
                System.out.println("[Error] Error code: C06");
                System.out.println("[WARNING] data destination '" + dataSet.to + "' found in storedData input is illegal.");
                return false;
            }
            if(!inputList.contains(dataSet.from)){
                System.out.println("[Error] Error code: C07");
                System.out.println("[WARNING] data source '" + dataSet.from + "' found in storedData input is illegal.");
                return false;
            }
        }
        return true;
    }

    private boolean isStoredDataOutputLegal(ArrayList<DataLabel> dataLabels, ArrayList<String> outputDataLabelList){
        if(dataLabels == null)
            return true;
        for(DataLabel dataSet: dataLabels){
            if(isPropertyIllegal(dataSet.to)){
                System.out.println("[Error] Error code: C08");
                System.out.println("[WARNING] data destination '" + dataSet.to + "' found in storedData output is illegal.");
                return false;
            }
            if(!outputDataLabelList.contains(dataSet.from)){
                System.out.println("[Error] Error code: C09");
                System.out.println("[WARNING] data source '" + dataSet.from + "' found in storedData output is illegal.");
                return false;
            }
        }
        return true;
    }

    /**
     * verify loaded service config
     */
    private void verifyServiceConfig(){
        System.out.println("> start to verify service config.");
        for(ServiceSystem system: serviceList.serviceList){
            System.out.println("[DEBUG] start checking system '" + system.name + "'");
            /* check system-wide config */
            // context
            if(system.config != null){
                if(system.config.removeIf(config -> !vocabularyList.isAvailableContext(config.context))) {
                    System.out.println("[Error] Error code: S01");
                    System.out.println("[WARNING] illegal context found in system '" + system.name + "'.");
                    System.out.println("[WARNING] this context config setting will be ignored by system from now on.");
                }
                // context property
                for(ServiceConfig serviceConfig: system.config){
                    System.out.println("[DEBUG] checking system config setting of context '" + serviceConfig.context + "'");
                    if(serviceConfig.properties.removeIf(config -> isPropertyIllegal(config.name))){
                        System.out.println("[Error] Error code: S02");
                        System.out.println("[WARNING] illegal property found in context '" + serviceConfig.context + "' config.");
                        System.out.println("[WARNING] illegal property config will be ignored by system from now on.");
                    }
                }
            }
            /* check service config */
            System.out.println("[DEBUG] start checking subService settings of system '" + system.name + "'");
            for(Service service: system.service){
                System.out.println("[DEBUG] start checking service '" + service.name + "'");
                // context
                if(service.config != null){
                    if(service.config.removeIf(config -> !vocabularyList.isAvailableContext(config.context))){
                        System.out.println("[Error] Error code: S03");
                        System.out.println("[WARNING] illegal context found in service '" + service.name + "'.");
                        System.out.println("[WARNING] this context config setting will be ignored by system from now on.");
                    }
                    // context property
                    for(ServiceConfig serviceConfig: service.config){
                        if(serviceConfig.properties.removeIf(config -> isPropertyIllegal(config.name))){
                            System.out.println("[Error] Error code: S04");
                            System.out.println("[WARNING] illegal property found in context '" + serviceConfig.context + "' config.");
                            System.out.println("[WARNING] illegal property config will be ignored by system from now on.");
                        }
                    }
                }
            }
        }
        System.out.println(">>> " + gson.toJson(serviceList));
    }

    /**
     * check if given custom mapping list contains any illegal property
     * @param mappingList given custom mapping list
     * @return legal mapping name list, otherwise empty list
     * @throws IllegalConceptException if any schema or property is illegal
     */
    public ArrayList<String> getLegalCustomMapping(ArrayList<CustomMapping> mappingList) throws IllegalConceptException {
        ArrayList<String> legalMappingList = new ArrayList<>();
        Pattern propertyPattern = Pattern.compile("%\\{([a-zA-Z0-9-/.]+)}");
        Matcher propertyMatcher;
        for(CustomMapping mapping: mappingList){
            // check schema
            if(!isValidJsonString(mapping.schema.replaceAll("%\\{[a-zA-Z0-9-/.]+}", "\"test\""))) {
                System.out.println("[Error] Error code: C02");
                System.out.println("[WARNING] given schema is not a legal json string.");
                throw new IllegalConceptException("illegal schema format");
            }
            propertyMatcher = propertyPattern.matcher(mapping.schema);
            while(propertyMatcher.find()){
                String property = propertyMatcher.group(1);
                // check extracted property
                if(isPropertyIllegal(property)) {
                    System.out.println("[Error] Error code: C03");
                    System.out.println("[WARNING] property '" + property + "' found in mapping '" + mapping.mappingName + "' is not a legal property.");
                    throw new IllegalConceptException(property + " is illegal.");
                }
            }
            legalMappingList.add(mapping.mappingName);
        }
        return legalMappingList;
    }

    /**
     * get all dataLabel in output config
     * @param output output config
     * @return output stored data label list
     * @throws IllegalConceptException if illegal output type found
     */
    public ArrayList<String> getOutputStoredDataList(CapabilityOutput output) throws IllegalConceptException {
        // check output type
        if(!vocabularyList.getOutputConcept().contains(output.type))
            throw new IllegalConceptException("[WARNING] illegal output type detected.");
        ArrayList<String> storedDataLabelList = new ArrayList<>();
        if(output.dataLabel != null && !output.dataLabel.isEmpty())
            storedDataLabelList.add(output.dataLabel);
        // check json info
        if(output.jsonInfo != null)
            for(JsonInfo jsonInfo: output.jsonInfo)
                if(jsonInfo.dataLabel != null && !jsonInfo.dataLabel.isEmpty())
                    storedDataLabelList.add(jsonInfo.dataLabel);
        return storedDataLabelList;
    }

    /**
     * verify all used properties in each context, each property is expected existing in assigned concept<br>
     * illegal property usage will be ignored
     */
    public void verifyContextProperties(){
        System.out.println("> start to verify properties used in context");
        for(Context context: vocabularyList.ContextList){
            System.out.println("[DEBUG] checking context '" + context.contextName + "'");
            Iterator<String> iterator = context.properties.iterator();
            while(iterator.hasNext()){
                String property = iterator.next();
                System.out.println("[DEBUG] checking property '" + property + "'");
                if(isPropertyIllegal(property)){
                    System.out.println("[Error] Error code: V01");
                    System.out.println("[WARNING] system will ignore this property from now on.");
                    iterator.remove();
                }
            }
        }
        System.out.println(">>> " + gson.toJson(vocabularyList.ContextList));
        vocabularyList.createContextHashMap();
        System.out.println("---");
    }

    /**
     * check if given property is available in assigned concept, concept name and property are expected to be separated by hyphen character<br>example: conceptA-propertyA
     * @param property input property
     * @return true if illegal, otherwise false
     */
    private boolean isPropertyIllegal(String property){
        if(!property.contains("."))
            return true;
//        String[] token = property.split("-", 2);
        String[] token = property.split("\\.", 2);
        String conceptName = token[0];
        String value = token[1];
        return vocabularyList.isIllegalConceptProperty(conceptName, value);
    }

    /**
     * check if given property is available in assigned concept, concept name and property are expected to be separated by hyphen character<br>example: conceptA-propertyA<br>if given property does not match general property format (contains hyphen), check if available in given exception list
     * @param property input property
     * @param exceptionList exception property list
     * @return true if illegal, otherwise false
     */
    private boolean isPropertyIllegal(String property, ArrayList<String> exceptionList){
        if(property.contains(".")){
            String[] token = property.split("\\.", 2);
            String conceptName = token[0];
            String value = token[1];
            return vocabularyList.isIllegalConceptProperty(conceptName, value);
        }else {
            if (!exceptionList.contains(property)) {
                System.out.println("[Error] Error code: C04");
                System.out.println("[WARNING] property '" + property + "' does not exist in exception list.");
                return true;
            }
            return false;
        }
    }

    /**
     * verify if input json string is legal json format
     * @param raw json string
     * @return true if legal, otherwise false
     */
    private boolean isValidJsonString(String raw){
        try{
            gson.fromJson(raw, JsonObject.class);
        }catch (Exception e){
            try{
                gson.fromJson(raw, JsonArray.class);
            }catch (Exception ae){
                return false;
            }
        }
        return true;
    }
}
