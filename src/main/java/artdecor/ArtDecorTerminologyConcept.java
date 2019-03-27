package artdecor;

/**
 * Representation of an Art-Decor Concept for the Terminology
 */
public class ArtDecorTerminologyConcept {
    private String artdecorConceptId;
    private String conceptFlexibility;
    private String code;
    private String codeSystemName;
    private String codeSystemId;
    private String displayName;
    private String effectiveDate;


    public ArtDecorTerminologyConcept(String artdecorConceptId, String conceptFlexibility, String code, String codeSystemName, String displayName, String effectiveDate, String codeSystemId){
        this.artdecorConceptId = artdecorConceptId;
        this.conceptFlexibility = conceptFlexibility;
        this.code = code;
        this.codeSystemName = codeSystemName;
        this.codeSystemId = codeSystemId;
        this.displayName = displayName;
        this.effectiveDate = effectiveDate;
    }

    public String toXML(){
        return "<terminologyAssociation conceptId=\""+ artdecorConceptId +"\" conceptFlexibility=\""+conceptFlexibility+"\" code=\""+code+"\" codeSystem=\""+ codeSystemId+"\" codeSystemName=\""+codeSystemName+"\" displayName=\""+displayName+"\" effectiveDate=\""+effectiveDate+"\"/>\n";
    }
}
