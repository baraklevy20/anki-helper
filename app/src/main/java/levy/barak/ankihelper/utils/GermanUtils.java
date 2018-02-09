package levy.barak.ankihelper.utils;

/**
 * Created by baraklev on 2/3/2018.
 */

public class GermanUtils {
    public enum WordCategory {
        NOUN("Substantiv", true),
        VERB("Verb", false),
        ADJECTIVE("Adjektiv", false),
        ADVERB("Adverb", false);

        private String germanTranslation;
        private boolean isUppercase; // Will be used in accessing Wiktionary

        WordCategory(String germanTranslation, boolean isUppercase) {
            this.germanTranslation = germanTranslation;
            this.isUppercase = isUppercase;
        }

        public String getGermanTranslation() {
            return germanTranslation;
        }

        public boolean isUppercase() {
            return isUppercase;
        }
    }

}
