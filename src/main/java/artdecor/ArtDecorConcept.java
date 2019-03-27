package artdecor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settings.Statics;

import java.util.*;

/**
 *
 */
public class ArtDecorConcept {
    private static final Logger logger = LogManager.getLogger(ArtDecorConcept.class.getName());

    private String statusCode;
    private String effectiveDate;
    private String type = "item";
    // code, text, etc.
    private String valueDomainType;

    // date for references
    private String refEffectiveDate="";

    private List<LanguageConcept> languageConceptList = new ArrayList<>();
    private Map<String, String> propertyMap;

    private String artdecorConceptId;
    private String artdecorConceptListId="";

    private String xmlConceptTypeStatus="NEW";
    private String xmlConceptListTypeStatus="";
    private String xmlValueDomainTypeStatus="NEW";

    private String conceptId;
    private String parent="";
    private List<ArtDecorConcept> children = new ArrayList<>();

    public ArtDecorConcept(String conceptId, String artdecorConceptId, String effectiveDate, String valueDomainType, String parent, String statusCode){
        this.conceptId = conceptId;
        this.parent = parent;
        this.artdecorConceptId = artdecorConceptId;
        this.effectiveDate = effectiveDate;
        this.valueDomainType = valueDomainType;
        this.statusCode = statusCode;
    }

    public String getArtdecorConceptId() {
        return artdecorConceptId;
    }

    // relations with other concepts

    /**
     * return this concept's parent
     * @return the concept's parent
     */
    String getParent(){
        return parent;
    }

    /**
     * return this concept's conceptId
     * @return the concept's conceptId
     */
    String getConceptId(){
        return conceptId;
    }

    /**
     * add a child to this concept
     * @param artDecorConcept the child
     */
    void addChild(ArtDecorConcept artDecorConcept){
        children.add(artDecorConcept);
        type="group";
    }

    /**
     * sort the languageConceptList to make it possible to compare to the languageConceptList of another concept
     */
    private void sortList(){
        languageConceptList.sort((o1, o2) -> o1.language.compareToIgnoreCase(o2.language));
    }



    public boolean identicalTo(ArtDecorConcept artDecorConcept){
        setValueDomainTypeStatus(artDecorConcept);
        setConceptTypeStatus(artDecorConcept);
//        if(xmlValueDomainTypeStatus.equalsIgnoreCase("CHANGED") || xmlConceptTypeStatus.equalsIgnoreCase("CHANGED")){
//            return false;
//        }
//        return true;

        if(xmlValueDomainTypeStatus.equalsIgnoreCase("CHANGED") ||
                xmlConceptTypeStatus.equalsIgnoreCase("CHANGED") ||
                xmlConceptListTypeStatus.equalsIgnoreCase("CHANGED")){
            return false;
        }
        return true;
    }

    /**
     * compare this artDecorConcept to another artDecorConcept to see whether its valueDomain is the same
     * @param artDecorConcept artDecorConcept to compare to
     */
    private void setValueDomainTypeStatus(ArtDecorConcept artDecorConcept){
        if(valueDomainType.equalsIgnoreCase(artDecorConcept.valueDomainType)){
            xmlValueDomainTypeStatus="SAME";
        }
        else{
            xmlValueDomainTypeStatus="CHANGED";
        }
    }

    /**
     * compare this artDecorConcept to another artDecorConcept to see whether its values are the same
     * this excludes the valueset, as that is handled in a separate parameter
     * @param artDecorConcept artDecorConcept to compare to
     */
    private void setConceptTypeStatus(ArtDecorConcept artDecorConcept){
        if(sameValues(artDecorConcept)){
            xmlConceptTypeStatus = "SAME";
        }
        else{
            xmlConceptTypeStatus = "CHANGED";
        }
    }

    /**
     * the status of the conceptlist will decide the xml for the concept, so we need to keep track of it
     * @param xmlConceptListTypeStatus the new xmlConceptListType status
     */
    public void setXMLConceptListTypeStatus(String xmlConceptListTypeStatus) {
        this.xmlConceptListTypeStatus = xmlConceptListTypeStatus;
    }

    /**
     * compare the conceptlist of this concept to the conceptlist of another concept
     * @param artDecorConcept concept to compare to
     * @return true/false
     */
    private boolean sameValues(ArtDecorConcept artDecorConcept){
        if(languageConceptList.size()!=artDecorConcept.languageConceptList.size() ||
                !type.equalsIgnoreCase(artDecorConcept.type)){
            return false;
        }

        sortList();
        artDecorConcept.sortList();

        for(int i=0; i<languageConceptList.size(); i++){
            LanguageConcept languageConcept1 = languageConceptList.get(i);
            LanguageConcept languageConcept2 = artDecorConcept.languageConceptList.get(i);

            if(!languageConcept1.language.equalsIgnoreCase(languageConcept2.language) ||
               !languageConcept1.description.equalsIgnoreCase(languageConcept2.description)){
                return false;
            }
        }
        return true;
    }

