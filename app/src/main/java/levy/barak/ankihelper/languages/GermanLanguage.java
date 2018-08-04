package levy.barak.ankihelper.languages;

import android.app.Fragment;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

import levy.barak.ankihelper.AnkiHelperApp;
import levy.barak.ankihelper.anki.Word;

/**
 * Created by baraklev on 2/10/2018.
 */

public class GermanLanguage extends Language {
    public HashMap<Word.WordCategory, Boolean> wordCategoriesUppercases;

    public GermanLanguage() {
        wordCategoriesUppercases = new HashMap<>();
        wordCategoriesUppercases.put(Word.WordCategory.NOUN, true);
        wordCategoriesUppercases.put(Word.WordCategory.VERB, false);
        wordCategoriesUppercases.put(Word.WordCategory.ADJECTIVE, false);
        wordCategoriesUppercases.put(Word.WordCategory.ADVERB, false);
        wordCategoriesUppercases.put(Word.WordCategory.PREPOSITION, false);
    }

    @Override
    protected void addWordCategoriesTranslations() {
        wordCategoriesTranslations.put(Word.WordCategory.NOUN, "Substantiv");
        wordCategoriesTranslations.put(Word.WordCategory.VERB, "Verb");
        wordCategoriesTranslations.put(Word.WordCategory.ADJECTIVE, "Adjektiv");
        wordCategoriesTranslations.put(Word.WordCategory.ADVERB, "Adverb");
        wordCategoriesTranslations.put(Word.WordCategory.PREPOSITION, "Präposition");
    }

    @Override
    public String getGoogleTranslateLanguageCode() {
        return "de";
    }

    @Override
    public String getWiktionaryLanguageCode() {
        return "de";
    }

    @Override
    public String getSearchableWord() {
        String[] split = AnkiHelperApp.currentWord.secondLanguageWord.split(" ");
        return split[split.length - 1];
    }

    @Override
    public String parseGoogleTranslateWord(String googleTranslateWord, Word.WordCategory wordCategory) {
        // Nothing to do...
        if (wordCategory == null) {
            return googleTranslateWord;
        }

        // Set the german word to either capitalized or lower cased
        if (wordCategoriesUppercases.get(wordCategory)) {
            String[] split = googleTranslateWord.split(" ");

            if (split.length == 2) {
                return split[0] + " " + split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
            }
            else {
                return split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
            }
        }

        return googleTranslateWord.toLowerCase();
    }

    @Override
    public String parseTypedWord(String word) {
        return word;
    }

    @Override
    public void getInformationFromWiktionary(Fragment fragment, Document doc, boolean isFirstToSecondLanguage) {
        Elements ipas = doc.select(".ipa");

        if (ipas.size() == 0) {
            fragment.getActivity().runOnUiThread(() -> Toast.makeText(fragment.getActivity(), "Couldn't find an IPA. Wrong word perhaps?", Toast.LENGTH_LONG).show());
            return;
        }

        AnkiHelperApp.currentWord.ipa = ipas.first().text();

        Element examplesElement = doc.select("[title=Verwendungsbeispielsätze]").first();

        // If there are any examples
        if (examplesElement != null) {
            // Read them
            Element currentNode = examplesElement.nextElementSibling();
            String html = "";

            // Read until we get to the next subject. The next subject has a title attribute
            while (currentNode != null && currentNode.select("[title]").size() == 0) {
                html += currentNode.html();
                currentNode = currentNode.nextElementSibling();
            }

            AnkiHelperApp.currentWord.exampleSentences = html;
        }

        // Change the word by adding the definite article in the following cases:
        // If the word category is null
        // If we're translating from German to English
        // If it's a noun with one word (no definite article)
        if (!isFirstToSecondLanguage && AnkiHelperApp.currentWord.wordCategory == Word.WordCategory.NOUN ||
                isFirstToSecondLanguage && (AnkiHelperApp.currentWord.wordCategory == null ||
                AnkiHelperApp.currentWord.wordCategory == Word.WordCategory.NOUN &&
                AnkiHelperApp.currentWord.secondLanguageWord.split(" ").length == 1)) {
            String fullType = doc.select(".mw-headline").get(1).id();
            char gender = fullType.charAt(fullType.length() - 1); // m, f or n
            String article = gender == 'm' ? "der" :
                             gender == 'f' ? "die" :
                                             "das";

            AnkiHelperApp.currentWord.secondLanguageWord = article + " " + AnkiHelperApp.currentWord.secondLanguageWord;
        }

        // If it's a noun, get it's plural form as well
        if (AnkiHelperApp.currentWord.wordCategory == Word.WordCategory.NOUN) {
            AnkiHelperApp.currentWord.plural =
                    doc.select("[title=Hilfe:Plural]").first().parent().parent().nextElementSibling().child(2).text();
        }
    }
}
