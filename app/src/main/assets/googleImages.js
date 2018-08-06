(
	function () {
        document.addEventListener('click', function (e) {
            console.log("im in click event")
            if (AF_initDataChunkQueue == null || AF_initDataChunkQueue == undefined || AF_initDataChunkQueue == "") {
                Android.clearCache();
                return;
            }
            var imageLink = e.target.getAttribute('data-iurl');

            if (!imageLink) {
                imageLink = e.target.getAttribute('data-src');
            }

            imageLink = unescape(imageLink);
            console.log(imageLink)
            console.log(AF_initDataChunkQueue)
            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1"))
            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10])
            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && imageLink.indexOf(unescape(o[1][2][0])) >= 0))
            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && imageLink.indexOf(unescape(o[1][2][0])) >= 0)[1][3][0])

            Android.catchHref(AF_initDataChunkQueue.
                find(o => o.key == "ds:1").data()[10].
                find(o => o[1] != null && o[1][2] != null && imageLink.indexOf(unescape(o[1][2][0])) >= 0)
                [1][3][0]);
        });
    }
)();