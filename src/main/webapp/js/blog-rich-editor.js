
(function () {
    "use strict";

    function initializeEditor() {

        const editorElement =
            document.getElementById("richEditor");

        const contentField =
            document.getElementById("content");

        const htmlField =
            document.getElementById("contentHtml");

        const previewContent =
            document.getElementById("previewContent");

        const previewTitle =
            document.getElementById("previewTitle");

        const previewCategory =
            document.getElementById("previewCategory");

        const title = document.getElementById("title");
        const category = document.getElementById("category");

        if (!editorElement || !contentField || !htmlField) {
            return;
        }

        if (typeof Quill === "undefined") {
            console.error("Quill library missing.");
            return;
        }

        // Used when reopening an existing draft.
        const initialHtmlElement =
            document.getElementById("initialContentHtml");

        const oldText = contentField.value;

        const quill = new Quill(editorElement, {
            theme: "snow",
            placeholder: "Write your blog here...",

            modules: {
                toolbar: [
                    ["bold", "italic", "underline", "strike"],
                    [{ header: 2 }, { header: 3 }],
                    [{ list: "ordered" }, { list: "bullet" }],
                    ["blockquote", "link"],
                    ["clean"]
                ]
            },

            formats: [
                "bold", "italic", "underline", "strike",
                "header", "list", "blockquote", "link"
            ]
        });

        if (initialHtmlElement &&
            initialHtmlElement.value.trim()) {

            // Previously saved HTML has been sanitized
            // by the Java backend.
            quill.clipboard.dangerouslyPasteHTML(
                initialHtmlElement.value,
                "silent"
            );

        } else if (oldText) {

            // Existing plain-text blogs.
            quill.setText(oldText, "silent");
        }

        editorElement.hidden = false;
        contentField.hidden = true;
        contentField.required = false;

        function updatePreview() {

            if (title && previewTitle) {
                previewTitle.textContent =
                    title.value.trim() || "Your blog title";
            }

            if (category && previewCategory) {
                previewCategory.textContent =
                    category.value.trim() || "Category";
            }

            if (previewContent) {

                // Copy the rendered Quill content.
                // No untrusted HTML parsing is needed.
                const clonedNodes = Array.from(
                    quill.root.childNodes,
                    node => node.cloneNode(true)
                );

                previewContent.replaceChildren(
                    ...clonedNodes
                );
            }
        }

        function syncEditor() {

            contentField.value =
                quill.getText().trim();

            htmlField.value =
                quill.getSemanticHTML();

            updatePreview();
        }

        quill.on("text-change", syncEditor);

        if (title) {
            title.addEventListener(
                "input", updatePreview
            );
        }

        if (category) {
            category.addEventListener(
                "input", updatePreview
            );
        }

        const form = editorElement.closest("form");

        if (form) {
            form.addEventListener("submit", syncEditor);
        }

        syncEditor();

        console.log("Quill preview and saving initialized.");
    }

    if (document.readyState === "loading") {
        document.addEventListener(
            "DOMContentLoaded", initializeEditor
        );
    } else {
        initializeEditor();
    }

})();
