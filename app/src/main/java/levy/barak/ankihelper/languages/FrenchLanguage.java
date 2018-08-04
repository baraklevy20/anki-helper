package levy.barak.ankihelper.languages;

import android.app.Fragment;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import levy.barak.ankihelper.AnkiHelperApp;
import levy.barak.ankihelper.anki.Word;

public class FrenchLanguage extends Language {
    @Override
    protected void addWordCategoriesTranslations() {
        wordCategoriesTranslations.put(Word.WordCategory.NOUN, "Nom");
        wordCategoriesTranslations.put(Word.WordCategory.VERB, "Verbe");
        wordCategoriesTranslations.put(Word.WordCategory.ADJECTIVE, "Adjectif");
        wordCategoriesTranslations.put(Word.WordCategory.ADVERB, "Adverbe");
        wordCategoriesTranslations.put(Word.WordCategory.PREPOSITION, "préposition");
    }

    @Override
    public String getGoogleTranslateLanguageCode() {
        return "fr";
    }

    @Override
    public String getWiktionaryLanguageCode() {
        return "fr";
    }

    @Override
    public String getSearchableWord() {
        String[] split = AnkiHelperApp.currentWord.secondLanguageWord.split(" ");
        return split[split.length - 1].toLowerCase();
    }

    @Override
    public String parseGoogleTranslateWord(String googleTranslateWord, Word.WordCategory wordCategory) {
        return googleTranslateWord.toLowerCase();
    }

    @Override
    public String parseTypedWord(String word) {
        return word.toLowerCase();
    }

    @Override
    public void getInformationFromWiktionary(Fragment fragment, Document doc, boolean isFirstToSecondLanguage) {
        // Get the IPA
        Elements ipas = doc.select(".API");

        if (ipas.size() == 0) {
            fragment.getActivity().runOnUiThread(() -> Toast.makeText(fragment.getActivity(), "Couldn't find an IPA. Wrong word perhaps?", Toast.LENGTH_LONG).show());
            return;
        }

        String ipa = ipas.first().text();
        // Remove \ and \ that surround the ipa
        ipa = ipa.substring(1, ipa.length() - 1);
        AnkiHelperApp.currentWord.ipa = ipa;

        // Get the example sentences
        Element differentDefinitionsElement = doc.select("ol").first();
        AnkiHelperApp.currentWord.exampleSentences = "<ul>";
        for (Element definitionElement : differentDefinitionsElement.children()) {
            Element sentencesPerDefinition = definitionElement.select("ul").first();

            if (sentencesPerDefinition != null) {
                for (Element sentence : sentencesPerDefinition.children()) {
                    AnkiHelperApp.currentWord.exampleSentences += "<li>" + sentence.child(0).text() + "</li>";
                }
            }
        }

        AnkiHelperApp.currentWord.exampleSentences += "</ul>";

        // Change the word by adding the definite article in the following cases:
        // If the word category is null
        // If we're translating from French to English
        // If it's a noun with one word that doesn't start with l' (no definite article)
        if (!isFirstToSecondLanguage && AnkiHelperApp.currentWord.wordCategory == Word.WordCategory.NOUN ||
                isFirstToSecondLanguage && (AnkiHelperApp.currentWord.wordCategory == null ||
                AnkiHelperApp.currentWord.wordCategory == Word.WordCategory.NOUN &&
                AnkiHelperApp.currentWord.secondLanguageWord.split(" ").length == 1 &&
                !AnkiHelperApp.currentWord.secondLanguageWord.startsWith("l'"))) {
            String gender = doc.select(".ligne-de-forme").first().text();

            String article = gender.startsWith("masculin") ? "le" : "la";
            AnkiHelperApp.currentWord.secondLanguageWord = article + " " + AnkiHelperApp.currentWord.secondLanguageWord;
        }

        // If it's a noun, get it's plural form as well
        if (AnkiHelperApp.currentWord.wordCategory == Word.WordCategory.NOUN) {
            AnkiHelperApp.currentWord.plural = "les " + doc.select(".flextable.flextable-fr-mfsp").first().child(0).child(1).select("td").get(1).child(0).text();
        }
    }
}
