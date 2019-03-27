package artdecor;

/**
 * Representation of an Art-Decor Value Set for the Terminology
 */
public class ArtDecorTerminologyValueSet {
    private String artdecorConceptListId;
    private String artdecorValueSetId;
    private String flexibilityDate;
    private String effectiveDate;

    public ArtDecorTerminologyValueSet(String artdecorConceptListId, String artdecorValueSetId, String flexibilityDate, String effectiveDate){
        this.artdecorConceptListId = artdecorConceptListId;
        this.artdecorValueSetId = artdecorValueSetId;
        this.flexibilityDate = flexibilityDate;
        this.effectiveDate = effectiveDate;
    }

    public String toXML(){
        return "<terminologyAssociation conceptId=\""+artdecorConceptListId+"\" valueSet=\""+artdecorValueSetId+"\" flexibility=\""+flexibilityDate+"\" effectiveDate=\""+effectiveDate+"\"/>\n";
    }
}
