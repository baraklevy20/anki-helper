(
	function () {
        document.addEventListener('click', function (e) {
            // This gets the entire source code
            //Android.catchHref(document.documentElement.outerHTML);

            var imageLink = e.target.getAttribute('data-iurl');

            if (!imageLink) {
                imageLink = e.target.getAttribute('data-src');
            }

            imageLink = unescape(imageLink);

            Android.catchHref(AF_initDataChunkQueue.
                find(o => o.key == "ds:1").data()[10].
                find(o => o[1] != null && o[1][2] != null && imageLink.indexOf(unescape(o[1][2][0])) >= 0)
                [1][3][0]);
        });
    }
)();