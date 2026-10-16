
(function () {
    "use strict";

    function initializePreview() {

        const title = document.getElementById("title");
        const category = document.getElementById("category");
        const content = document.getElementById("content");

        const previewTitle =
            document.getElementById("previewTitle");

        const previewCategory =
            document.getElementById("previewCategory");

        const previewContent =
            document.getElementById("previewContent");

        if (!title || !category || !content ||
            !previewTitle || !previewCategory ||
            !previewContent) {

            console.error(
                "NewsWeb Preview: Required elements not found."
            );
            return;
        }

        function updatePreview() {

            previewTitle.textContent =
                title.value.trim() || "Your article title";

            previewCategory.textContent =
                category.value.trim() || "Category";

            previewContent.textContent =
                content.value ||
                "Your article content will appear here.";
        }

        title.addEventListener("input", updatePreview);
        category.addEventListener("input", updatePreview);
        content.addEventListener("input", updatePreview);

        // Important for editing previously saved drafts.
        updatePreview();

        console.log("NewsWeb Live Preview is working!");
    }

    if (document.readyState === "loading") {
        document.addEventListener(
            "DOMContentLoaded", initializePreview
        );
    } else {
        initializePreview();
    }

})();
