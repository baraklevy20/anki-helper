package levy.barak.ankihelper.vocabulary_screen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.anki.Sentence;

public class GrammarActivity extends Activity {
    Sentence sentence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);
    }

    public void onGoClick(View view) {
        sentence = new Sentence();
        sentence.words = ((EditText)findViewById(R.id.grammarSentence)).getText().toString().split(" ");
        sentence.isWordUsed = new boolean[sentence.words.length];

        for (int i = 0; i < sentence.isWordUsed.length; i++) {
            sentence.isWordUsed[i] = true;
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.grammarButtonsLayout);

        for (int i = 0; i < sentence.words.length; i++) {
            ToggleButton wordButton = new ToggleButton(this);
            wordButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            wordButton.setTextOn(sentence.words[i]);
            wordButton.setTextOff(sentence.words[i]);
            wordButton.setText(sentence.words[i]);
            wordButton.setChecked(true);
            int finalI = i;
            wordButton.setOnClickListener((v) -> sentence.isWordUsed[finalI] = wordButton.isChecked());

            layout.addView(wordButton);
        }
    }

    public void onDoneClick(View view) {
        AnkiHelperApplication.allSentences.add(sentence);
        AnkiHelperApplication.writeSentences();
        startActivity(new Intent(this, VocabularyListActivity.class));
    }
}
