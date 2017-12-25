package levy.barak.ankihelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import levy.barak.ankihelper.utils.ImageUtils;

/**
 * Created by baraklev on 12/25/2017.
 */

public class CardsListAdapter extends RecyclerView.Adapter<CardsListAdapter.DataObjectHolder> {
    private ArrayList<Word> mWords;
    private Context mContext;

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        private TextView word;
        private ImageView image;
        private TextView ipa;
        private ImageButton sound;

        public DataObjectHolder(View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.word_list_word);
            image = itemView.findViewById(R.id.word_list_image);
            ipa = itemView.findViewById(R.id.word_list_ipa);
            sound = itemView.findViewById(R.id.word_list_sound);
        }
    }

    public CardsListAdapter(Context context, ArrayList<Word> myDataset) {
        mWords = myDataset;
        mContext = context;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
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

        // Set image
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/anki_helper/" + word.imagesUrl.get(0));
        Bitmap bt = Bitmap.createScaledBitmap(bitmap, ImageUtils.dipToPixels(mContext, 150), ImageUtils.dipToPixels(mContext, 100), true);
        holder.image.setImageBitmap(bt);

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

    @Override
    public int getItemCount() {
        return mWords.size();
    }
}