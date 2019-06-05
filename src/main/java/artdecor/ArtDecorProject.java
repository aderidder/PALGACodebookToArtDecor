package artdecor;

import settings.RunParameters;
import settings.Statics;

import java.util.Set;

/**
 * Representation of an Art-Decor Project
 */
public class ArtDecorProject {
    private RunParameters runParameters;

    public ArtDecorProject(RunParameters runParameters){
        this.runParameters = runParameters;
    }

    public String toXML(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<project id=\""+ runParameters.getProjectId()+"\" prefix=\""+ runParameters.getProjectPrefix()+"\" experimental=\""+ runParameters.getExperimental()+"\" defaultLanguage=\""+ Statics.getArtDecorLanguage(runParameters.getDefaultLanguage())+"\">\n");
        Set<String> languages = runParameters.getLanguages();

        for(String language:languages){
            String languageArtDecor = Statics.getArtDecorLanguage(language);
            stringBuilder.append("<name language=\""+ languageArtDecor+"\">"+ runParameters.getProjectName(language)+"</name>\n");
        }

        for(String language:languages){
            String languageArtDecor = Statics.getArtDecorLanguage(language);
            stringBuilder.append("<desc language=\""+ languageArtDecor+"\">"+ runParameters.getProjectDescription(language)+"</desc>\n");
        }

        // copyright by and years should probably become parameters as well...
        stringBuilder.append(
                "<copyright by=\"NKI and VUmc \" years=\"2016 2017 2018 2019\" type=\"author\"/>\n" +
                        runParameters.getAuthorsStringFormatted()+"\n"+
                        "<reference url=\"http://decor.nictiz.nl/pub/"+ runParameters.getProjectReference()+"/\"/>\n" +
                        "<defaultElementNamespace ns=\"hl7:\"/>\n" +
                        "</project>\n");
        return stringBuilder.toString();
    }
}
