package levy.barak.ankihelper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;

import levy.barak.ankihelper.anki_database.AnkiDatabase;

public class TranslateActivity extends Activity {
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

        AnkiHelperApplication.readWords();

        RecyclerView cardsList = findViewById(R.id.cardsList);
        cardsList.setHasFixedSize(true);
        cardsList.setLayoutManager(new LinearLayoutManager(this));
        cardsList.setAdapter(new CardsListAdapter(this, AnkiHelperApplication.allWords));

        EditText editText = findViewById(R.id.englishWordEditText);
        editText.setText("cat");

        // This enable to click enter and it would translate the word, instead of clicking "translate"
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
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
            AnkiHelperApplication.currentWord = new Word(editText.getText().toString());
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

        ankiDatabase.generateDatabase();

        // Clear everything
        onClearClick(view);

        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
    }

    public static String getGermanWordWithoutPrefix() {
        String[] splitted = AnkiHelperApplication.currentWord.germanWord.split(" ");
        return splitted[splitted.length - 1];
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // Reset the words to 0
                    AnkiHelperApplication.allWords.clear();
                    AnkiHelperApplication.writeWords();
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
