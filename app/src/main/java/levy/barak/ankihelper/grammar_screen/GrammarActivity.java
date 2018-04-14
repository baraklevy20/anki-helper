package levy.barak.ankihelper.grammar_screen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import levy.barak.ankihelper.R;

public class GrammarActivity extends Activity {
    GrammarFormSentenceFragment firstFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        // Create a new Fragment to be placed in the activity layout
        firstFragment = new GrammarFormSentenceFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        firstFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getFragmentManager().beginTransaction()
                .add(R.id.grammarFragmentsContainer, firstFragment).commit();
    }

    public void onAnyGrammarClick(View view) {
        firstFragment.onClick(view);
    }
}
