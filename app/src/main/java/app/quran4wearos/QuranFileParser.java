package app.quran4wearos;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class QuranFileParser {
    private final Context context;
    public QuranFileParser(Context context) { this.context = context; }

    public static class QuranDataMaps {
        public final Map<Integer, String> textMap = new HashMap<>();
        public final Map<Integer, String> emlaeyMap = new HashMap<>();
    }

    public QuranDataMaps loadAllTextData(String fileName) {
        QuranDataMaps maps = new QuranDataMaps();
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            Map<String, Integer> colMap = createHeaderMap(header);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] t = line.split(",", -1);
                int id = Integer.parseInt(getVal(t, colMap, "id"));
                maps.textMap.put(id, getVal(t, colMap, "aya_text"));
                maps.emlaeyMap.put(id, getVal(t, colMap, "aya_text_emlaey"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return maps;
    }

    public Map<Integer, QuranListEntry> loadMetadata(String fileName) {
        Map<Integer, QuranListEntry> entryMap = new HashMap<>();
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String header = reader.readLine();
            Map<String, Integer> colMap = createHeaderMap(header);
//            for (Map.Entry entry : colMap.entrySet()) {
////                System.out.println("["+entry.getKey()+"] : "+ entry.getValue());
//            }
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] t = line.split(",", -1);
//                System.out.print("Line: "+ line);
//                for (String val : t) {
//                    System.out.print(" ["+val+"]");
//                }
//                System.out.println();
                int id = Integer.parseInt(getVal(t, colMap, "id"));
                entryMap.put(id, new QuranListEntry(id,
                        Integer.parseInt(getVal(t, colMap, "jozz")),
                        Integer.parseInt(getVal(t, colMap, "page")),
                        Integer.parseInt(getVal(t, colMap, "sura_no")),
                        getVal(t, colMap, "sura_name_en"),
                        getVal(t, colMap, "sura_name_ar"),
                        Integer.parseInt(getVal(t, colMap, "aya_no")),
                        getVal(t, colMap, "type"),
                        null, null
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return entryMap;
    }

    private Map<String, Integer> createHeaderMap(String h) {
        Map<String, Integer> m = new HashMap<>();
        if (h == null) return m;
        String[] c = h.split(",");
        for (int i=0; i<c.length; i++) {
            String col = c[i].trim().toLowerCase();
            if (col.startsWith("\uFEFF")) {
                col = col.substring(1);
            }
            m.put(col, i);
        }
        return m;
    }
    private String getVal(String[] t, Map<String, Integer> m, String k) {
//        System.out.println("inside getVal() num map keys ="+ m.size());
//        System.out.println("K length = "+k.length());
//        for (Map.Entry entry : m.entrySet()) {
//            String entryKey = (String) entry.getKey();
//            System.out.println("["+ entryKey +"]->["+entry.getValue()+"] .. "+ entryKey.length()+" " +(k.equals(entryKey))+" " +m.get(entryKey));
//        }
//        System.out.println(k+"->"+m.get(k));
        Integer i = m.get(k);
        return (i != null && i < t.length) ? t[i] : null;
    }
}