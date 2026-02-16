package app.quran4wearos;

import android.content.Context;
import java.io.*;
import java.util.*;

public class QuranFileParser {
    private final Context context;

    public QuranFileParser(Context context) {
        this.context = context;
    }

    private Map<String, Integer> createHeaderMap(String h) {
        Map<String, Integer> m = new HashMap<>();
        if (h == null) return m;
        String[] c = h.split(",");
        for (int i = 0; i < c.length; i++) {
            String col = c[i].trim().toLowerCase();
            // Handle UTF-8 BOM if present
            if (col.startsWith("\uFEFF")) {
                col = col.substring(1);
            }
            m.put(col, i);
        }
        return m;
    }

    private String getVal(String[] row, Map<String, Integer> header, String colName) {
        Integer index = header.get(colName.toLowerCase());
        return (index != null && index < row.length) ? row[index].trim() : "";
    }

    private int getInt(String[] row, Map<String, Integer> header, String colName) {
        String val = getVal(row, header, colName);
        try {
            return val.isEmpty() ? 0 : Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 1. Quran_sura_names.csv (sura_no, sura_name_en, sura_name_ar)
    public Map<Integer, SuraName> loadSuraNames() {
        Map<Integer, SuraName> map = new HashMap<>();
        try (BufferedReader r = getReader("Quran_sura_names.csv")) {
            Map<String, Integer> header = createHeaderMap(r.readLine());
            String line;
            while ((line = r.readLine()) != null) {
                String[] t = line.split(",");
                int no = getInt(t, header, "sura_no");
                map.put(no, new SuraName(
                        no,
                        getVal(t, header, "sura_name_en"),
                        getVal(t, header, "sura_name_ar")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // 2. Quran_entryPoints_hafs_smart_v8.csv
    // (id, jozz, sura_no, page, line_start, line_end, aya_no, aya_text, aya_text_emlaey, type)
    public List<QuranListEntry> loadEntryPoints() {
        List<QuranListEntry> list = new ArrayList<>();
        try (BufferedReader r = getReader("Quran_entryPoints_hafs_smart_v8.csv")) {
            Map<String, Integer> header = createHeaderMap(r.readLine());
            String line;
            while ((line = r.readLine()) != null) {
                String[] t = line.split(",", -1);
                list.add(new QuranListEntry(
                        getInt(t, header, "id"),
                        getInt(t, header, "jozz"),
                        getInt(t, header, "sura_no"),
                        getInt(t, header, "page"),
                        getInt(t, header, "line_start"), // New
                        getInt(t, header, "line_end"),   // New
                        getInt(t, header, "aya_no"),
                        getVal(t, header, "type"), "", ""
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. Quran_metadata_hafs_smart_v8.csv (id, jozz, sura_no, page, line_start, line_end, aya_no)
    public Map<Integer, QuranListEntry> loadMetadata() {
        Map<Integer, QuranListEntry> map = new HashMap<>();
        try (BufferedReader r = getReader("Quran_metadata_hafs_smart_v8.csv")) {
            Map<String, Integer> header = createHeaderMap(r.readLine());
            String line;
            while ((line = r.readLine()) != null) {
                String[] t = line.split(",", -1);
                int id = getInt(t, header, "id");
                map.put(id, new QuranListEntry(
                        id,
                        getInt(t, header, "jozz"),
                        getInt(t, header, "sura_no"),
                        getInt(t, header, "page"),
                        getInt(t, header, "line_start"), // New
                        getInt(t, header, "line_end"),   // New
                        getInt(t, header, "aya_no"),
                        "A", // Default type for metadata
                        "", ""
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // 4. Quran_text_hafs_smart_v8.csv (id, aya_text, aya_text_emlaey)
    public Map<Integer, String[]> loadTextData() {
        Map<Integer, String[]> map = new HashMap<>();
        try (BufferedReader r = getReader("Quran_text_hafs_smart_v8.csv")) {
            Map<String, Integer> header = createHeaderMap(r.readLine());
            String line;
            while ((line = r.readLine()) != null) {
                // Using -1 in split to ensure empty trailing columns are preserved
                String[] t = line.split(",", -1);
                int id = getInt(t, header, "id");
                map.put(id, new String[]{
                        getVal(t, header, "aya_text"),
                        getVal(t, header, "aya_text_emlaey")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    private BufferedReader getReader(String f) throws IOException {
        return new BufferedReader(new InputStreamReader(context.getAssets().open(f), "UTF-8"));
    }
}