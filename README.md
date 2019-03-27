# The Excel Codebook to Art-Decor program
This program aims to facilitate the translation of an Excel codebook to an Art-Decor XML input file.  

## The Excel codebook format
The Excel codebook needs to be in a certain format.
### INFO worksheet 
The first worksheet needs to be called "INFO". It has to contain:

|     |     |
| --- | --- |
| Version | version number |
| Effectivedate | date |
| DatasetName_\<en/nl> | name of the dataset in the language |
| DatasetDescription_\<en/nl> | description of the dataset in the language |

Version should currently be an **integer**
Effectivedate should be in the **yyyy-MM-dd** format, e.g. 2017-01-17
If both English and Dutch are to be supported, a description and name have to be given in both languages 

#### Example
|     |     |
| --- | --- |
| Version | 33 |
| DatasetName_nl | PALGA colonbiopt protocol |
| DatasetDescription_nl | Dit is versie 33 van het PALGA Colonbiopt Protocol | 
| DatasetName_en | PALGA colonbiopsy protocol |
| DatasetDescription_en | Version 33 of the PALGA colonbiopsy protocol | 
| Effectivedate | 2017-10-17 |


### CODEBOOK worksheet
The second worksheet needs to be called "CODEBOOK". It has to contain the follow header:

|     |     |     |     |     |     |     |     |     |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| id | description_<en/nl> | codesystem | code | description_code | codelist_ref | data_type | properties | parent |

The meaning of the column names:

|     |     |
| --- | --- |
| id | the unique identifier of the concept |
| description_\<en/nl> | description of the concept in the language |
| codesystem | codesystem to which the concept belongs, e.g. SNOMED |
| code | a code for the concept, e.g. a SNOMED code | 
| description_code | a description of the code |
| codelist_ref | a reference to a different worksheet, which contains the options for this concept |
| data_type | datatype for this concept. Currently valid options are “code”; everything else becomes “string” |
| properties | custom properties in Art-Decor, e.g. {PALGA_COLNAME=Aantalinzendingen}{SOME_OTHER_PROPERTY=SomeValue} |
| parent | id of a parent in the Art-Decor tree. Leave empty if concept has no parent |

#### Example
id | description_nl | description_en | codesystem | code | description_code | codelist_ref | data_type | properties | parent
--- | --- | --- | --- | --- | --- | --- | --- | --- | --- 
Aantalinzendingen | Aantal inzendingen | number of specimens | s2nki-codesystem-0 | 1 | number of specimens |  | text | {PALGA_COLNAME=Aantalinzendingen} | 
Afstandtotbasalesnijvlak | Afstand tot basale snijvlak | distance to basal resection margin | s2nki-codesystem-0 | 3 | distance to basal resection margin | Afstandtotbasalesnijvlak | code | {PALGA_COLNAME=Afstandtotbasalesnijvlak} |
Snijvlak | Snijvlak | Surgical margin finding (finding) | SNOMED CT | 395536008 | Surgical margin finding (finding) | Snijvlak | code | {PALGA_COLNAME=Snijvlak} |   

### Reference worksheets
The remaining worksheets contain the possible values for concepts. The name of the worksheet has to match the name specified in the *codelist_ref* column of the CODEBOOK worksheet. Each worksheet contains the following header:

|     |     |     |     |     |     |     |
| --- | --- | --- | --- | --- | --- | --- |
| value_\<en/nl> | description_\<en/nl> | codesystem | code | description_code |

The meaning of the column names:

|     |     |
| --- | --- |
| value_\<en/nl> | value in a specific language, for future use, this could perhaps become the local code | 
| description_\<en/nl> | description in a specific language, currently used to set the preferred value |
| codesystem | codesystem to which can be recoded |
| code | the code of this description in the codesystem |
| description_code |  the description of the code in the codesystem |

As specified above, the **description** is used to set the preferred value. For the PALGA translations, this implies that the description is the value that is translated from!

#### Example
value_nl | description_nl | value_en | description_en | codesystem | code | description_code 
--- | --- | --- | --- | --- | --- | --- 
niet aanwezig | niet aanwezig | Venous (large vessel)/lymphatic (small vessel) invasion by tumor absent (finding) | Venous (large vessel)/lymphatic (small vessel) invasion by tumor absent (finding) | SNOMED CT | 395552006 | Venous (large vessel)/lymphatic (small vessel) invasion by tumor absent (finding) | 
aanwezig | aanwezig | Venous (large vessel)/lymphatic (small vessel) invasion by tumor present (finding) | Venous (large vessel)/lymphatic (small vessel) invasion by tumor present (finding) | SNOMED CT | 395553001 | Venous (large vessel)/lymphatic (small vessel) invasion by tumor present (finding) | 
suspect | suspect | Venous (large vessel)/lymphatic (small vessel) invasion by tumor indeterminate (finding) | Venous (large vessel)/lymphatic (small vessel) invasion by tumor indeterminate (finding) | SNOMED CT | 395554007 | Venous (large vessel)/lymphatic (small vessel) invasion by tumor indeterminate (finding) | 

