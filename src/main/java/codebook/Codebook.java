package codebook;

import artdecor.ArtDecorDataset;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import settings.RunParameters;
import settings.Statics;
import utils.ExcelUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class for the Excel Codebook
 */
class Codebook {
    private static final Logger logger = LogManager.getLogger(Codebook.class.getName());
    private static final SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
    private Date effectiveDateAsDate;
    private String effectiveDate;

    private String datasetVersionLabel="";
    private Map<String, CodebookLanguageParameters> codebookLanguageParametersMap = new HashMap<>();

    private RunParameters runParameters;
    private List<String> headerList;
    private Map<String, Concept> conceptMap = new LinkedHashMap<>();


    private Codebook(RunParameters runParameters){
        this.runParameters = runParameters;
    }

    static Codebook readExcel(Path path, RunParameters runParameters) throws IOException, InvalidFormatException {
        Codebook codebook = new Codebook(runParameters);
        try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
            parseInfoSheet(codebook, workbook, runParameters);
            parseMainSheet(codebook, workbook);
        }
        return codebook;
    }

    private static void parseInfoSheet(Codebook codebook, Workbook workbook, RunParameters runParameters){
        Sheet sheet = workbook.getSheet("Info");
        if(sheet==null) throw new RuntimeException("Info sheet missing...");
        Map<String, String> valueMap = createValueMap(sheet);
        codebook.datasetVersionLabel = valueMap.get("version");
        codebook.setEffectiveDate(valueMap);

        Set<String> languageList = runParameters.getLanguages();
        for(String language:languageList) {
            String datasetDescription = valueMap.get("DatasetDescription_"+language);
            String datasetName  = valueMap.get("DatasetDescription_"+language);
            codebook.addLanguageSetting(language, datasetDescription, datasetName);
        }
    }

    private static Map<String, String> createValueMap(Sheet sheet){
        Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int lastRowNr = sheet.getLastRowNum();
        for(int i=0; i<=lastRowNr; i++){
            Row row = sheet.getRow(i);
            // if the row exists, add the information in the row to our excelCodebook
            if(row!=null) {
                String key = ExcelUtils.getCellValue(row, 0);
                String value = ExcelUtils.getCellValue(row, 1);
                valueMap.put(key, value);
            }
        }
        return valueMap;
    }

    private static void parseMainSheet(Codebook codebook, Workbook workbook){
        // head to the main sheet of the codebook
        Sheet sheet = workbook.getSheet("Codebook");

        // first row contains the header.
        Row row = sheet.getRow(0);
        codebook.addHeader(row);

        // iterate over the rest of the rows
        int lastRowNr = sheet.getLastRowNum();
        for(int i=1; i<=lastRowNr; i++){
            row = sheet.getRow(i);
            // if the row exists, add the information in the row to our excelCodebook
            if(row!=null) {
                codebook.addData(workbook, row);
            }
        }
    }

    private void setEffectiveDate(Map<String, String> valueMap){
        if(valueMap.containsKey("effectiveDate")) {
            try {
                effectiveDateAsDate = parseFormat.parse(valueMap.get("effectivedate"));
                effectiveDate = outFormat.format(effectiveDateAsDate);

            } catch (ParseException e) {
                logger.log(Level.ERROR, "codebook version: {}; Severe Error: The effective date is not in the correct format {}", datasetVersionLabel, valueMap.get("effectivedate"));
                try{
                    effectiveDateAsDate = parseFormat.parse("1900-01-01");
                    effectiveDate = outFormat.format(effectiveDateAsDate);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        }
        else{
            logger.log(Level.WARN, "codebook version: {}; Warning: The Effectivedate is not available in the INFO sheet (yyyy-mm-dd). Setting it to today... ", datasetVersionLabel);
            effectiveDateAsDate = new Date();
            effectiveDate = outFormat.format(effectiveDateAsDate);
        }
    }

    Collection<Concept> getAllConcepts(){
        return conceptMap.values();
    }

    private void addLanguageSetting(String language, String datasetDescription, String datasetName){
        CodebookLanguageParameters codebookLanguageParameters = new CodebookLanguageParameters(datasetDescription, datasetName);
        codebookLanguageParametersMap.put(language, codebookLanguageParameters);
    }


    private void addHeader(Row row){
        headerList = ExcelUtils.getRowAsList(row);
    }

    private boolean isValidEntry(String id, String codesystem, String code, String description_code){
        boolean isValid=true;
        if(conceptMap.containsKey(id)){
            logger.log(Level.ERROR, "codebook version: {}; Concept: The identifier in the codebook must be unique {}", datasetVersionLabel, id);
            isValid = false;
        }
        if(Statics.mayBeTypo(codesystem)){
            logger.log(Level.WARN, "codebook version: {}; Concept: Codesystem found: {} for {}. Did you mean {}?", datasetVersionLabel, codesystem, id, Statics.getTypoValue(codesystem));
        }
        if(code.equalsIgnoreCase("")){
            logger.log(Level.ERROR, "codebook version: {}; Concept: Mandatory code missing for concept {}", datasetVersionLabel, id);
            isValid = false;
        }
        if(codesystem.equalsIgnoreCase("")){
            logger.log(Level.ERROR, "codebook version: {}; Concept: Mandatory codesystem missing for concept {}", datasetVersionLabel, id);
            isValid = false;
        }
        if(description_code.equalsIgnoreCase("")){
            logger.log(Level.ERROR, "codebook version: {}; Concept: Mandatory code description missing for concept {}", datasetVersionLabel, id);
            isValid = false;
        }
        return isValid;
    }

    private void addData(Workbook workbook, Row row){
        // create a codebook item for the row and store it in a map
        String id = ExcelUtils.getValue(row, "id", headerList);
        String codesystem = ExcelUtils.getValue(row, "codesystem", headerList);
        String code = ExcelUtils.getValue(row, "code", headerList);
        String description_code = ExcelUtils.getValue(row, "description_code", headerList);
        String codelist_ref = ExcelUtils.getValue(row, "codelist_ref", headerList);
        String properties =  ExcelUtils.getValue(row, "properties", headerList);
        String parent = ExcelUtils.getValue(row, "parent", headerList);
        String data_type = ExcelUtils.getValue(row, "data_type", headerList);

        // If the concept itself is invalid, we basically stop for this entry. This also implies that any errors made
        // in the concept's codelist will not be shown until the concept itself is fixed.
        if(isValidEntry(id, codesystem, code, description_code)) {
            Concept concept = new Concept(id, codesystem, code, description_code, properties, codelist_ref, parent, data_type, effectiveDate, datasetVersionLabel, runParameters.getStatusCode());

            Set<String> languages = runParameters.getLanguages();
            for (String language : languages) {
                String languageDescription = ExcelUtils.getValue(row, "description_" + language, headerList);
                concept.addLanguageConcept(language, languageDescription);
            }

            conceptMap.put(id, concept);

            // if the codebook item has a codelist add it as well
            if (!codelist_ref.equalsIgnoreCase("")) {
                addCodeList(workbook, concept, codelist_ref);
            }
        }
    }

    private void addCodeList(Workbook workbook, Concept concept, String codelist_ref) {
        try {
            Sheet sheet = workbook.getSheet(codelist_ref);

            // retrieve the header of the sheet
            Row row = sheet.getRow(0);
            List<String> codelistHeaderList = ExcelUtils.getRowAsList(row);

            // parse the remaining rows
            int lastRowNr = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNr; i++) {
                row = sheet.getRow(i);
                // if the row exists, add the information in the row to our excelCodebook
                if (row != null && !ExcelUtils.isEmptyRow(row)) {
                    concept.addCodeListEntry(row, codelistHeaderList, runParameters.getLanguages(), codelist_ref);
//                    addCodeListEntry(concept, row, codelistHeaderList);
                }
            }
        } catch (NullPointerException e){
            logger.log(Level.ERROR, "codebook version: {}; Severe Error: Issue adding codelist, ref = {}", datasetVersionLabel, codelist_ref);
        }
    }

//    private void addCodeListEntry(Concept concept, Row row, List<String> codelistHeaderList){
//        String codesystem = ExcelUtils.getValue(row, "codesystem", codelistHeaderList);
//        String code = ExcelUtils.getValue(row, "code", codelistHeaderList);
//        String description_code = ExcelUtils.getValue(row, "description_code", codelistHeaderList);


//        // in some cases the codebook contains a blank line, which we do not want to add to our codelists
//        // first check whether one of the mandatory fields is empty
//        if(code.equalsIgnoreCase("") || codesystem.equalsIgnoreCase("") || description_code.equalsIgnoreCase("")) {
//            // next check one of the mandatory fields is not empty; if so give an error
//            if(!code.equalsIgnoreCase("") || !codesystem.equalsIgnoreCase("") || !description_code.equalsIgnoreCase("")){
//                logger.log(Level.ERROR, "Something wrong with a codelist entry for concept"+concept.getId());
//            }
//        }
//        else{
//            ConceptOption conceptOption = new ConceptOption(codesystem, code, description_code);
//
//            Set<String> languages = runParameters.getLanguages();
//            for (String language : languages) {
//                String languageDescription = ExcelUtils.getValue(row, "description_" + language, codelistHeaderList);
//                String languageValue = ExcelUtils.getValue(row, "value_" + language, codelistHeaderList);
//                LanguageConceptOptions languageConceptOptions = new LanguageConceptOptions(language, languageValue, languageDescription);
//                conceptOption.addLanguageConceptOptions(language, languageConceptOptions);
//            }
//            concept.addConceptOption(conceptOption);
//        }
//
//    }

    int getDatasetVersionLabel() {
        return Integer.parseInt(datasetVersionLabel);
    }

    ArtDecorDataset createArtDecorDataset(String artdecorDatasetId){
        ArtDecorDataset artDecorDataset = new ArtDecorDataset(artdecorDatasetId, effectiveDate, Integer.parseInt(datasetVersionLabel), runParameters.getStatusCode());
        for(Map.Entry<String, CodebookLanguageParameters> entrySet:codebookLanguageParametersMap.entrySet()) {
            CodebookLanguageParameters codebookLanguageParameters = entrySet.getValue();
            artDecorDataset.addLanguageParameter(entrySet.getKey(), codebookLanguageParameters.datasetName, codebookLanguageParameters.datasetDescription);
        }
        return artDecorDataset;
    }

    Date getEffectiveDateAsDate() {
        return effectiveDateAsDate;
    }

    private class CodebookLanguageParameters{
        private String datasetDescription="";
        private String datasetName="";

        CodebookLanguageParameters(String datasetDescription, String datasetName){
            this.datasetDescription = datasetDescription;
            this.datasetName = datasetName;
        }
    }

}
