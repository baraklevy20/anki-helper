package levy.barak.ankihelper.anki;

/**
 * Created by baraklev on 2/25/2018.
 */

public class Sentence {
    public long id;
    public String[] words;
    public boolean[] isWordUsed;

    public Sentence() {
        this.id = (long)(Math.random() * Long.MAX_VALUE);
    }

    public int getFirstBlank() {
        for (int i = 0; i < words.length; i++) {
            if (!isWordUsed[i]) {
                return i;
            }
        }

        return 0;
    }

    public String getFullSentence() {
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            builder.append(word + " ");
        }

        return builder.toString().trim();
    }

    public String getBlankedSentence(int index) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i != index) {
                builder.append(words[i] + " ");
            }
            else {
                for (int j = 0; j < words[index].length(); j++) {
                    builder.append("_");
                }

                builder.append(" ");
            }
        }

        return builder.toString().trim();
    }
}
