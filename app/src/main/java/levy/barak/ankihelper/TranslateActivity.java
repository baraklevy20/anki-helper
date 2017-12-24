package levy.barak.ankihelper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import levy.barak.ankihelper.anki_database.AnkiDatabase;

import static levy.barak.ankihelper.utils.ImageUtils.dipToPixels;

public class TranslateActivity extends Activity {
    public static final String PREFERENCES = "levy.barak.ankihelper";
    public static final String PREFERENCES_CURRENT_WORD = "levy.barak.ankihelper.CURRENT_WORD";

    public static String englishWord;
    public static String germanWord;
    public static int currentWord;

    public class CustomAdapter extends ArrayAdapter<Word> {
        Context context;
        ArrayList<Word> words;

        public CustomAdapter(Context context, ArrayList<Word> words) {
            super(context, -1, words);

            this.context = context;
            this.words = words;
        }

        class ViewHolder {
            TextView word;
            ImageView image;
            TextView ipa;
            ImageButton sound;
        }

        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.list_all_words, null, true);

                holder = new ViewHolder();
                holder.word = row.findViewById(R.id.word_list_word);
                holder.image = row.findViewById(R.id.word_list_image);
                holder.ipa = row.findViewById(R.id.word_list_ipa);
                holder.sound = row.findViewById(R.id.word_list_sound);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            try {
                // Set word and IPA
                holder.word.setText(getSharedPreferences("Word " + position, MODE_PRIVATE).getString(GoogleTranslateActivity.GERMAN_WORD, ""));
                holder.ipa.setText(getSharedPreferences("Word " + position, MODE_PRIVATE).getString(GoogleTranslateActivity.SHARED_IPA_SRC, ""));

                // Set image
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                        "/anki_helper/anki_helper_image" + position;

                String extension = new File(path + ".jpg").exists() ? "jpg" :
                        (new File(path + ".png").exists() ? "png" : "gif");

                Bitmap bitmap = BitmapFactory.decodeFile(path + "." + extension);
                Bitmap bt = Bitmap.createScaledBitmap(bitmap, dipToPixels(context, 150), dipToPixels(context, 100), true);
                holder.image.setImageBitmap(bt);

                //image.setImageURI(Uri.parse(path + "." + extension));

                // Set sound
                holder.sound.setOnClickListener(v -> {
                    try {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                        mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                                "/anki_helper/anki_helper_sound" + position + ".mp3"));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            catch (Exception e) {
                Toast.makeText(context, "There was an error reading card #" + position, Toast.LENGTH_LONG).show();
            }

            return row;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0
                || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "The app requires external write permissions. The app will now close", Toast.LENGTH_SHORT).show();
            finishAndRemoveTask();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        // Ask for permissions
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.INTERNET}, 0);
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        currentWord = getSharedPreferences(PREFERENCES, MODE_PRIVATE).getInt(PREFERENCES_CURRENT_WORD, 0);

        ArrayList<Word> words = new ArrayList<>();

        for (int i = 0; i < currentWord; i++) {
            words.add(new Word());
        }

        ListView listView = findViewById(R.id.newWordsList);
        listView.setAdapter(new CustomAdapter(this, words));
        EditText editText = findViewById(R.id.englishWordEditText);
        editText.setText("dog");

        // This enable to click enter and it would translate the word, instead of clicking "translate"
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                onTranslateClick(null);
                return true;
            }
            return false;
        });
    }

    public void onTranslateClick(View view) {
        EditText editText = findViewById(R.id.englishWordEditText);

        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError("Please enter a word");
        }
        else {
            englishWord = editText.getText().toString();
            startActivity(new Intent(this, GoogleTranslateActivity.class));
        }
    }

    public void onClearClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Are you sure you want to remove the cards?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void onGenerateCardsClick(View view) {
        boolean isDebug = ((ToggleButton)findViewById(R.id.debugButton)).isChecked();

        AnkiDatabase ankiDatabase = new AnkiDatabase(this, isDebug);

        ankiDatabase.generateDatabase(currentWord);

        // Clear everything
        onClearClick(view);

        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
    }

    public static String getImagePath(int index) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/anki_helper/anki_helper_image" + index;

        String extension = new File(path + ".jpg").exists() ? "jpg" :
                (new File(path + ".png").exists() ? "png" : "gif");

        return "anki_helper_image" + index + "." + extension;
    }

    public static SharedPreferences getCorrectPreferences(Context context) {
        return context.getSharedPreferences("Word " + currentWord, MODE_PRIVATE);
    }

    public static String getGermanWordWithoutPrefix() {
        Log.i("splitted", germanWord);
        String[] splitted = germanWord.split(" ");
        return splitted[splitted.length - 1];
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // Reset the words to 0
                    getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit().remove(PREFERENCES_CURRENT_WORD).commit();
                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                            "/anki_helper");

                    if (directory.exists()) {
                        for (File file : directory.listFiles()) {
                            file.delete();
                        }
                    }

                    // Refresh this activity
                    finish();
                    startActivity(getIntent());
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };
}
