package levy.barak.ankihelper.vocabulary_screen;

import android.app.Activity;
import android.os.Bundle;

import levy.barak.ankihelper.R;

public class VocabularyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        // Create a new Fragment to be placed in the activity layout
        GoogleTranslateFragment firstFragment = new GoogleTranslateFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        firstFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getFragmentManager().beginTransaction()
                .add(R.id.fragmentsContainer, firstFragment).commit();
    }
}
