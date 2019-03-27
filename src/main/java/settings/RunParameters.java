package settings;

import java.io.File;
import java.util.*;

/**
 * Parameters for a run
 */
public class RunParameters {
    private String codebookDirectory;
    private String projectId;
    private String projectPrefix;
    private String experimental;
    private String statusCode;
    private String defaultLanguage;

    private String authorString;
    private String authorsStringFormatted;


    private Map<String, LanguageParameters> languageParametersMap = new HashMap<>();

    public RunParameters(String codebookDirectory, String projectId, String projectPrefix, String experimental, String authorString, String statusCode){
        this.codebookDirectory = codebookDirectory;
        this.projectId = projectId;
        this.experimental = experimental;
        this.statusCode = statusCode;
        this.authorString = authorString;
        formatAuthorString();
        formatProjectPrefix(projectPrefix);
    }

    private void formatProjectPrefix(String projectPrefix){
        if(!projectPrefix.endsWith("-") && !(projectPrefix.equalsIgnoreCase(""))){
            projectPrefix+="-";
        }
        this.projectPrefix = projectPrefix;
    }

    public String getOutputFile(){
        return codebookDirectory.endsWith("\\")||codebookDirectory.endsWith("/")?codebookDirectory+"output.xml":codebookDirectory+File.separator+"output.xml";
    }

    public String getAuthorString(){
        return authorString;
    }

    private void formatAuthorString(){
        if(authorString.length()>0) {
            StringBuilder stringBuilder = new StringBuilder();
            int idIndex = 0;
            String[] splitString = authorString.split("\\n");
            for (String authorString : splitString) {
                String[] splitString2 = authorString.split(";");
                String username = splitString2[0];
                String email = splitString2[1];
                String name = splitString2[2];
//                stringBuilder.append("<author id=\"" + (++idIndex) + "\" username=\"" + username + "\" email=\"" + email + "\" notifier=\"off\">" + name + "</author>");
                stringBuilder.append("<author id=\"").append(++idIndex).append("\" username=\"").append(username).append("\" email=\"").append(email).append("\" notifier=\"off\">").append(name).append("</author>");
            }
            authorsStringFormatted = stringBuilder.toString().trim();
        }
    }

    public String getDefaultLanguage(){
        return defaultLanguage;
    }

    public void addLanguageSettings(String language, String projectDescription, String projectName){
        if(languageParametersMap.size()==0){
            defaultLanguage = Statics.getArtDecorLanguage(language);
        }
        languageParametersMap.put(language, new LanguageParameters(projectDescription, projectName));
    }

    public Set<String> getLanguages(){
        return languageParametersMap.keySet();
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getCodebookDirectory(){
        return codebookDirectory;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectPrefix() {
        return projectPrefix;
    }

    public String getExperimental() {
        return experimental;
    }

    public String getProjectName(String language){
        if(languageParametersMap.containsKey(language)) {
            return languageParametersMap.get(language).getProjectName();
        }
        return "";
    }

    public String getProjectDescription(String language){
        if(languageParametersMap.containsKey(language)) {
            return languageParametersMap.get(language).getProjectDescription();
        }
        return "";
    }

    public String getAuthorsStringFormatted(){
        return authorsStringFormatted;
    }

    public String getProjectReference(){
        return projectPrefix.substring(0, projectPrefix.length()-1);
    }

    class LanguageParameters{
        private String projectName="";
        private String projectDescription="";

        LanguageParameters(String projectDescription, String projectName){
            this.projectDescription = projectDescription;
            this.projectName = projectName;
        }

        String getProjectName() {
            return projectName;
        }

        String getProjectDescription() {
            return projectDescription;
        }
    }
}
