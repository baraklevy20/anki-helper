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

    // Debug
    private long deckId = 1508871866835L;

    public AnkiDatabase(Context context, boolean isDebug) {
        this.context = context;
        this.deckId = isDebug ? 1508871866832L : 1508871866835L;
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
        String decks = FileUtils.getFileContent(context, "dbCollectionDecks.json", "").replace("\'", "\'\'");
        String dconf = FileUtils.getFileContent(context, "dbCollectionDecksConfigurations.json", "").replace("\'", "\'\'");

        return FileUtils.getFileContent(context, "dbInsertCollection.sql")
                .replace("$(configuration)", conf)
                .replace("$(models)", models)
                .replace("$(decks)", decks)
                .replace("$(decksConfigurations)", dconf);
    }

    public void insertCard(int order, long noteId) {
        db.execSQL(String.format("INSERT into cards VALUES ('%d', '%d', '%d', '%d', '%d', '-1','0','0','484332854','0','0','0','0','0','0','0','0','')",
                (long)(Math.random() * Long.MAX_VALUE),
                noteId,
                deckId,
                order,
                System.currentTimeMillis() / 1000
        ));
    }

    public void insertNote(Word word) throws NoSuchAlgorithmException {
        String fullText = getFullText(word).replace("\'", "\'\'");

        db.execSQL(String.format("INSERT into notes VALUES ('%d', '%s', '1366716141610', '%d', '-1', '', '%s', '%s', '%s', '0', '')",
                word.id,
                UUID.randomUUID().toString().substring(0, 10),
                System.currentTimeMillis() / 1000,
                fullText,
                word.germanWord,
                Long.parseLong(sha1(word.germanWord).substring(0, 8), 16)
        ));
    }

    public String getFullText(Word word) {
        String germanWord = word.germanWord.replace(" ", "&nbsp");
        String image = "<img src=\"anki_helper_image_" + word.id + "_0\" /><div>" + word.type.getGermanTranslation() + "</div>";
        String personal = word.type.getGermanTranslation();
        String soundAndIpa = "[sound:anki_helper_sound_" + word.id + "_0]<div>" + word.ipa + "</div>";
        String spelling = "";

        if (word.wordInASentences != null) {
            soundAndIpa += "<div><dl>";

            for (String sentence : word.wordInASentences) {
                soundAndIpa += "<dd>" + sentence + "</dd>";
            }

            soundAndIpa += "</dl></div>";
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
                insertNote(word);
                insertCard(0, word.id);
                insertCard(1, word.id);

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

        zip(fileNames, Environment.getExternalStorageDirectory() + "/AnkiDroid/test.apkg");
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
