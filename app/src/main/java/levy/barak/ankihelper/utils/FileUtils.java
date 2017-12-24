package levy.barak.ankihelper.utils;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by baraklev on 12/2/2017.
 */

public class FileUtils {
    public static String getFileContent(Context context, String fileName, String delimiter) {
        try {
            List<String> doc = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName),
                    StandardCharsets.UTF_8)).lines().collect(Collectors.toList());

            return String.join(delimiter, doc);
        } catch (IOException e) {
            Toast.makeText(context, "There was an error reading the file", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return null;
    }

    public static String getFileContent(Context context, String fileName) {
        return getFileContent(context, fileName, "\n");
    }
}
