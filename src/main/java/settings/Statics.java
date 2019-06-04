package settings;

import java.util.*;

/**
 * Static parameters
 */
public class Statics {
    private static Map<String, String> languageMap = new HashMap<>();
    private static Map<String, String> valueDomainTypeMap = new HashMap<>();
    private static Map<String, String> optionsInLangaugeMap = new HashMap<>();
    private static List<String> exceptionCodelists = new ArrayList<>();
    private static Map<String,String> typoMap = new HashMap<>();

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

        // art-decor support the following data types:
        // https://art-decor.org/mediawiki/index.php?title=DECOR-dataset
        // attempting to convert some of our datatypes to their datatypes
        valueDomainTypeMap.put("numeric", "decimal");
        valueDomainTypeMap.put("text", "string");
        valueDomainTypeMap.put("st", "string");
        valueDomainTypeMap.put("string", "string");
        valueDomainTypeMap.put("date", "date");
        valueDomainTypeMap.put("int", "count");
        valueDomainTypeMap.put("real", "decimal");

        optionsInLangaugeMap.put("nl", "Opties voor");
        optionsInLangaugeMap.put("en", "Options for");

        exceptionCodelists.add("2.16.840.1.113883.5.1008"); // Nullflavors

        typoMap.put("SNOMEDCT", "SNOMED CT");
    }

    public static boolean mayBeTypo(String codesystem){
        return typoMap.containsKey(codesystem);
    }

    public static String getTypoValue(String codesystem){
        return typoMap.get(codesystem);
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
        System.out.println("NOT FOUND FOR CONVERSION: "+type);
        return "string";
    }

    public static boolean isExceptionCodeList(String codeId){
        return exceptionCodelists.contains(codeId);
    }
}
