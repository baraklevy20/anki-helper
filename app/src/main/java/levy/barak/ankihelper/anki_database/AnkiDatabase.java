package levy.barak.ankihelper.anki_database;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import levy.barak.ankihelper.GoogleTranslateActivity;
import levy.barak.ankihelper.utils.FileUtils;

import static android.content.Context.MODE_PRIVATE;
import static levy.barak.ankihelper.TranslateActivity.getImagePath;

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

    public void generateDatabase(int currentWord) {
        createDatabase();
        createMedia(currentWord);
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

    public long insertNote(int index) throws NoSuchAlgorithmException {
        long noteId = (long)(Math.random() * Long.MAX_VALUE);
        String fullText = getFullText(index, noteId);
        String germanWord = getGermanWord(index);

        db.execSQL(String.format("INSERT into notes VALUES ('%d', '%s', '1366716141610', '%d', '-1', '', '%s', '%s', '%s', '0', '')",
                noteId,
                UUID.randomUUID().toString().substring(0, 10),
                System.currentTimeMillis() / 1000,
                fullText,
                germanWord,
                Long.parseLong(sha1(germanWord).substring(0, 8), 16)
        ));

        return noteId;
    }

    public String getFullText(int index, long noteId) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/anki_helper/anki_helper_image_" + noteId + "_0";

        String extension = new File(path + ".jpg").exists() ? "jpg" :
                (new File(path + ".png").exists() ? "png" : "gif");

        String germanWord = getGermanWord(index).replace(" ", "&nbsp");
        String image = "<img src=\"" + "anki_helper_image_" + noteId + "_0." + extension + "\" />";
        String personal = "";
        String soundAndIpa = "[sound:" + "anki_helper_sound_" + noteId + "_0.mp3]<div>" + getIPA(index) + "</div>";
        String spelling = "";

        char separator = 0x1F;
        return germanWord + separator + image + separator + personal + separator + soundAndIpa + separator + spelling;
    }

    public String getGermanWord(int index) {
        return context.getSharedPreferences("Word " + index, MODE_PRIVATE).getString(GoogleTranslateActivity.GERMAN_WORD, "");
    }

    public String getIPA(int index) {
        return context.getSharedPreferences("Word " + index, MODE_PRIVATE).getString(GoogleTranslateActivity.SHARED_IPA_SRC, "");
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

    public void createMedia(int currentWord) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/anki_helper/collection.anki2";
        // Create media
        String path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/anki_helper/";
        String[] medias = new String[currentWord * 2];

        // Add notes and cards
        for (int i = 0; i < currentWord; i++) {
            try {
                long noteId = insertNote(i);
                insertCard(0, noteId);
                insertCard(1, noteId);

                try {
                    String imagePath = getImagePath(i);
                    String soundPath = "anki_helper_sound" + i + ".mp3";

                    // Add the medias to the array
                    String path3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                            "/anki_helper/anki_helper_image" + i;

                    String extension = new File(path + ".jpg").exists() ? "jpg" :
                            (new File(path + ".png").exists() ? "png" : "gif");

                    medias[2 * i] = "anki_helper_image_" + noteId + "_0." + extension;
                    medias[2 * i + 1] = "anki_helper_sound_" + noteId + "_0.mp3";

                    // Convert the files
                    copy(new File(path2 + imagePath), new File(path2 + (2 * i)));
                    copy(new File(path2 + soundPath), new File(path2 + (2 * i + 1)));
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
        File jsonFile = new File(path2 + "media");
        try {
            PrintWriter pw = new PrintWriter(jsonFile);
            pw.print(json);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create ZIP
        String[] fileNames = new String[currentWord * 2 + 2];
        for (int i = 0; i < currentWord * 2; i++) {
            fileNames[i] = path2 + i;
        }

        fileNames[currentWord * 2] = path2 + "media";
        fileNames[currentWord * 2 + 1] = path2 + "collection.anki2";

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

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
