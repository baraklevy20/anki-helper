package levy.barak.ankihelper.anki;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.utils.FileUtils;

/**
 * Created by baraklev on 12/24/2017.
 */

public class AnkiDatabase {
    private SQLiteDatabase db;
    private Context context;

    private String deckName;
    private static final long DECK_ID = 1234567890;

    public AnkiDatabase(Context context, String deckName) {
        this.context = context;
        this.deckName = deckName;
    }

    public void generateDatabase() {
        createDatabase();
        createMedia();
    }

    public void createDatabase() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/anki_helper/collection.anki2";
        db = SQLiteDatabase.openOrCreateDatabase(path, null, null);

        String[] queries = FileUtils.getFileContent(context, "dbCreateTables.sql").split(";");

        for(String query : queries) {
            db.execSQL(query);
        }

        // Add a collection (needed, has the deck's name and what not)
        db.execSQL(getInsertCollectionSql());
    }

    public String getInsertCollectionSql() {
        String conf = FileUtils.getFileContent(context, "dbCollectionConfiguration.json", "").replace("\'", "\'\'");
        String models = FileUtils.getFileContent(context, "dbCollectionModels.json", "").replace("\'", "\'\'");
        String decks = FileUtils.getFileContent(context, "dbCollectionDecks.json", "").replace("$(deckName)", deckName).replace("\'", "\'\'");
        String dconf = FileUtils.getFileContent(context, "dbCollectionDecksConfigurations.json", "").replace("\'", "\'\'");

        return FileUtils.getFileContent(context, "dbInsertCollection.sql")
                .replace("$(configuration)", conf)
                .replace("$(models)", models)
                .replace("$(decks)", decks)
                .replace("$(decksConfigurations)", dconf);
    }

    public void insertWordCard(int order, long noteId) {
        db.execSQL(String.format("INSERT into cards VALUES ('%d', '%d', '%d', '%d', '%d', '-1','0','0','484332854','0','0','0','0','0','0','0','0','')",
                (long)(Math.random() * Long.MAX_VALUE),
                noteId,
                DECK_ID,
                order,
                System.currentTimeMillis() / 1000
        ));
    }

    public void insertWordNote(Word word) throws NoSuchAlgorithmException {
        String fullText = getFullText(word).replace("\'", "\'\'");

        db.execSQL(String.format("INSERT into notes VALUES ('%d', '%s', '1366716141610', '%d', '-1', '', '%s', '%s', '%s', '0', '')",
                word.id,
                UUID.randomUUID().toString().substring(0, 10),
                System.currentTimeMillis() / 1000,
                fullText,
                word.secondLanguageWord,
                Long.parseLong(sha1(word.secondLanguageWord).substring(0, 8), 16)
        ));
    }

    public void insertSentenceCard(int order, long noteId) {
        db.execSQL(String.format("INSERT into cards VALUES ('%d', '%d', '%d', '%d', '%d', '-1','0','0','484332854','0','0','0','0','0','0','0','0','')",
                (long)(Math.random() * Long.MAX_VALUE),
                noteId,
                DECK_ID,
                order,
                System.currentTimeMillis() / 1000
        ));
    }

    public void insertSentenceNote(Sentence sentence, int index) throws NoSuchAlgorithmException {
        String fullText = getFullText(sentence, index).replace("\'", "\'\'");
        String blankedSentence = sentence.getBlankedSentence(index);

        db.execSQL(String.format("INSERT into notes VALUES ('%d', '%s', '1366982516457', '%d', '-1', '', '%s', '%s', '%s', '0', '')",
                sentence.id,
                UUID.randomUUID().toString().substring(0, 10),
                System.currentTimeMillis() / 1000,
                fullText,
                blankedSentence,
                Long.parseLong(sha1(blankedSentence).substring(0, 8), 16)
        ));
    }

    public String getFullText(Sentence sentence, int index) {
        final String separator = (char)0x1F + "";

        String blankedSentence = sentence.getBlankedSentence(index);
        String image = "";
        String explanationOfTheWord = "";
        String germanWord = sentence.words[index];
        String fullSentence = sentence.getFullSentence();
        String additionalInformation = "";
        String makeTwoCards = "y"; // Must be y to have 2 cards
        String spelling = "";
        String copyPasteArea = "";

        return String.join(separator, new String[] {blankedSentence, image, explanationOfTheWord,
                    germanWord, fullSentence, additionalInformation, makeTwoCards, spelling, copyPasteArea});
    }

    public String getFullText(Word word) {
        String germanWord = (word.secondLanguageWord + (word.plural == null ? "" : "<br>" + word.plural)).replace(" ", "&nbsp");
        String categoryTranslation = AnkiHelperApplication.language.wordCategoriesTranslations.get(word.wordCategory);
        String image = "<img src=\"anki_helper_image_" + word.id + "_0\" />" +
                (word.wordCategory == null ? "" : "<div>" + (categoryTranslation == null ? word.wordCategory : categoryTranslation) + "</div>") +
                "<div>" + word.additionalInformation + "</div>";
        String personal = ""; // not used
        String soundAndIpa = "[sound:anki_helper_sound_" + word.id + "_0]<div>" + word.ipa + "</div>";
        String spelling = ""; // not used

        if (word.exampleSentences != null) {
            soundAndIpa += "<div>" + word.exampleSentences + "</div>";
        }

        char separator = 0x1F;
        return germanWord + separator + image + separator + personal + separator + soundAndIpa + separator + spelling;
    }

    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public void createMedia() {
        // Create media
        String ankiHelperPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/anki_helper";
        String[] medias = new String[AnkiHelperApplication.allWords.size() * 2];

        // Add notes and cards
        for (int i = 0; i < AnkiHelperApplication.allWords.size(); i++) {
            try {
                Word word = AnkiHelperApplication.allWords.get(i);
                insertWordNote(word);
                insertWordCard(0, word.id);
                insertWordCard(1, word.id);

                try {
                    // Add the medias to the array
                    medias[2 * i] = "anki_helper_image_" + word.id + "_0";
                    medias[2 * i + 1] = "anki_helper_sound_" + word.id + "_0";

                    // Convert the files
                    Files.move(Paths.get(ankiHelperPath + "/anki_helper_image_" + word.id + "_0"), Paths.get(ankiHelperPath + "/" + (2 * i)));
                    Files.move(Paths.get(ankiHelperPath + "/anki_helper_sound_" + word.id + "_0"), Paths.get(ankiHelperPath + "/" + (2 * i + 1)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < AnkiHelperApplication.allSentences.size(); i++) {
            try {
                Sentence sentence = AnkiHelperApplication.allSentences.get(i);
                insertSentenceNote(sentence, sentence.getFirstBlank());
                insertSentenceCard(0, sentence.id);
                insertSentenceCard(1, sentence.id);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        // Save the media JSON
        for (int i = 0; i < medias.length; i++) {
            medias[i] = String.format("\"%d\": \"%s\"", i, medias[i]);
        }

        String json = "{" + String.join(", ", medias) + "}";
        File jsonFile = new File(ankiHelperPath + "/media");
        try {
            PrintWriter pw = new PrintWriter(jsonFile);
            pw.print(json);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create ZIP
        String[] fileNames = new String[AnkiHelperApplication.allWords.size() * 2 + 2];
        for (int i = 0; i < AnkiHelperApplication.allWords.size() * 2; i++) {
            fileNames[i] = ankiHelperPath + "/" + i;
        }

        fileNames[AnkiHelperApplication.allWords.size() * 2] = ankiHelperPath + "/media";
        fileNames[AnkiHelperApplication.allWords.size() * 2 + 1] = ankiHelperPath + "/collection.anki2";

        zip(fileNames, Environment.getExternalStorageDirectory() + "/AnkiDroid/AnkiHelper.apkg");
    }

    public void zip(String[] _files, String zipFileName) {
        try {
            final int BUFFER = 1024;
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
