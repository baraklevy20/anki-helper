package levy.barak.ankihelper.levy.barak.ankihelper.utils;

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

import static android.content.Context.MODE_PRIVATE;
import static levy.barak.ankihelper.TranslateActivity.getImagePath;

/**
 * Created by baraklev on 12/24/2017.
 */

public class AnkiDatabase {
    private SQLiteDatabase db;
    private Context context;

    public AnkiDatabase(Context context) {
        this.context = context;
    }

    public void generateDatabase(int currentWord) {
        createDatabase();
        createMedia(currentWord);
    }

    public void createDatabase() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/anki_helper/collection.anki2";
        db = SQLiteDatabase.openOrCreateDatabase(path, null, null);

        String[] queries = dbCreation.split(";");

        for(String query : queries) {
            db.execSQL(query);
        }

        // Add a column (needed, has the deck's name and what not)
        db.execSQL(dbCreationCol);
    }

    public void insertCard(int order, long noteId) {
        db.execSQL(String.format("INSERT into cards VALUES ('%d', '%d', '%d', '%d', '%d', '-1','0','0','484332854','0','0','0','0','0','0','0','0','')",
                (long)(Math.random() * Long.MAX_VALUE),
                noteId,
                1508871866832L,
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

    final String dbCreation = "DROP TABLE IF EXISTS `revlog`;\n" +
            "CREATE TABLE IF NOT EXISTS `revlog` (\n" +
            "\t`id`\tinteger,\n" +
            "\t`cid`\tinteger NOT NULL,\n" +
            "\t`usn`\tinteger NOT NULL,\n" +
            "\t`ease`\tinteger NOT NULL,\n" +
            "\t`ivl`\tinteger NOT NULL,\n" +
            "\t`lastIvl`\tinteger NOT NULL,\n" +
            "\t`factor`\tinteger NOT NULL,\n" +
            "\t`time`\tinteger NOT NULL,\n" +
            "\t`type`\tinteger NOT NULL,\n" +
            "\tPRIMARY KEY(`id`)\n" +
            ");\n" +
            "DROP TABLE IF EXISTS `notes`;\n" +
            "CREATE TABLE IF NOT EXISTS `notes` (\n" +
            "\t`id`\tinteger,\n" +
            "\t`guid`\ttext NOT NULL,\n" +
            "\t`mid`\tinteger NOT NULL,\n" +
            "\t`mod`\tinteger NOT NULL,\n" +
            "\t`usn`\tinteger NOT NULL,\n" +
            "\t`tags`\ttext NOT NULL,\n" +
            "\t`flds`\ttext NOT NULL,\n" +
            "\t`sfld`\tinteger NOT NULL,\n" +
            "\t`csum`\tinteger NOT NULL,\n" +
            "\t`flags`\tinteger NOT NULL,\n" +
            "\t`data`\ttext NOT NULL,\n" +
            "\tPRIMARY KEY(`id`)\n" +
            ");\n" +
            "DROP TABLE IF EXISTS `graves`;\n" +
            "CREATE TABLE IF NOT EXISTS `graves` (\n" +
            "\t`usn`\tinteger NOT NULL,\n" +
            "\t`oid`\tinteger NOT NULL,\n" +
            "\t`type`\tinteger NOT NULL\n" +
            ");\n" +
            "DROP TABLE IF EXISTS `col`;\n" +
            "CREATE TABLE IF NOT EXISTS `col` (\n" +
            "\t`id`\tinteger,\n" +
            "\t`crt`\tinteger NOT NULL,\n" +
            "\t`mod`\tinteger NOT NULL,\n" +
            "\t`scm`\tinteger NOT NULL,\n" +
            "\t`ver`\tinteger NOT NULL,\n" +
            "\t`dty`\tinteger NOT NULL,\n" +
            "\t`usn`\tinteger NOT NULL,\n" +
            "\t`ls`\tinteger NOT NULL,\n" +
            "\t`conf`\ttext NOT NULL,\n" +
            "\t`models`\ttext NOT NULL,\n" +
            "\t`decks`\ttext NOT NULL,\n" +
            "\t`dconf`\ttext NOT NULL,\n" +
            "\t`tags`\ttext NOT NULL,\n" +
            "\tPRIMARY KEY(`id`)\n" +
            ");\n" +
            "DROP TABLE IF EXISTS `cards`;\n" +
            "CREATE TABLE IF NOT EXISTS `cards` (\n" +
            "\t`id`\tinteger,\n" +
            "\t`nid`\tinteger NOT NULL,\n" +
            "\t`did`\tinteger NOT NULL,\n" +
            "\t`ord`\tinteger NOT NULL,\n" +
            "\t`mod`\tinteger NOT NULL,\n" +
            "\t`usn`\tinteger NOT NULL,\n" +
            "\t`type`\tinteger NOT NULL,\n" +
            "\t`queue`\tinteger NOT NULL,\n" +
            "\t`due`\tinteger NOT NULL,\n" +
            "\t`ivl`\tinteger NOT NULL,\n" +
            "\t`factor`\tinteger NOT NULL,\n" +
            "\t`reps`\tinteger NOT NULL,\n" +
            "\t`lapses`\tinteger NOT NULL,\n" +
            "\t`left`\tinteger NOT NULL,\n" +
            "\t`odue`\tinteger NOT NULL,\n" +
            "\t`odid`\tinteger NOT NULL,\n" +
            "\t`flags`\tinteger NOT NULL,\n" +
            "\t`data`\ttext NOT NULL,\n" +
            "\tPRIMARY KEY(`id`)\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_revlog_usn`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_revlog_usn` ON `revlog` (\n" +
            "\t`usn`\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_revlog_cid`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_revlog_cid` ON `revlog` (\n" +
            "\t`cid`\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_notes_usn`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_notes_usn` ON `notes` (\n" +
            "\t`usn`\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_notes_csum`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_notes_csum` ON `notes` (\n" +
            "\t`csum`\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_cards_usn`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_cards_usn` ON `cards` (\n" +
            "\t`usn`\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_cards_sched`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_cards_sched` ON `cards` (\n" +
            "\t`did`,\n" +
            "\t`queue`,\n" +
            "\t`due`\n" +
            ");\n" +
            "DROP INDEX IF EXISTS `ix_cards_nid`;\n" +
            "CREATE INDEX IF NOT EXISTS `ix_cards_nid` ON `cards` (\n" +
            "\t`nid`\n" +
            ");";
    static final String dbCreationCol = "INSERT INTO `col` (id,crt,mod,scm,ver,dty,usn,ls,conf,models,decks,dconf,tags) VALUES (1,1505955600,1511814589332,1511814588844,11,0,0,0,'{\"nextPos\": 1, \"estTimes\": true, \"activeDecks\": [1], \"sortType\": \"noteFld\", \"timeLim\": 0, \"sortBackwards\": false, \"addToCur\": true, \"curDeck\": 1, \"newBury\": true, \"newSpread\": 0, \"dueCounts\": true, \"curModel\": \"1511814588845\", \"collapseTime\": 1200}','{\"1366717138083\": {\"vers\": [], \"name\": \"1. Spellings and Sounds\", \"tags\": [], \"did\": 1398979153351, \"usn\": 28, \"req\": [[0, \"any\", [0, 1, 2]], [1, \"any\", [2, 3]]], \"flds\": [{\"name\": \"Spelling (a letter or combination of letters)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 0, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Example word for that spelling/sound combination\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 1, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"Picture of the example word\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 2, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"Recording of the Word (/IPA)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 3, \"font\": \"Arial\", \"size\": 20}], \"sortf\": 1, \"latexPre\": \"\\\\documentclass[12pt]{article}\\n\\\\special{papersize=3in,5in}\\n\\\\usepackage{amssymb,amsmath}\\n\\\\pagestyle{empty}\\n\\\\setlength{\\\\parindent}{0in}\\n\\\\begin{document}\\n\", \"tmpls\": [{\"name\": \"What''s the sound?\", \"qfmt\": \"What sound does this make?<br><br>\\n<div style=''font-family: Arial; font-size: 40px;''>{{Spelling (a letter or combination of letters)}}</div><br>\\nas in <b>\\\"{{Example word for that spelling/sound combination}}\\\"</b>\\n<br><br>\\n<div style=\\\"margin:0 auto\\\">{{Picture of the example word}}</div>\\n\\n\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\n{{#Recording of the Word (/IPA)}}{{Recording of the Word (/IPA)}}{{/Recording of the Word (/IPA)}}<br>\\n\", \"ord\": 0, \"bqfmt\": \"\"}, {\"name\": \"What''s the spelling?\", \"qfmt\": \"How do you spell this word?\\n\\n{{#Recording of the Word (/IPA)}}{{Recording of the Word (/IPA)}}\\n{{/Recording of the Word (/IPA)}}<br>\\n<br>\\n<div style=\\\"margin:0 auto\\\">{{Picture of the example word}}</div>\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"<div style=''font-family: Arial; font-size: 30px;''>\\\"{{Example word for that spelling/sound combination}}\\\"</b></div>\\n\\n<br><br>(Example for the spelling: \\\"{{Spelling (a letter or combination of letters)}}\\\")\", \"ord\": 1, \"bqfmt\": \"\"}], \"latexPost\": \"\\\\end{document}\", \"type\": 0, \"id\": 1366717138083, \"css\": \".card {\\n font-family: arial;\\n font-size: 20px;\\n text-align: center;\\n color: black;\\n background-color: white;\\n}\\n\\n.card1 { background-color: #FFFFFF; }\\n.card2 { background-color: #FFFFFF; }\", \"mod\": 1508871866}, \"1366716141610\": {\"vers\": [], \"name\": \"2. Picture Words\", \"tags\": [], \"did\": 1508871866832, \"usn\": 66, \"req\": [[0, \"all\", [0]], [1, \"all\", [1]], [2, \"all\", [4]]], \"flds\": [{\"name\": \"Word\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 0, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"Picture\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 1, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"Gender, Personal Connection, Extra Info (Back side)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 2, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"Pronunciation (Recording and/or IPA)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 3, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Test Spelling? (y = yes, blank = no)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 4, \"font\": \"Arial\", \"size\": 20}], \"sortf\": 0, \"tmpls\": [{\"name\": \"Comprehension Card\", \"qfmt\": \"{{Word}}\\n\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n{{Picture}}\\n\\n{{#Pronunciation (Recording and/or IPA)}}<br><font color=blue>{{Pronunciation (Recording and/or IPA)}}</font>{{/Pronunciation (Recording and/or IPA)}}<br>\\n\\n\\n<span style=\\\"color:grey\\\">{{Gender, Personal Connection, Extra Info (Back side)}}</span>\\n<br><br>\\n\", \"ord\": 0, \"bqfmt\": \"\"}, {\"name\": \"Production Card\", \"qfmt\": \"{{Picture}}<br><br>\\n\\n<font color=red></font><br><br>\\n<font color=red></font><br><br>\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\n<span style=\\\"font-size:1.5em;\\\">{{Word}}</span><br>\\n\\n\\n{{#Pronunciation (Recording and/or IPA)}}<br><font color=blue>{{Pronunciation (Recording and/or IPA)}}</font>{{/Pronunciation (Recording and/or IPA)}}\\n\\n{{#Gender, Personal Connection, Extra Info (Back side)}}<br><font color=grey>{{Gender, Personal Connection, Extra Info (Back side)}}</font>{{/Gender, Personal Connection, Extra Info (Back side)}}\\n\\n\\n<span style=\\\"\\\">\", \"ord\": 1, \"bqfmt\": \"\"}, {\"name\": \"Spelling?\", \"qfmt\": \"{{#Test Spelling? (y = yes, blank = no)}}\\nSpell this word: <br><br>\\n{{Picture}}<br>\\n\\n{{#Pronunciation (Recording and/or IPA)}}<br><font color=blue>{{Pronunciation (Recording and/or IPA)}}</font>{{/Pronunciation (Recording and/or IPA)}}\\n<br>\\n\\n{{/Test Spelling? (y = yes, blank = no)}}\\n\\n\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"<span style=\\\"font-size:1.5em;\\\">{{Word}}</span><br><br>\\n\\n\\n{{Picture}}<br>\\n\\n<span style=\\\"color:grey;\\\">{{Gender, Personal Connection, Extra Info (Back side)}}</span>\\n\", \"ord\": 2, \"bqfmt\": \"\"}], \"mod\": 1509951317, \"latexPost\": \"\\\\end{document}\", \"type\": 0, \"id\": 1366716141610, \"css\": \".card {\\n font-family: arial;\\n font-size: 20px;\\n text-align: center;\\n color: black;\\n background-color: white;\\n}\\n\\n.card1 { background-color: #FFFFFF; }\\n.card2 { background-color: #FFFFFF; }\", \"latexPre\": \"\\\\documentclass[12pt]{article}\\n\\\\special{papersize=3in,5in}\\n\\\\usepackage{amssymb,amsmath}\\n\\\\pagestyle{empty}\\n\\\\setlength{\\\\parindent}{0in}\\n\\\\begin{document}\\n\"}, \"1355577990691\": {\"vers\": [], \"name\": \"1. Minimal Pairs\", \"tags\": [], \"did\": 1382740944947, \"usn\": 28, \"req\": [[0, \"any\", [0, 1, 2, 3, 4, 6, 7]], [1, \"any\", [0, 2, 3, 4, 5, 6, 7]], [2, \"all\", [8]], [3, \"all\", [8]], [4, \"all\", [8, 12]], [5, \"all\", [8, 12]]], \"flds\": [{\"name\": \"Word 1\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 0, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Recording 1\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 1, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 1 IPA\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 2, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 1 English\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 3, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 2\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 4, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Recording 2\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 5, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 2 IPA\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 6, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 2 English\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 7, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 3\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 8, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Recording 3\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 9, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 3 IPA\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 10, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Word 3 English\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 11, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Compare Word 2 to Word 3? (y = yes, leave blank = no)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 12, \"font\": \"Arial\", \"size\": 20}], \"sortf\": 0, \"tmpls\": [{\"name\": \"Card 1\", \"qfmt\": \"<i>Do you hear</i><br><br>\\n<div class=container>\\n<div class=box>{{Word 1}}\\n<span class=ipa>[{{Word 1 IPA}}]</span>\\n<span class=translation>{{Word 1 English}}</span>\\n</div>\\n\\n<div class=or><i> or </i></div>\\n\\n<div class=box>{{Word 2}}\\n<span class=ipa>[{{Word 2 IPA}}]</span>\\n<span class=translation>{{Word 2 English}}</span>\\n</div>\\n</div>\\n<br>{{Recording 1}} \", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\nYou heard: <div class=box>{{Word 1}}</div></b><br><br>\\n\\n{{Recording 1}}\", \"ord\": 0, \"bqfmt\": \"\"}, {\"name\": \"Card 2\", \"qfmt\": \"<i>Do you hear</i><br><br>\\n<div class=container>\\n<div class=box>{{Word 1}}\\n<span class=ipa>[{{Word 1 IPA}}]</span>\\n<span class=translation>{{Word 1 English}}</span>\\n</div>\\n\\n<div class=or><i> or </i></div>\\n\\n<div class=box>{{Word 2}}\\n<span class=ipa>[{{Word 2 IPA}}]</span>\\n<span class=translation>{{Word 2 English}}</span>\\n</div>\\n</div>\\n<br>{{Recording 2}} \\n\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\nYou heard: <div class=box>{{Word 2}}</div></b><br><br>\\n{{Recording 2}}\\n  \\n\", \"ord\": 1, \"bqfmt\": \"\"}, {\"name\": \"Card 3\", \"qfmt\": \"{{#Word 3}}\\n<i>Do you hear</i><br><br>\\n<div class=container>\\n<div class=box>{{Word 1}}\\n<span class=ipa>[{{Word 1 IPA}}]</span>\\n<span class=translation>{{Word 1 English}}</span>\\n</div>\\n\\n<div class=or><i> or </i></div>\\n\\n<div class=box>{{Word 3}}\\n<span class=ipa>[{{Word 3 IPA}}]</span>\\n<span class=translation>{{Word 3 English}}</span>\\n</div>\\n</div>\\n<br>{{Recording 3}} \\n{{/Word 3}}\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\nYou heard: <div class=box>{{Word 3}}</div></b><br><br>\\n\\n{{Recording 3}}\", \"ord\": 2, \"bqfmt\": \"\"}, {\"name\": \"Card 4\", \"qfmt\": \"{{#Word 3}}\\n<i>Do you hear</i><br><br>\\n<div class=container>\\n<div class=box>{{Word 1}}\\n<span class=ipa>[{{Word 1 IPA}}]</span>\\n<span class=translation>{{Word 1 English}}</span>\\n</div>\\n\\n<div class=or><i> or </i></div>\\n\\n<div class=box>{{Word 3}}\\n<span class=ipa>[{{Word 3 IPA}}]</span>\\n<span class=translation>{{Word 3 English}}</span>\\n</div>\\n</div>\\n<br>{{Recording 1}} \\n{{/Word 3}}\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\nYou heard: <div class=box>{{Word 1}}</div></b><br><br>\\n\\n{{Recording 1}}\", \"ord\": 3, \"bqfmt\": \"\"}, {\"name\": \"Card 5\", \"qfmt\": \"{{#Compare Word 2 to Word 3? (y = yes, leave blank = no)}}\\n{{#Word 3}}\\n<i>Do you hear</i><br><br>\\n<div class=container>\\n<div class=box>{{Word 2}}\\n<span class=ipa>[{{Word 2 IPA}}]</span>\\n<span class=translation>{{Word 2 English}}</span>\\n</div>\\n\\n<div class=or><i> or </i></div>\\n\\n<div class=box>{{Word 3}}\\n<span class=ipa>[{{Word 3 IPA}}]</span>\\n<span class=translation>{{Word 3 English}}</span>\\n</div>\\n</div>\\n<br>{{Recording 2}} \\n{{/Word 3}}\\n{{/Compare Word 2 to Word 3? (y = yes, leave blank = no)}}\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\nYou heard: <div class=box>{{Word 2}}</div></b><br><br>\\n\\n{{Recording 2}}\", \"ord\": 4, \"bqfmt\": \"\"}, {\"name\": \"Card 6\", \"qfmt\": \"{{#Compare Word 2 to Word 3? (y = yes, leave blank = no)}}\\n{{#Word 3}}\\n<i>Do you hear</i><br><br>\\n<div class=container>\\n<div class=box>{{Word 2}}\\n<span class=ipa>[{{Word 2 IPA}}]</span>\\n<span class=translation>{{Word 2 English}}</span>\\n</div>\\n\\n<div class=or><i> or </i></div>\\n\\n<div class=box>{{Word 3}}\\n<span class=ipa>[{{Word 3 IPA}}]</span>\\n<span class=translation>{{Word 3 English}}</span>\\n</div>\\n</div>\\n<br>{{Recording 3}} \\n{{/Word 3}}\\n{{/Compare Word 2 to Word 3? (y = yes, leave blank = no)}}\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\nYou heard: <div class=box>{{Word 3}}</div></b><br><br>\\n\\n{{Recording 3}}\", \"ord\": 5, \"bqfmt\": \"\"}], \"mod\": 1508871866, \"latexPost\": \"\\\\end{document}\", \"type\": 0, \"id\": 1355577990691, \"css\": \".card {\\n font-family: arial;\\n font-size: 20px;\\n text-align: center;\\n color: black;\\n background-color: white;\\n}\\n.box {\\n display:inline-block;\\nborder:2px solid black;\\npadding:5px;\\nfont-size:1.4em\\n}\\n\\n.ipa {\\nfont-size:0.7em;\\ndisplay:block;\\ncolor:blue;\\npadding:0 0 5px 0px;\\n}\\n\\n.container {\\nborder:0px solid;\\ndisplay:table;\\nmargin:auto;\\n}\\n\\n.or {\\ndisplay:table-cell;\\nvertical-align:middle;\\npadding:0 10px\\n}\\n.translation {\\nfont-size:0.6em;\\ndisplay:block;\\ncolor:gray;\\n}\\n\", \"latexPre\": \"\\\\documentclass[12pt]{article}\\n\\\\special{papersize=3in,5in}\\n\\\\usepackage{amssymb,amsmath}\\n\\\\pagestyle{empty}\\n\\\\setlength{\\\\parindent}{0in}\\n\\\\begin{document}\\n\"}, \"1366981665623\": {\"vers\": [], \"name\": \"2. Mnemonics\", \"tags\": [], \"did\": 1398979153351, \"usn\": 28, \"req\": [[0, \"all\", [0]], [1, \"any\", [1, 2]]], \"flds\": [{\"name\": \"Mnemonic Image (Burning, Exploding. Use a picture)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 0, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"Meaning of this mnemonic (Masculine, feminine)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 1, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Example word for this mnemonic (With pictures)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 2, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Optional: Any extra info (Back side of both cards)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 3, \"font\": \"Arial\", \"size\": 12}, {\"name\": \"(Just for copy/paste)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 4, \"font\": \"Arial\", \"size\": 20}], \"sortf\": 0, \"tmpls\": [{\"name\": \"What''s the mnemonic mean?\", \"qfmt\": \"What''s this mnemonic mean?<br><br>{{Mnemonic Image (Burning, Exploding. Use a picture)}}\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\n{{#Meaning of this mnemonic (Masculine, feminine)}}<i>{{Meaning of this mnemonic (Masculine, feminine)}}<br></i>{{/Meaning of this mnemonic (Masculine, feminine)}}\\n<br>\\n\\n<font color=grey>{{Optional: Any extra info (Back side of both cards)}}</font>\\n\", \"ord\": 0, \"bqfmt\": \"\"}, {\"name\": \"What''s the mnemonic for __?\", \"qfmt\": \"What''s the mnemonic for: {{#Meaning of this mnemonic (Masculine, feminine)}}<font color=red>{{Meaning of this mnemonic (Masculine, feminine)}}</font>?<br><br>{{/Meaning of this mnemonic (Masculine, feminine)}}\\n\\nFor examples like:<div style=''font-family: Arial; font-size: 20px;''>{{Example word for this mnemonic (With pictures)}}</div>\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\n{{Mnemonic Image (Burning, Exploding. Use a picture)}}\\n\\n\\n\\n{{#Optional: Any extra info (Back side of both cards)}}<br><font color=grey>{{Optional: Any extra info (Back side of both cards)}}</font>{{/Optional: Any extra info (Back side of both cards)}}\", \"ord\": 1, \"bqfmt\": \"\"}], \"mod\": 1508871866, \"latexPost\": \"\\\\end{document}\", \"type\": 0, \"id\": 1366981665623, \"css\": \".card {\\n font-family: arial;\\n font-size: 20px;\\n text-align: center;\\n color: black;\\n background-color: white;\\n}\\n\\n.card1 { background-color: #FFFFFF; }\\n.card2 { background-color: #FFFFFF; }\", \"latexPre\": \"\\\\documentclass[12pt]{article}\\n\\\\special{papersize=3in,5in}\\n\\\\usepackage{amssymb,amsmath}\\n\\\\pagestyle{empty}\\n\\\\setlength{\\\\parindent}{0in}\\n\\\\begin{document}\\n\"}, \"1366982516457\": {\"vers\": [], \"name\": \"3. All-Purpose Card\", \"tags\": [], \"did\": 1398979153351, \"usn\": 28, \"req\": [[0, \"any\", [0, 1, 2]], [1, \"all\", [3, 6]], [2, \"all\", [7]]], \"flds\": [{\"name\": \"Front (Example with word blanked out or missing)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 0, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Front (Picture)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 1, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Front (Definitions, base word, etc.)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 2, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"Back (a single word/phrase, no context)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 3, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"- The full sentence (no words blanked out)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 4, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"- Extra Info (Pronunciation, personal connections, conjugations, etc)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 5, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"\\u2022 Make 2 cards? (\\\"y\\\" = yes, blank = no)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 6, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"\\u2022 Test Spelling (Insert sound file/pronunciation here to test, leave blank otherwise)\", \"media\": [], \"sticky\": false, \"rtl\": false, \"ord\": 7, \"font\": \"Arial\", \"size\": 20}, {\"name\": \"(Copy and paste area)\", \"media\": [], \"sticky\": true, \"rtl\": false, \"ord\": 8, \"font\": \"Arial\", \"size\": 20}], \"sortf\": 0, \"tmpls\": [{\"name\": \"Card 1: What word fits into the blank?\", \"qfmt\": \"{{Front (Example with word blanked out or missing)}}<br><br>\\n\\n<div style=''font-family: Arial; font-size: 20px;''>{{Front (Picture)}}</div>\\n<br>\\n<div style=''font-family: Arial; font-size: 20px;color:red''>{{Front (Definitions, base word, etc.)}}</div>\\n\\n\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\n{{Back (a single word/phrase, no context)}}\\n<br>\\n<br>\\n<strong><div style=''font-family: Arial; font-size: 20px;''>{{- The full sentence (no words blanked out)}}</div></strong><br>\\n<div style=''font-family: Arial; font-size: 20px;color:grey;''>{{- Extra Info (Pronunciation, personal connections, conjugations, etc)}}</div>\\n\\n\", \"ord\": 0, \"bqfmt\": \"\"}, {\"name\": \"Optional Card 2: Give an example for this word\", \"qfmt\": \"{{#\\u2022 Make 2 cards? (\\\"y\\\" = yes, blank = no)}}{{Back (a single word/phrase, no context)}}{{/\\u2022 Make 2 cards? (\\\"y\\\" = yes, blank = no)}}\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{FrontSide}}\\n\\n<hr id=answer>\\n\\n{{Front (Example with word blanked out or missing)}}<br><br>\\n<strong><div style=''font-family: Arial; font-size: 20px;''>{{- The full sentence (no words blanked out)}}</div></strong><br>\\n\\n<div style=''font-family: Arial; font-size: 20px;''>{{Front (Picture)}}</div>\\n<br>\\n<div style=''font-family: Arial; font-size: 20px;color:red''>{{Front (Definitions, base word, etc.)}}</div><br>\\n<div style=''font-family: Arial; font-size: 20px;color:grey;''>{{- Extra Info (Pronunciation, personal connections, conjugations, etc)}}</div>\\n\\n\\n\\n\", \"ord\": 1, \"bqfmt\": \"\"}, {\"name\": \"Optional Card #3: Test spelling (For Chinese, Japanese)\", \"qfmt\": \"{{#\\u2022 Test Spelling (Insert sound file/pronunciation here to test, leave blank otherwise)}}Spell this word:<br><br>\\n{{Front (Example with word blanked out or missing)}}<br><br>\\n\\n<div style=''font-family: Arial; font-size: 20px;''>{{Front (Picture)}}</div>\\n<br>\\n<div style=''font-family: Arial; font-size: 20px;color:red''>{{Front (Definitions, base word, etc.)}}</div><br>\\n{{\\u2022 Test Spelling (Insert sound file/pronunciation here to test, leave blank otherwise)}}\\n{{/\\u2022 Test Spelling (Insert sound file/pronunciation here to test, leave blank otherwise)}}\\n\\n\\n\\n\", \"did\": null, \"bafmt\": \"\", \"afmt\": \"{{Back (a single word/phrase, no context)}}\\n\\n<br>\\n\\n<br>\\n<strong><div style=''font-family: Arial; font-size: 20px;''>{{- The full sentence (no words blanked out)}}</div></strong><br>\\n<div style=''font-family: Arial; font-size: 20px;color:grey;''>{{- Extra Info (Pronunciation, personal connections, conjugations, etc)}}</div>\\n<hr id=answer>\\n{{FrontSide}}\\n\\n\", \"ord\": 2, \"bqfmt\": \"\"}], \"mod\": 1508871866, \"latexPost\": \"\\\\end{document}\", \"type\": 0, \"id\": 1366982516457, \"css\": \".card {\\n font-family: arial;\\n font-size: 20px;\\n text-align: center;\\n color: black;\\n background-color: white;\\n}\\n\", \"latexPre\": \"\\\\documentclass[12pt]{article}\\n\\\\special{papersize=3in,5in}\\n\\\\usepackage{amssymb,amsmath}\\n\\\\pagestyle{empty}\\n\\\\setlength{\\\\parindent}{0in}\\n\\\\begin{document}\\n\"}}','{\"1\": {\"desc\": \"\", \"name\": \"Default\", \"extendRev\": 50, \"usn\": 0, \"collapsed\": false, \"newToday\": [0, 0], \"timeToday\": [0, 0], \"dyn\": 0, \"extendNew\": 10, \"conf\": 1, \"revToday\": [0, 0], \"lrnToday\": [0, 0], \"id\": 1, \"mod\": 1511814588}, \"1508871866834\": {\"desc\": \"\", \"name\": \"My German List :)::2. Everything Else\", \"extendRev\": 50, \"usn\": 55, \"collapsed\": false, \"newToday\": [67, 0], \"mid\": 1366716141610, \"dyn\": 0, \"extendNew\": 10, \"lrnToday\": [67, 0], \"conf\": 1360013976288, \"revToday\": [67, 0], \"timeToday\": [67, 0], \"id\": 1508871866834, \"mod\": 1509742262}, \"1508871866832\": {\"desc\": \"\", \"name\": \"My German List :)\", \"extendRev\": 50, \"usn\": 65, \"collapsed\": false, \"browserCollapsed\": false, \"mid\": 1366716141610, \"newToday\": [67, 0], \"dyn\": 0, \"extendNew\": 10, \"lrnToday\": [67, 0], \"conf\": 1360013976288, \"revToday\": [67, 0], \"timeToday\": [67, 0], \"id\": 1508871866832, \"mod\": 1510816860}, \"1508871866833\": {\"desc\": \"\", \"name\": \"My German List :)::1. Minimal Pairs\", \"extendRev\": 50, \"usn\": 28, \"collapsed\": false, \"newToday\": [67, 0], \"mid\": 1355577990691, \"dyn\": 0, \"extendNew\": 10, \"lrnToday\": [67, 0], \"conf\": 1355587023114, \"revToday\": [67, 0], \"timeToday\": [67, 0], \"id\": 1508871866833, \"mod\": 1508872300}}','{\"1\": {\"name\": \"Default\", \"replayq\": true, \"lapse\": {\"leechFails\": 8, \"minInt\": 1, \"delays\": [10], \"leechAction\": 0, \"mult\": 0}, \"rev\": {\"perDay\": 100, \"fuzz\": 0.05, \"ivlFct\": 1, \"maxIvl\": 36500, \"ease4\": 1.3, \"bury\": true, \"minSpace\": 1}, \"timer\": 0, \"maxTaken\": 60, \"usn\": 0, \"new\": {\"perDay\": 20, \"delays\": [1, 10], \"separate\": true, \"ints\": [1, 4, 7], \"initialFactor\": 2500, \"bury\": true, \"order\": 1}, \"mod\": 0, \"id\": 1, \"autoplay\": true}, \"1355587023114\": {\"name\": \"Pronunciation\", \"replayq\": true, \"lapse\": {\"leechFails\": 15, \"minInt\": 1, \"delays\": [1, 1], \"leechAction\": 0, \"mult\": 0.0}, \"rev\": {\"perDay\": 100, \"fuzz\": 0.1, \"ivlFct\": 1.0, \"maxIvl\": 36500, \"ease4\": 1.3, \"bury\": true, \"minSpace\": 1}, \"timer\": 0, \"addon_audio_download_language\": \"fr\", \"dyn\": false, \"maxTaken\": 60, \"usn\": 30, \"new\": {\"separate\": true, \"delays\": [1, 1, 1, 1], \"perDay\": 8, \"ints\": [1, 3, 7], \"initialFactor\": 2500, \"bury\": false, \"order\": 1}, \"autoplay\": true, \"id\": 1355587023114, \"mod\": 1509046931}, \"1360013976288\": {\"name\": \"Grammar\", \"replayq\": true, \"lapse\": {\"leechFails\": 10, \"minInt\": 2, \"delays\": [10], \"leechAction\": 0, \"mult\": 0.2}, \"rev\": {\"perDay\": 150, \"ivlFct\": 1.0, \"maxIvl\": 36500, \"minSpace\": 2, \"ease4\": 1.3, \"bury\": true, \"fuzz\": 0.05}, \"timer\": 0, \"dyn\": false, \"maxTaken\": 60, \"usn\": 66, \"new\": {\"perDay\": 30, \"delays\": [10], \"separate\": false, \"ints\": [2, 4, 7], \"initialFactor\": 2500, \"bury\": false, \"order\": 0}, \"mod\": 1509951259, \"id\": 1360013976288, \"autoplay\": true}}','{}');\n";
}