## Creating an executable jar
You can use maven to create an executable jar file, using mvn package. The jar is placed in the target directory and can be run using java -jar <generated_jar_file>

## Generating an XML for Art-Decor
When you start the program's Wizard, it will request the following parameters:

|     |     |
| --- | --- |
| Codebook directory | directory which contains one or more Excel codebooks |
| Project Id |  an identifier provided by Art-Decor, something like 1.2.3.5.6.7 |
| Project Prefix | a prefix provided by Art-Decor, something like s2nki |
| Select Languages | languages that will be in the codebook |
| Experimental | whether the codebook should have the experimental flag |
| Status code | whether the items in the codebook will have the draft or final status |
| Authors | list of the authors involved in the codebook. The format is: art-decor userid; email address; name of the user | 

After clicking the "next" button, details will have to be provided about the Project in the languages specified:

|     |     |
| --- | --- |
| Project name in \<en/nl> | Name of the project in the language |
| Project description \<en/nl> |  Description of the project in the language |

After clicking the "next" button, the program explains where the output will be generated. After clicking the "finish" button, the program starts the transformation. 

## The XML relationship components
For our purpose there are a couple of relevant sections in the XML. Without getting into too much detail, here are some essentials:

* Datasets
    * Dataset: one codebook version
        * Concept: contains an identifier, contains description and the concept's EffectiveDate
            * Value Domain: code/text
                * Concept List: reference to a codelist, via the terminology association 

* Terminology
    * Terminology Association: links the Concept List to a Value Set, using a value set id as well as the EffectiveDate
    * Terminology Association: links the Concept's ConceptId to the ontology (Codesystem / Codesystem description etc)
    * Value Set: keeps the same identifier. Multiple versions of a value set can exist by setting the old one to deprecated and creating a new value set with a different EffectiveDate

## Multiple versions of a codebook
The one complicated issue in this program is supporting multiple versions of a codebook. After a discussion with the Art-Decor team, it was suggested that when a new codebook version needs to be published, it would be best to provided a completely new XML file containing **all** versions of a codebook, to make sure the identifiers are correctly linked. 
Inheritance itself is available in two flavours: full inheritance, through the "ref" tag, and partial inheritance, through the "spec" tag. 
 
When two codebooks are transformed, the lowest version's concepts will all be new. 
When the second codebook is parsed, the program does the following:

* First we check whether the domain type has remained the same (e.g. something was a CODE and now suddenly is a STRING):
    * if the type changed: generate an error
    * otherwise: check whether the type is CODE
        * if yes: check the concept's status (NEW, SAME, CHANGED) and the concept list's status (NEW, SAME, CHANGED)
            * generate the xml based on these options
        * if no: there's no list, so just check the concept's status (NEW, SAME, CHANGED)
            * generate the xml based on these options

The concept's status in combination with the concept list's status leads to a number of options, some of which are valid for the codebook and some of which are invalid:

Concept | ConceptList | Valid | Explanation
--- | --- | --- | ---
NEW | NEW | Y | a new concept with a new list
NEW | SAME | Y | a new concept can share a list with another concept
NEW | CHANGED | N | a new concept will always refer to a new list. Actually... check this. What would happen if in v1 ConceptA --> List1 A, B and in v2 ConceptB created --> List1 A, B, C? I think this is possible.
SAME | NEW | N | someone changed the concept from e.g. a string to a code datatype. Check this.. How about ConceptA --> List1 A, B and in v2 ConceptA List2 A, C? Doesn't really make sense... makes more sense to keep the same list and just alter it. 
SAME | SAME | Y | everything stays the same, REF
SAME | CHANGED | Y | concept stayed the some but the options were changed, SPEC
CHANGED | NEW | N | someone changed the concept from e.g. a string to a code datatype
CHANGED | SAME | Y | concept was changed but the options stayed the same, SPEC
CHANGED | CHANGED | Y | concept was changed and options were changed

## About
Codebook to Art-Decor was designed and created by **Sander de Ridder** (NKI 2017; VUmc 2018/2019)<br>
Testers & Consultants: Maarten Ligtvoet (Nictiz), Alexander Henket (Nictiz), Elze de Groot (Nictiz), Jeroen Belien (VUmc)<br>
This project was sponsored by MLDS project OPSLAG and KWF project TraIT2Health-RI (WP: Registry-in-a-Box)<br>
 
CodebookToArtDecor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

CodebookToArtDecor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
