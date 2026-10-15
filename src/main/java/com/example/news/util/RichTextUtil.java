
package com.example.news.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jsoup.nodes.Document;

public final class RichTextUtil {

    private RichTextUtil() {
        // Utility class; no instances.
    }

    // Define which HTML elements users may save.
    private static final Safelist ALLOWED_HTML =
            new Safelist()
                .addTags(
                    "p",
                    "br",
                    "strong",
                    "b",
                    "em",
                    "i",
                    "u",
                    "s",
                    "h2",
                    "h3",
                    "blockquote",
                    "ul",
                    "ol",
                    "li",
                    "a"
                )
                .addAttributes(
                    "a",
                    "href",
                    "title"
                )
                .addProtocols(
                    "a",
                    "href",
                    "http",
                    "https",
                    "mailto"
                )
                .addEnforcedAttribute(
                    "a",
                    "rel",
                    "nofollow noopener noreferrer"
                );

    public static String sanitize(String html) {

        if (html == null || html.isBlank()) {
            return "";
        }

        Document.OutputSettings settings =
                new Document.OutputSettings()
                    .prettyPrint(false);

        return Jsoup.clean(
                html,
                "",
                ALLOWED_HTML,
                settings
        );
    }

    public static String toPlainText(String html) {

        String safeHtml = sanitize(html);

        return Jsoup.parseBodyFragment(safeHtml)
                .text();
    }
}
