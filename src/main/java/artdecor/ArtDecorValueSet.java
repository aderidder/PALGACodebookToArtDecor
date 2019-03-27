package artdecor;

import settings.Statics;

import java.util.*;

/**
 * Representation of an Art-Decor Valueset
 */
public class ArtDecorValueSet {
    private String name;
    private String displayName;
    private String versionLabel;
    private String effectiveDate;
    private String statusCode = "draft";

    private List<ConceptOption> conceptOptionList = new ArrayList<>();
    private List<LanguageValueSet> languageValueSetList = new ArrayList<>();

    private String artdecorValueSetId;


    public ArtDecorValueSet(String name, String displayName, String versionLabel, String effectiveDate){
        this.name = name;
        this.displayName = displayName;
        this.versionLabel = versionLabel;
        this.effectiveDate = effectiveDate;
    }

    public void setArtdecorValueSetId(String artdecorValueSetId) {
        this.artdecorValueSetId = artdecorValueSetId;
    }

    public void addConceptLanguageValueSet(String language, String description){
        languageValueSetList.add(new LanguageValueSet(language, description));
    }

    public void addConceptOption(String conceptCode, String conceptCodeSystem, String conceptCodeSystemName, String displayName){
        conceptOptionList.add(new ConceptOption(conceptCode, conceptCodeSystem, conceptCodeSystemName, displayName));
    }

    public void addConceptDesignation(String language, String displayName){
        conceptOptionList.get(conceptOptionList.size()-1).addDesignation(language, displayName);
    }

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }


    private void sortList(){
        conceptOptionList.sort((o1, o2) -> o1.conceptCode.compareToIgnoreCase(o2.conceptCode));
    }

    public boolean sameValues(ArtDecorValueSet otherValueSet){
        if(conceptOptionList.size()!=otherValueSet.conceptOptionList.size())
            return false;

        // sort the lists by code
        sortList();
        otherValueSet.sortList();

        for(int i=0; i<conceptOptionList.size(); i++){
            ConceptOption conceptOption1 = conceptOptionList.get(i);
            ConceptOption conceptOption2 = otherValueSet.conceptOptionList.get(i);
            if(!conceptOption1.conceptCode.equalsIgnoreCase(conceptOption2.conceptCode) ||
               !conceptOption1.conceptCodeSystem.equalsIgnoreCase(conceptOption2.conceptCodeSystem) ||
               !conceptOption1.displayName.equalsIgnoreCase(conceptOption2.displayName)){
                return false;
            }
        }
        return true;
    }

    public String toXML(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<valueSet name=\""+name+"_Opts\" displayName=\""+displayName+" Options\" versionLabel=\""+versionLabel+"\" id=\""+artdecorValueSetId+"\" effectiveDate=\""+effectiveDate+"\" statusCode=\""+statusCode+"\">\n");

        for(LanguageValueSet languageValueSet:languageValueSetList){
            stringBuilder.append("<desc language=\""+languageValueSet.language+"\">\n");
            stringBuilder.append(languageValueSet.description+"\n");
            stringBuilder.append("</desc>\n");
        }

        stringBuilder.append("<conceptList>\n");
        for(ConceptOption conceptOption:conceptOptionList){
            stringBuilder.append(conceptOption.toXML());
        }
        stringBuilder.append("</conceptList>\n");
        stringBuilder.append("</valueSet>\n");

        return stringBuilder.toString();
    }

    public String getArtDecorValueSetId() {
        return artdecorValueSetId;
    }

//    // keep or somehow move?
//    public void setArtDecorConceptListId(String artDecorConceptListId) {
//        this.artdecorConceptListId = artDecorConceptListId;
//    }
//
//    // keep or somehow move?
//    public String getArtDecorConceptListId() {
//        return artdecorConceptListId;
//    }


    private class ConceptOption {
        private String conceptCode;
        private String conceptCodeSystem;
        private String conceptCodeSystemName;
        private String displayName;
        private String level = "0";
        private String type = "L";

        private List<Designation> designationList = new ArrayList<>();


        ConceptOption(String conceptCode, String conceptCodeSystem, String conceptCodeSystemName, String displayName){
            this.conceptCodeSystemName = conceptCodeSystemName;
            this.conceptCodeSystem = conceptCodeSystem;
            this.conceptCode = conceptCode;
            this.displayName = displayName;
        }

        private void addDesignation(String language, String displayName){
            designationList.add(new Designation(language, displayName));
        }

        private String toXML(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<concept code=\""+conceptCode+"\" codeSystem=\""+ conceptCodeSystem+"\" codeSystemName=\""+conceptCodeSystemName+"\" displayName=\""+displayName+"\" level=\""+level+"\" type=\""+type+"\">\n");
            for(Designation designation:designationList){
                stringBuilder.append(designation.toXML());
            }
            stringBuilder.append("</concept>\n");
            return stringBuilder.toString();
        }
    }

    private class LanguageValueSet{
        private String language;
        private String description;

        LanguageValueSet(String language, String description){
            this.description = description;
            this.language = Statics.getArtDecorLanguage(language);
        }
    }

    private class Designation {
        private String language;
        private String type = "preferred";
        private String displayName;

        Designation(String language, String displayName){
            this.displayName = displayName;
            this.language = language;
        }

        private String toXML(){
            return "<designation language=\""+ Statics.getArtDecorLanguage(language)+"\" type=\""+type+"\" displayName=\""+displayName+"\"/>\n";
        }
    }

}
