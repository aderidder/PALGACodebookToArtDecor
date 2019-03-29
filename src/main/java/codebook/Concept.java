package codebook;

import artdecor.ArtDecorConcept;
import artdecor.ArtDecorValueSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import settings.IdentifierManager;
import settings.Statics;
import utils.ExcelUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a concept as read in the excel
 */
class Concept {
    private static final Logger logger = LogManager.getLogger(Concept.class.getName());

    private String effectiveDate;
    private String id;
    private String codesystem;
    private String code;
    private String description_code;
    private String codelist_ref="";
    private String parent;
    private String data_type;
    private String versionLabel;
    private String statusCode;
    private Map<String, LanguageConcept> languageConceptMap = new HashMap<>();
    private Map<String, ConceptOption> conceptOptionsMap = new HashMap<>();
    private Map<String, String> propertiesMap = new HashMap<>();

    Concept(String id, String codesystem, String code, String description_code, String properties, String codelist_ref, String parent, String data_type, String effectiveDate, String versionLabel, String statusCode){
        this.effectiveDate = effectiveDate;
        this.id = id;
        this.code = code;
        this.codesystem = codesystem;
        this.description_code = description_code;
        this.codelist_ref = codelist_ref;
        this.parent = parent;
        this.versionLabel = versionLabel;
        this.data_type = data_type;
        this.statusCode = statusCode;
        handleProperties(properties);
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    void addCodeListEntry(Row row, List<String> codelistHeaderList, Set<String> languages){
        String codesystem = ExcelUtils.getValue(row, "codesystem", codelistHeaderList);
        String code = ExcelUtils.getValue(row, "code", codelistHeaderList);
        String description_code = ExcelUtils.getValue(row, "description_code", codelistHeaderList);

        // in some cases the codebook contains a blank line, which we do not want to add to our codelists
        // first check whether one of the mandatory fields is empty
        if(code.equalsIgnoreCase("") || codesystem.equalsIgnoreCase("") || description_code.equalsIgnoreCase("")) {
            // next check one of the mandatory fields is not empty; if so give an error
            if(!code.equalsIgnoreCase("") || !codesystem.equalsIgnoreCase("") || !description_code.equalsIgnoreCase("")){
                logger.log(Level.ERROR, "Mandatory values missing in a codelist entry for concept "+id+" (code, codesystem and description of the code need a value)");
            }
        }
        else{
            ConceptOption conceptOption = new ConceptOption(codesystem, code, description_code);
            for (String language : languages) {
                String languageDescription = ExcelUtils.getValue(row, "description_" + language, codelistHeaderList);
                String languageValue = ExcelUtils.getValue(row, "value_" + language, codelistHeaderList);
                LanguageConceptOptions languageConceptOptions = new LanguageConceptOptions(language, languageValue, languageDescription);
                conceptOption.addLanguageConceptOptions(language, languageConceptOptions);
            }
            conceptOptionsMap.put(conceptOption.code, conceptOption);
        }
    }

    ArtDecorValueSet generateArtDecorValueSet(){
        ArtDecorValueSet artDecorValueSet = new ArtDecorValueSet(id, id, versionLabel, effectiveDate);
        for(ConceptOption conceptOption:conceptOptionsMap.values()){
            String codesystemName = conceptOption.codesystemName;
            artDecorValueSet.addConceptOption(conceptOption.code,
                    IdentifierManager.getIdentifierManager().getCodeSystemId(codesystemName, effectiveDate),
                    codesystemName,
                    conceptOption.description_code);
            conceptOption.addValueSetDesignations(artDecorValueSet);
        }

        for(LanguageConcept languageConcept:languageConceptMap.values()){
            String language = languageConcept.language;
            artDecorValueSet.addConceptLanguageValueSet(language, Statics.getOptionsInLanguage(language)+" "+languageConcept.description);
        }

        return artDecorValueSet;
    }

    ArtDecorConcept generateArtDecorConcept(String artdecorConceptId){
        ArtDecorConcept artDecorConcept;
        if(conceptOptionsMap.size()>0){
            String type = "code";
            artDecorConcept = new ArtDecorConcept(id, artdecorConceptId, effectiveDate, type, parent, statusCode);
        }
        else{
            String type = Statics.getArtDecorValueDomainType(data_type);
            artDecorConcept = new ArtDecorConcept(id, artdecorConceptId, effectiveDate, type, parent, statusCode);
        }

        artDecorConcept.setPropertyMap(propertiesMap);
        for(LanguageConcept languageConcept:languageConceptMap.values()){
            artDecorConcept.addLanguageConcept(languageConcept.language, languageConcept.description);
        }
        return artDecorConcept;
    }

    String getEffectiveDate() {
        return effectiveDate;
    }

    String getCodesystem() {
        return codesystem;
    }

    String getDescription_code() {
        return description_code;
    }

    String getCodelist_ref() {
        return codelist_ref;
    }

    void addLanguageConcept(String language, String languageDescription){
        LanguageConcept languageConcept = new LanguageConcept(language, languageDescription);
        languageConceptMap.put(language, languageConcept);
    }

    boolean hasConceptOptions(){
        return conceptOptionsMap.size()>0;
    }

    // e.g. {DATA_COLNAME=Aantalinzendingen}{OTHER_PROPERTY=SomeValue}
    private void handleProperties(String properties){
        if(!properties.equalsIgnoreCase("")){
            Pattern pattern = Pattern.compile("\\{(.+?)=(.+?)}.*");
            Matcher matcher = pattern.matcher(properties);
            while(matcher.find()){
                propertiesMap.put(matcher.group(1).trim(), matcher.group(2).trim());
            }
        }
    }

    private class LanguageConcept{
        private String language;
        private String description;

        LanguageConcept(String language, String description){
            this.description = description;
            this.language = language;
        }
    }

    private class ConceptOption {
        private String codesystemName;
        private String code;
        private String description_code;
        private Map<String, LanguageConceptOptions> languageConceptOptionsMap = new HashMap<>();

        ConceptOption(String codesystemName, String code, String description_code){
            this.code = code;
            this.codesystemName = codesystemName;
            this.description_code = description_code;
        }

        private void addLanguageConceptOptions(String language, LanguageConceptOptions languageConceptOptions){
            languageConceptOptionsMap.put(language, languageConceptOptions);
        }

        private void addValueSetDesignations(ArtDecorValueSet artDecorValueSet){
            for(LanguageConceptOptions languageConceptOptions:languageConceptOptionsMap.values()){
                artDecorValueSet.addConceptDesignation(languageConceptOptions.language, languageConceptOptions.description);
            }
        }
    }

    private class LanguageConceptOptions{
        private String language;
        private String value;
        private String description;

        LanguageConceptOptions(String language, String value, String description){
            this.description = description;
            this.language = language;
            this.value = value;
        }
    }


}




