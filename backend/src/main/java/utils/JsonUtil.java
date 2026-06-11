package utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;

public class JsonUtil {

    public static String readBody(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    public static String getString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        if (idx >= json.length() || json.charAt(idx) != '"') return null;
        idx++;
        StringBuilder value = new StringBuilder();
        while (idx < json.length() && json.charAt(idx) != '"') {
            if (json.charAt(idx) == '\\') idx++;
            value.append(json.charAt(idx++));
        }
        return value.toString();
    }

    public static Integer getInt(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (end == idx) return null;
        return Integer.parseInt(json.substring(idx, end));
    }

    public static Double getDouble(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && Character.isWhitespace(json.charAt(idx))) idx++;
        int end = idx;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '.')) end++;
        if (end == idx) return null;
        return Double.parseDouble(json.substring(idx, end));
    }

    public static void send(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        PrintWriter out = resp.getWriter();
        out.write(json);
        out.flush();
    }

    public static String error(String message) {
        return "{\"erro\":\"" + escape(message) + "\"}";
    }

    public static String success(String message) {
        return "{\"mensagem\":\"" + escape(message) + "\"}";
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
