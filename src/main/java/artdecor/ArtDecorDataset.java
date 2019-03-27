package artdecor;

import settings.Statics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Representation of an Art-Decor dataset
 */
public class ArtDecorDataset {
    private String effectiveDate;
    private String statusCode;
    private int versionLabel;

    private String artdecorDatasetId;

    private List<LanguageDataset> languageDatasetList = new ArrayList<>();

    private Map<String, ArtDecorConcept> artDecorConcepMap = new TreeMap<>();
    private List<ArtDecorConcept> topArtDecorConceptList = new ArrayList<>();

    public ArtDecorDataset(String artdecorDatasetId, String effectiveDate, int versionLabel, String statusCode){
        this.artdecorDatasetId = artdecorDatasetId;
        this.effectiveDate = effectiveDate;
        this.versionLabel = versionLabel;
        this.statusCode = statusCode;
    }

    public void addLanguageParameter(String language, String name, String description){
        languageDatasetList.add(new LanguageDataset(language, name, description));
    }

    public void addArtDecorConcept(ArtDecorConcept artDecorConcept){
        artDecorConcepMap.put(artDecorConcept.getConceptId(), artDecorConcept);
    }

    public void connectConcepts(){
        for(ArtDecorConcept artDecorConcept:artDecorConcepMap.values()){
            String parent = artDecorConcept.getParent();
            if(artDecorConcepMap.containsKey(parent)) {
                ArtDecorConcept parentConcept = artDecorConcepMap.get(parent);
                parentConcept.addChild(artDecorConcept);
            }
            else{
                topArtDecorConceptList.add(artDecorConcept);
            }
        }
    }

    public String toXML() throws Exception{
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<dataset id=\"" + artdecorDatasetId + "\" effectiveDate=\"" + effectiveDate + "\" statusCode=\"" + statusCode + "\" versionLabel=\"" + versionLabel + "\">\n");
            for (LanguageDataset languageDataset : languageDatasetList) {
                stringBuilder.append("<name language=\"" + languageDataset.language + "\">" + languageDataset.datasetName + "</name>\n");
            }

            for (LanguageDataset languageDataset : languageDatasetList) {
                stringBuilder.append("<desc language=\"" + languageDataset.language + "\">" + languageDataset.datasetDescription + "</desc>\n");
            }

            for (ArtDecorConcept artDecorConcept : topArtDecorConceptList) {
                stringBuilder.append(artDecorConcept.toXML());
            }

            stringBuilder.append("</dataset>\n");
            return stringBuilder.toString();
        } catch(Exception e){
            throw new Exception("codebook version: "+versionLabel+"; "+e.getMessage());
        }
    }

    private class LanguageDataset{
        private String language;
        private String datasetName;
        private String datasetDescription;

        LanguageDataset(String language, String datasetName, String datasetDescription){
            this.language = Statics.getArtDecorLanguage(language);
            this.datasetName = datasetName;
            this.datasetDescription = datasetDescription;
        }
    }
}
