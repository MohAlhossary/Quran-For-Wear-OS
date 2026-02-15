package app.quran4wearos;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuranFileParser {
    private final Context context;

    private static final String COL_ID = "id";
    private static final String COL_JOZZ = "jozz";
    private static final String COL_SURA_NO = "sura_no";
    private static final String COL_SURA_NAME = "sura_name_en";
    private static final String COL_AYA_NO = "aya_no";
    private static final String COL_TEXT = "aya_text_emlaey";
    private static final String COL_TYPE = "type";

    public QuranFileParser(Context context) {
        this.context = context;
    }

    public List<QuranEntry> loadData(String fileName) {
        List<QuranEntry> entries = new ArrayList<>();
        Map<String, Integer> colMap = new HashMap<>();

        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return entries;

            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                colMap.put(headers[i].trim(), i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",", -1);
                String type = getVal(tokens, colMap, COL_TYPE).trim();

                if (type.equals("S") || type.equals("J")) {
                    entries.add(new QuranEntry(
                            parseInt(getVal(tokens, colMap, COL_ID)),
                            parseInt(getVal(tokens, colMap, COL_JOZZ)),
                            parseInt(getVal(tokens, colMap, COL_SURA_NO)),
                            getVal(tokens, colMap, COL_SURA_NAME),
                            parseInt(getVal(tokens, colMap, COL_AYA_NO)),
                            getVal(tokens, colMap, COL_TEXT),
                            type
                    ));
                }
            }
        } catch (IOException e) {
            Log.e("QuranFileParser", "Error reading CSV", e);
        }
        return entries;
    }

    private String getVal(String[] tokens, Map<String, Integer> map, String key) {
        Integer index = map.get(key);
        return (index != null && index < tokens.length) ? tokens[index] : "";
    }

    private int parseInt(String val) {
        try {
            return (val == null || val.isEmpty()) ? 0 : Integer.parseInt(val.trim());
        } catch (NumberFormatException e) { return 0; }
    }
}