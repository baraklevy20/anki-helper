package levy.barak.ankihelper.vocabulary_screen;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

import levy.barak.ankihelper.AnkiHelperApp;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.anki.AnkiDatabase;
import levy.barak.ankihelper.anki.Word;
import levy.barak.ankihelper.grammar_screen.GrammarActivity;
import levy.barak.ankihelper.languages.Language;

public class VocabularyListActivity extends Activity {
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

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
        setContentView(R.layout.activity_vocabulary_list);

        // Ask for permissions
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.INTERNET}, 0);
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        AnkiHelperApp.readWords();

        RecyclerView cardsList = (RecyclerView) findViewById(R.id.cardsList);
        cardsList.getRecycledViewPool().setMaxRecycledViews(0,0);
        cardsList.setHasFixedSize(true);
        cardsList.setLayoutManager(new LinearLayoutManager(this));
        cardsList.setAdapter(new CardsListAdapter(this, AnkiHelperApp.allWords));

        EditText editText = (EditText) findViewById(R.id.englishWordEditText);

        // This enable to click enter and it would translate the word, instead of clicking "translate"
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_NEXT) {
                onFirstToSecondClick(null);
                return true;
            }
            return false;
        });


        // Used for the zoom animation
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // Disable the generate/clear buttons if necessary
        if (AnkiHelperApp.allWords.size() + AnkiHelperApp.allSentences.size() == 0) {
            findViewById(R.id.generateCardsButton).setEnabled(false);
            findViewById(R.id.clearButton).setEnabled(false);
        }

        // Populate the decks spinner
        String[] decksNames = AnkiHelperApp.decks.keySet().toArray(new String[AnkiHelperApp.decks.keySet().size()]);
        Spinner decksSpinner = (Spinner) findViewById(R.id.ankiDecksSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, decksNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        decksSpinner.setAdapter(adapter);

        // Set the value of the deck spinner to the last selected deck
        decksSpinner.setSelection(adapter.getPosition(AnkiHelperApp.lastUsedDeck));
        decksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AnkiHelperApp.saveLastUsedDeck((String) decksSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set the value of the language spinner to the last selected deck
        Spinner languageSpinner = (Spinner) findViewById(R.id.languagesSpinner);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Language.getLanguages());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
        languageSpinner.setSelection(adapter.getPosition(AnkiHelperApp.lastUsedLanguage));
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AnkiHelperApp.onLanguageChanged((String) languageSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onFirstToSecondClick(View view) {
        startVocabularyActivity(true);
    }

    public void onSecondToFirstClick(View view) {
        startVocabularyActivity(false);
    }

    private void startVocabularyActivity(boolean isFirstToSecondLanguage) {
        EditText editText = (EditText) findViewById(R.id.englishWordEditText);

        if (editText.getText().toString().trim().isEmpty()) {
            editText.setError("Please enter a word");
        }
        else {
            AnkiHelperApp.currentWord = new Word(
                    AnkiHelperApp.language.parseTypedWord(editText.getText().toString()), isFirstToSecondLanguage);
            startActivity(new Intent(this, VocabularyActivity.class).putExtra(
                    GoogleTranslateFragment.FIRST_LANGUAGE_TO_SECOND_LANGUAGE, isFirstToSecondLanguage));
        }
    }

    public void onClearClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Are you sure you want to remove the cards?").setPositiveButton("Yes", (dialog, which) -> clearList())
                .setNegativeButton("No", null).show();
    }

    public void onGenerateCardsClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("Are you sure you want to generate the cards? This would remove the cards and generate a ZIP for Anki.")
                .setPositiveButton("Yes", (dialog, which) -> {
            String selectedDeck = ((Spinner)findViewById(R.id.ankiDecksSpinner)).getSelectedItem().toString();
            //long selectedId = AnkiHelperApp.decks.get(selectedDeck);
            AnkiDatabase ankiDatabase = new AnkiDatabase(this, selectedDeck);

            ankiDatabase.generateDatabase();

            // Clear everything
            clearList();

            Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
        }).setNegativeButton("No", null).show();
    }

    public void zoomImageFromThumb(final View thumbView, String imagePath) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        expandedImageView.setImageBitmap(bitmap);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.translateActviityContainer)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    public void clearList() {
        // Clear the words
        AnkiHelperApp.allWords.clear();
        AnkiHelperApp.writeWords();

        // Clear the sentences
        AnkiHelperApp.allSentences.clear();;
        AnkiHelperApp.writeSentences();

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/anki_helper");

        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }

        // Refresh the list
        RecyclerView cardsList = (RecyclerView) findViewById(R.id.cardsList);
        cardsList.getAdapter().notifyDataSetChanged();

        // Disable generate and clear buttons
        findViewById(R.id.generateCardsButton).setEnabled(false);
        findViewById(R.id.clearButton).setEnabled(false);
    }

    public void onGrammarClick(View view) {
        startActivity(new Intent(this, GrammarActivity.class));
    }
}
