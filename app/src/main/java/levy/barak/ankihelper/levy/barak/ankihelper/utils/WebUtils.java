package levy.barak.ankihelper.levy.barak.ankihelper.utils;

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

public class WebUtils {
    public static String getJavascript(Context context, String jsFileName) {
        try {
            List<String> doc = new BufferedReader(new InputStreamReader(context.getAssets().open(jsFileName),
                    StandardCharsets.UTF_8)).lines().collect(Collectors.toList());

            return String.join("\n", doc);
        } catch (IOException e) {
            Toast.makeText(context, "There was an error reading the JS files", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        return null;
    }
}
