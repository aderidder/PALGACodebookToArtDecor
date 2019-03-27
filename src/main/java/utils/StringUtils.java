package utils;

/**
 * String stuff
 */
class StringUtils {
    /**
     * does some string replacements to ensure the value does not interfere with XML syntax
     * @param value the value to check
     * @return an acceptable xml value
     */
    static String prepareValueForXML(String value){
        value = value.replaceAll("&", "&amp;");
        value = value.replaceAll("<", "&lt;");
        value = value.replaceAll(">", "&gt;");
        value = value.replaceAll("'", "&apos;");
        value = value.replaceAll("\"", "&quot;");
        // replace all whitespace characters with a normal space
        // this due to an a0 character appearing
        value = value.replaceAll("[\\p{Zs}\\s]+", " ");
        return value;
    }
}
