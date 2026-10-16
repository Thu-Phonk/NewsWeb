document.addEventListener("DOMContentLoaded", function () {
    const input = document.getElementById("coverImage");
    const preview = document.getElementById("coverPreview");
    const remove = document.getElementById("removeCover");
    if (!input || !preview) return;

    const originalUrl = preview.getAttribute("src") || "";
    let objectUrl = null;

    function render() {
        if (objectUrl) {
            URL.revokeObjectURL(objectUrl);
            objectUrl = null;
        }

        let file = input.files[0];
        if (file && (!['image/jpeg', 'image/png'].includes(file.type) ||
                     file.size > 5 * 1024 * 1024)) {
            alert('Choose a JPEG or PNG smaller than 5 MB.');
            input.value = '';
            file = null;
        }
        if (file) {
            if (remove) remove.checked = false;
            objectUrl = URL.createObjectURL(file);
            preview.src = objectUrl;
            preview.hidden = false;
        } else if (remove && remove.checked) {
            preview.hidden = true;
            preview.removeAttribute('src');
        } else if (originalUrl) {
            preview.src = originalUrl;
            preview.hidden = false;
        } else {
            preview.hidden = true;
            preview.removeAttribute('src');
        }
    }

    input.addEventListener('change', render);
    if (remove) remove.addEventListener('change', function () {
        if (remove.checked) input.value = '';
        render();
    });
    window.addEventListener('pagehide', function () {
        if (objectUrl) URL.revokeObjectURL(objectUrl);
    });
});
