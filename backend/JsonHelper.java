import java.util.ArrayList;
import java.util.List;

/**
 * JsonHelper — Minimal JSON parsing and serialization using only the JDK.
 *
 * This is intentionally kept simple (no external libraries) and handles
 * only the exact JSON shapes used by this application.
 */
public class JsonHelper {

    // ========== Parsing ==========

    /**
     * Parse the request JSON and return two lists: lecture entries and lab entries.
     * Expected shape:
     * {
     *   "lecture": [ { "name": "...", "score": N, "totalScore": N, "percentage": N }, ... ],
     *   "laboratory": [ ... ]
     * }
     */
    public static List<List<ExamEntry>> parseRequest(String json) {
        List<ExamEntry> lecture = parseArray(extractArray(json, "lecture"));
        List<ExamEntry> lab = parseArray(extractArray(json, "laboratory"));

        List<List<ExamEntry>> result = new ArrayList<>();
        result.add(lecture);
        result.add(lab);
        return result;
    }

    /**
     * Extract the mode string from the request JSON.
     * Returns "both", "lecture", or "laboratory".
     */
    public static String parseMode(String json) {
        String mode = extractString(json, "mode");
        if (mode.isEmpty()) return "both";
        return mode;
    }

    /**
     * Extract the JSON array string for a given key.
     */
    private static String extractArray(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return "[]";

        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex == -1) return "[]";

        int bracketStart = json.indexOf('[', colonIndex);
        if (bracketStart == -1) return "[]";

        // Find matching closing bracket
        int depth = 0;
        for (int i = bracketStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(bracketStart, i + 1);
                }
            }
        }
        return "[]";
    }

    /**
     * Parse a JSON array of exam-entry objects.
     */
    private static List<ExamEntry> parseArray(String arrayJson) {
        List<ExamEntry> entries = new ArrayList<>();
        // Remove outer brackets
        String inner = arrayJson.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
        inner = inner.trim();
        if (inner.isEmpty()) return entries;

        // Split by top-level objects
        List<String> objects = splitObjects(inner);
        for (String obj : objects) {
            entries.add(parseExamEntry(obj));
        }
        return entries;
    }

    /**
     * Split a comma-separated list of JSON objects at the top level.
     */
    private static List<String> splitObjects(String s) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            else if (c == ',' && depth == 0) {
                parts.add(s.substring(start, i).trim());
                start = i + 1;
            }
        }
        String last = s.substring(start).trim();
        if (!last.isEmpty()) parts.add(last);
        return parts;
    }

    /**
     * Parse a single exam-entry JSON object.
     */
    private static ExamEntry parseExamEntry(String obj) {
        String name = extractString(obj, "name");
        double score = extractNumber(obj, "score");
        double totalScore = extractNumber(obj, "totalScore");
        double percentage = extractNumber(obj, "percentage");
        return new ExamEntry(name, score, totalScore, percentage);
    }

    /**
     * Extract a string value for a given key.
     */
    private static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return "";

        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex == -1) return "";

        // Find opening quote
        int quoteStart = json.indexOf('"', colonIndex + 1);
        if (quoteStart == -1) return "";

        // Find closing quote (handle escaped quotes)
        int quoteEnd = quoteStart + 1;
        while (quoteEnd < json.length()) {
            if (json.charAt(quoteEnd) == '"' && json.charAt(quoteEnd - 1) != '\\') break;
            quoteEnd++;
        }
        return json.substring(quoteStart + 1, quoteEnd);
    }

    /**
     * Extract a numeric value for a given key.
     */
    private static double extractNumber(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return 0;

        int colonIndex = json.indexOf(':', keyIndex + search.length());
        if (colonIndex == -1) return 0;

        // Skip whitespace
        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        // Read until non-numeric character
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) break;
            end++;
        }

        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ========== Serialization ==========

    /**
     * Build the response JSON string.
     */
    public static String buildResponse(String mode, double lectureGrade, double labGrade, double finalGrade) {
        StringBuilder sb = new StringBuilder("{");
        if ("both".equals(mode)) {
            sb.append("\"lectureGrade\":").append(round(lectureGrade)).append(",");
            sb.append("\"labGrade\":").append(round(labGrade)).append(",");
            sb.append("\"finalGrade\":").append(round(finalGrade));
        } else if ("lecture".equals(mode)) {
            sb.append("\"lectureGrade\":").append(round(lectureGrade));
        } else if ("laboratory".equals(mode)) {
            sb.append("\"labGrade\":").append(round(labGrade));
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Build an error response JSON.
     */
    public static String buildError(String message) {
        // Escape quotes in message
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"error\":\"" + escaped + "\"}";
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
