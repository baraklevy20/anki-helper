package levy.barak.ankihelper.grammar_screen;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.anki.Sentence;
import levy.barak.ankihelper.vocabulary_screen.VocabularyListActivity;

/**
 * Created by baraklev on 3/11/2018.
 */

public class GrammarFormSentenceFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_grammar_form_sentence, container, false);

        return fragment;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.grammarGoButton:
                onGoClick(view);
                break;
            case R.id.grammarDoneButton:
                onDoneClick(view);
                break;
        }
    }

    public void onGoClick(View view) {
        AnkiHelperApplication.currentSentence = new Sentence();
        AnkiHelperApplication.currentSentence.words = ((EditText)getActivity().findViewById(R.id.grammarSentence)).getText().toString().split(" ");
        AnkiHelperApplication.currentSentence.isWordUsed = new boolean[AnkiHelperApplication.currentSentence.words.length];

        for (int i = 0; i < AnkiHelperApplication.currentSentence.isWordUsed.length; i++) {
            AnkiHelperApplication.currentSentence.isWordUsed[i] = true;
        }

        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.grammarButtonsLayout);

        for (int i = 0; i < AnkiHelperApplication.currentSentence.words.length; i++) {
            ToggleButton wordButton = new ToggleButton(this.getContext());
            wordButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            wordButton.setTextOn(AnkiHelperApplication.currentSentence.words[i]);
            wordButton.setTextOff(AnkiHelperApplication.currentSentence.words[i]);
            wordButton.setText(AnkiHelperApplication.currentSentence.words[i]);
            wordButton.setChecked(true);
            int finalI = i;
            wordButton.setOnClickListener((v) -> AnkiHelperApplication.currentSentence.isWordUsed[finalI] = wordButton.isChecked());

            layout.addView(wordButton);
        }
    }

    public void onDoneClick(View view) {
        AnkiHelperApplication.allSentences.add(AnkiHelperApplication.currentSentence);
        AnkiHelperApplication.writeSentences();
        startActivity(new Intent(this.getActivity(), VocabularyListActivity.class));
    }
}
