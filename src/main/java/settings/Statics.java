package settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Static parameters
 */
public class Statics {
    private static Map<String, String> languageMap = new HashMap<>();
    private static Map<String, String> valueDomainTypeMap = new HashMap<>();
    private static Map<String, String> optionsInLangaugeMap = new HashMap<>();

    static{
        languageMap.put("nl", "nl-NL");
        languageMap.put("en", "en-US");
//        languageMap.put("nl-NL", "nl-NL");
//        languageMap.put("en-US", "en-US");

//        valueDomainTypeMap.put("st", "string");
//        valueDomainTypeMap.put("string", "string");
//        valueDomainTypeMap.put("text", "string");
//        valueDomainTypeMap.put("date", "date");
//        valueDomainTypeMap.put("int", "quantity");
//        valueDomainTypeMap.put("real", "quantity");

        // at the moment we're sticking to either code or string
        valueDomainTypeMap.put("st", "string");
        valueDomainTypeMap.put("string", "string");
        valueDomainTypeMap.put("text", "string");
        valueDomainTypeMap.put("date", "string");
        valueDomainTypeMap.put("int", "string");
        valueDomainTypeMap.put("real", "string");


        optionsInLangaugeMap.put("nl", "Opties voor");
        optionsInLangaugeMap.put("en", "Options for");
    }

    public static String getOptionsInLanguage(String language){
        return optionsInLangaugeMap.get(language);
    }

    public static String getArtDecorLanguage(String language){
        return languageMap.get(language);
    }

    public static Set<String> getLanguages(){
        return languageMap.keySet();
    }

    public static String getArtDecorValueDomainType(String type){
        if(valueDomainTypeMap.containsKey(type.toLowerCase())) {
            return valueDomainTypeMap.get(type.toLowerCase());
        }
        return "string";
    }
}
