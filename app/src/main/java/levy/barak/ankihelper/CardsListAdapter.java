package levy.barak.ankihelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import levy.barak.ankihelper.utils.ImageUtils;

/**
 * Created by baraklev on 12/25/2017.
 */

public class CardsListAdapter extends RecyclerView.Adapter<CardsListAdapter.DataObjectHolder> {
    private ArrayList<Word> mWords;
    private Context mContext;

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        private TextView word;
        private TextView ipa;
        private ImageButton sound;
        private LinearLayout imagesListLayout;

        public DataObjectHolder(View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.word_list_word);
            ipa = itemView.findViewById(R.id.word_list_ipa);
            sound = itemView.findViewById(R.id.word_list_sound);
            imagesListLayout = itemView.findViewById(R.id.word_images_list);
        }
    }

    public CardsListAdapter(Context context, ArrayList<Word> myDataset) {
        mWords = myDataset;
        mContext = context;
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
        holder.word.setText(word.germanWord);
        holder.ipa.setText(word.ipa);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        holder.imagesListLayout.removeAllViews();

        // Set image
        for (String imageUrl : word.imagesUrl) {
            //BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = true;
            Bitmap bitmap = decodeSampledBitmap(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + imageUrl, ImageUtils.dipToPixels(mContext, 48), ImageUtils.dipToPixels(mContext, 48));

            CircleImageView imageView = (CircleImageView) inflater.inflate(R.layout.card_image_view, holder.imagesListLayout, false);
            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(v -> ((TranslateActivity) mContext).zoomImageFromThumb(imageView, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + imageUrl));

            holder.imagesListLayout.addView(imageView);
        }

        // Set sound
        holder.sound.setOnClickListener(v -> {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                mediaPlayer.setDataSource(mContext, Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + word.soundsUrl.get(0)));
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
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