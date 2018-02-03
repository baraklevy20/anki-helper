package levy.barak.ankihelper.utils;

/**
 * Created by baraklev on 2/3/2018.
 */

public class GermanUtils {
    public static String translateTypes(String type) {
        switch (type.toLowerCase()) {
            case "noun":
                return "Substantiv";
            case "verb":
                return "Verb";
            case "adjective":
                return "Adjektiv";
            case "adverb":
                return "Adverb";
            default:
                return type;
        }
    }
}
