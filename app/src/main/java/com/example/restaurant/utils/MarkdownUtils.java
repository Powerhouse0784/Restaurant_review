package com.example.restaurant.utils;

import io.noties.markwon.Markwon;

public class MarkdownUtils {
    public static String markdownToHtml(String markdown) {
        // Simple markdown to HTML conversion (for more complex needs, use a library)
        if (markdown == null) return "";

        // Replace basic markdown syntax with HTML
        String html = markdown
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>") // bold
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>") // italic
                .replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>") // links
                .replaceAll("^#\\s(.*?)$", "<h1>$1</h1>") // h1
                .replaceAll("^##\\s(.*?)$", "<h2>$1</h2>") // h2
                .replaceAll("\n", "<br>"); // line breaks

        return html;
    }
}