package levy.barak.ankihelper.vocabulary_screen;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import levy.barak.ankihelper.AnkiHelperApplication;
import levy.barak.ankihelper.R;
import levy.barak.ankihelper.anki.Word;
import levy.barak.ankihelper.utils.ImageUtils;

/**
 * Created by baraklev on 12/25/2017.
 */

public class CardsListAdapter extends RecyclerView.Adapter<CardsListAdapter.DataObjectHolder> {
    private ArrayList<Word> mWords;
    private Activity mVocabularyListActivity;
    private MediaPlayer mMediaPlayer;

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        private TextView word;
        private TextView ipa;
        private TextView examples;
        private ImageButton sound;
        private ImageView image;
        private ImageButton removeButton;
        private TextView additionalInformation;

        private int currentExampleIndex;

        public DataObjectHolder(View itemView) {
            super(itemView);

            word = (TextView) itemView.findViewById(R.id.word_list_word);
            ipa = (TextView) itemView.findViewById(R.id.word_list_ipa);
            examples = (TextView) itemView.findViewById(R.id.word_list_examples);
            sound = (ImageButton) itemView.findViewById(R.id.word_list_sound);
            image = (ImageView) itemView.findViewById(R.id.word_list_image);
            removeButton = (ImageButton) itemView.findViewById(R.id.word_remove_button);
            additionalInformation = (TextView) itemView.findViewById(R.id.word_list_additional_information);

            currentExampleIndex = 0;
        }
    }

    public CardsListAdapter(Activity vocabularyListActivity, ArrayList<Word> myDataset) {
        mWords = myDataset;
        mVocabularyListActivity = vocabularyListActivity;
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_card, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        Word word = mWords.get(position);

        // Set word and IPA
        holder.word.setText(word.secondLanguageWord + "/" + word.plural + (word.wordCategory == null ? "" :
                " (" + AnkiHelperApplication.language.wordCategoriesTranslations.get(word.wordCategory) + ")"));
        holder.ipa.setText(word.ipa);
        holder.additionalInformation.setText(word.additionalInformation);

        // Set examples
        if (word.wordInASentences != null && word.wordInASentences.size() > 0) {
            holder.examples.setText(Html.fromHtml(word.wordInASentences.get(holder.currentExampleIndex), Html.FROM_HTML_MODE_COMPACT));

            if (word.wordInASentences.size() > 1) {
                holder.examples.setOnClickListener(v -> {
                    holder.currentExampleIndex = (holder.currentExampleIndex + 1) % word.wordInASentences.size();
                    holder.examples.setText(Html.fromHtml(word.wordInASentences.get(holder.currentExampleIndex), Html.FROM_HTML_MODE_COMPACT));
                });
            }
        }

        // Set image
        for (String imageUrl : word.imagesUrl) {
            Bitmap bitmap = decodeSampledBitmap(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + imageUrl, ImageUtils.dipToPixels(mVocabularyListActivity, 288), ImageUtils.dipToPixels(mVocabularyListActivity, 144));
            holder.image.setImageBitmap(bitmap);
            holder.image.setOnClickListener(v -> ((VocabularyListActivity) mVocabularyListActivity).zoomImageFromThumb(holder.image, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + imageUrl));
        }

        // Set sound
        holder.sound.setOnClickListener(v -> {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                mMediaPlayer.setDataSource(mVocabularyListActivity, Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + word.soundsUrl.get(0)));
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mVocabularyListActivity);
            builder.setMessage("Are you sure you want to remove the card?")
                    .setNegativeButton("No", null).setPositiveButton("Yes", (dialog, which) -> {
                mWords.remove(position);
                notifyDataSetChanged();
                AnkiHelperApplication.writeWords();

                if (mWords.size() == 0) {
                    mVocabularyListActivity.findViewById(R.id.generateCardsButton).setEnabled(false);
                    mVocabularyListActivity.findViewById(R.id.clearButton).setEnabled(false);
                }
            }).show();
        });
    }

    public static Bitmap decodeSampledBitmap(String imagePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public int getItemCount() {
        return mWords.size();
    }
}