    public void addLanguageConcept(String language, String description){
        languageConceptList.add(new LanguageConcept(language, description));
    }

    public void setPropertyMap(Map<String, String> propertyMap){
        this.propertyMap = propertyMap;
    }

    public void setArtdecorConceptListId(String artdecorConceptListId){
        this.artdecorConceptListId = artdecorConceptListId;
    }

    public void setRefEffectiveDate(String refEffectiveDate){
        this.refEffectiveDate = refEffectiveDate;
    }


    /**
     * transform this concept to an xml representation suitable for art-decor
     * this is quite complicated, due to multiple versions of a dataset existing in one file. This leads to
     * full inheritence, new items, new codelists, reuse of existing codelists, etc.
     *
     * First we check whether the domain type has remained the same (e.g. something was a CODE and now suddenly is a STRING):
     *      if the type changed: generate an error
     *      otherwise: check whether the type is CODE
     *          if yes: check the concept's status (NEW, SAME, CHANGED) and the concept list's status (NEW, SAME, CHANGED)
     *              generate the xml based on these options
     *          if no: there's no list, so just check the concept's status (NEW, SAME, CHANGED)
     *              generate the xml based on these options
     *
     * If it turns out the XML should be different for one of the options, it should now be relatively easy to change...
     *
     * @return xml representation of the concept, suitable for art-decor
     */
    String toXML() throws Exception{
        StringBuilder stringBuilder = new StringBuilder();

        if(!xmlValueDomainTypeStatus.equalsIgnoreCase("CHANGED")) {

            stringBuilder.append("<concept id=\"" + artdecorConceptId + "\" statusCode=\"" + statusCode + "\" effectiveDate=\"" + effectiveDate + "\" type=\"" + type + "\">\n");

            if (valueDomainType.equalsIgnoreCase("code")) {
                if (xmlConceptTypeStatus.equalsIgnoreCase("NEW") && xmlConceptListTypeStatus.equalsIgnoreCase("NEW")) {
                    logger.log(Level.DEBUG, "{}: New Concept, New options", artdecorConceptId);
                    newConceptNewOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("NEW") && xmlConceptListTypeStatus.equalsIgnoreCase("SAME")) {
                    logger.log(Level.DEBUG, "{}: New Concept, Same options", artdecorConceptId);
                    newConceptSameOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("NEW") && xmlConceptListTypeStatus.equalsIgnoreCase("CHANGED")) {
                    logger.log(Level.DEBUG, "{}: New Concept, Changed options", artdecorConceptId);
                    newConceptChangedOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("SAME") && xmlConceptListTypeStatus.equalsIgnoreCase("NEW")) {
                    logger.log(Level.DEBUG, "{}: Same Concept, New options", artdecorConceptId);
                    sameConceptNewOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("SAME") && xmlConceptListTypeStatus.equalsIgnoreCase("SAME")) {
                    logger.log(Level.DEBUG, "{}: Same Concept, Same options", artdecorConceptId);
                    sameConceptSameOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("SAME") && xmlConceptListTypeStatus.equalsIgnoreCase("CHANGED")) {
                    logger.log(Level.DEBUG, "{}: Same Concept, Changed options", artdecorConceptId);
                    sameConceptChangedOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("CHANGED") && xmlConceptListTypeStatus.equalsIgnoreCase("NEW")) {
                    logger.log(Level.DEBUG, "{}: Changed Concept, New options", artdecorConceptId);
                    changedConceptNewOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("CHANGED") && xmlConceptListTypeStatus.equalsIgnoreCase("SAME")) {
                    logger.log(Level.DEBUG, "{}: Changed Concept, Same options", artdecorConceptId);
                    changedConceptSameOptions(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("CHANGED") && xmlConceptListTypeStatus.equalsIgnoreCase("CHANGED")) {
                    logger.log(Level.DEBUG, "{}: Changed Concept, Changed options", artdecorConceptId);
                    changedConceptChangedOptions(stringBuilder);
                }

            }
            else {
                if (xmlConceptTypeStatus.equalsIgnoreCase("NEW")) {
                    logger.log(Level.DEBUG, "{}: New Concept, No options", artdecorConceptId);
                    newConceptNoValueDomain(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("SAME")) {
                    logger.log(Level.DEBUG, "{}: Same Concept, No options", artdecorConceptId);
                    sameConceptNoValueDomain(stringBuilder);
                } else if (xmlConceptTypeStatus.equalsIgnoreCase("CHANGED")) {
                    logger.log(Level.DEBUG, "{}: Changed Concept, No options", artdecorConceptId);
                    changedConceptNoValueDomain(stringBuilder);
                }
            }

            // add the children
            for(ArtDecorConcept artDecorConcept:children){
                stringBuilder.append(artDecorConcept.toXML());
            }

            stringBuilder.append("</concept>\n");
        }
        else {
            throw new Exception("Fatal Error: The data type was changed, which is not allowed. Please fix. Violating conceptId="+conceptId);
//            System.err.println("The type was changed! That's an issue");
        }
        return stringBuilder.toString();
    }

    // new concept and new options
    // create everything
    private void newConceptNewOptions(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addNewValueDomainCodeList(stringBuilder);
    }

    // a new concept will always refer to a new list, so I'd say this is not possible
    private void newConceptSameOptions(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addNewValueDomainCodeList(stringBuilder);
    }

    // a new concept will always refer to a new list, so I'd say this is not possible
    private void newConceptChangedOptions(StringBuilder stringBuilder){
        stringBuilder.append("NOT ALLOWED!");
    }

    // someone changed the concept from e.g. a string to a code datatype... not allowed I think? Shouldn't happen, as
    // there's a dataype check before this
    private void sameConceptNewOptions(StringBuilder stringBuilder){
        stringBuilder.append("NOT ALLOWED!");
    }

    // concept stays the same and options stay the same
    // inherit everything
    private void sameConceptSameOptions(StringBuilder stringBuilder){
        addRefTag(stringBuilder);
    }

    // concept stayed the some but the options were changed
    // not 100% sure whether we need the SPEC tag, or whether this is only necessary if you change the concept itself
    private void sameConceptChangedOptions(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addSpecTag(stringBuilder);
        addNewValueDomainCodeList(stringBuilder);
    }

    // someone changed the concept from e.g. a string to a code datatype... not allowed I think?
    private void changedConceptNewOptions(StringBuilder stringBuilder){
        stringBuilder.append("NOT ALLOWED!");
    }

    // concept was changed but the options stayed the same
    // keep the relationship with the old concept and refer to the old valuedomain
    private void changedConceptSameOptions(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addSpecTag(stringBuilder);
        addRefValueDomainCodeList(stringBuilder);
    }

    // concept was changed and options were changed
    // keep the relationship with the old concept and create new valuedomain
    private void changedConceptChangedOptions(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addSpecTag(stringBuilder);
        addNewValueDomainCodeList(stringBuilder);
    }

    private void newConceptNoValueDomain(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addNewValueDomainNoCodelist(stringBuilder);
    }

    private void sameConceptNoValueDomain(StringBuilder stringBuilder){
        addRefTag(stringBuilder);
    }

    // not sure... probably just attach the domain again, since we can't refer to some existing list?
    private void changedConceptNoValueDomain(StringBuilder stringBuilder){
        addNewHeader(stringBuilder);
        addSpecTag(stringBuilder);
        addNewValueDomainNoCodelist(stringBuilder);
    }

    private void addNewValueDomainNoCodelist(StringBuilder stringBuilder){
        stringBuilder.append("<valueDomain type=\""+valueDomainType+"\">\n");
        stringBuilder.append("</valueDomain>\n");
    }

    private void addRefTag(StringBuilder stringBuilder){
        stringBuilder.append("<inherit ref=\"" + artdecorConceptId + "\" effectiveDate=\"" + refEffectiveDate + "\"/>\n");
    }


    private void addNewHeader(StringBuilder stringBuilder){
        // name and desc both use the description since we don't have anything better at the moment
        for(LanguageConcept languageConcept:languageConceptList){
            stringBuilder.append("<name language=\""+ languageConcept.language+"\">"+languageConcept.description+"</name>\n");
        }

        for(LanguageConcept languageConcept:languageConceptList){
            stringBuilder.append("<desc language=\""+ languageConcept.language+"\">"+languageConcept.description+"</desc>\n");
        }

        // add properties
        for(Map.Entry property:propertyMap.entrySet()){
            stringBuilder.append("<property name=\""+property.getKey()+"\">"+property.getValue()+"</property>\n");
        }
    }

    private void addNewValueDomainCodeList(StringBuilder stringBuilder){
        stringBuilder.append("<valueDomain type=\""+valueDomainType+"\">\n");
        stringBuilder.append("<conceptList id=\""+artdecorConceptListId+"\"/>\n");
        stringBuilder.append("</valueDomain>\n");
    }

    private void addRefValueDomainCodeList(StringBuilder stringBuilder){
        stringBuilder.append("<valueDomain type=\""+valueDomainType+"\">\n");
        stringBuilder.append("<conceptList ref=\""+artdecorConceptListId+"\"/>\n");
        stringBuilder.append("</valueDomain>\n");
    }

    private void addSpecTag(StringBuilder stringBuilder){
        stringBuilder.append("<relationship type=\"SPEC\" ref=\""+artdecorConceptId+"\" flexibility=\""+refEffectiveDate+"\"/>\n");
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

//    public String getEffectiveDate() {
//        return effectiveDate;
//    }



    private class LanguageConcept{
        private String language;
        private String description;

        LanguageConcept(String language, String description){
            this.description = description;
            this.language = Statics.getArtDecorLanguage(language);
        }
    }
}
