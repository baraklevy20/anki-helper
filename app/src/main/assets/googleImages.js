(
	function () {
        document.addEventListener('click', function (e) {
            //if (e.target.getAttribute(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && o[1][2][0] == e.target.getAttribute('data-src'))) == null)
            // This gets the entire source code
            //Android.catchHref(document.documentElement.outerHTML);

//            console.log("barak");
//            console.log(AF_initDataChunkQueue);
//            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1"));
//            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data());
//            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10]);
//            console.log(e.target.outerHTML);
//            console.log(e.target.getAttribute('data-src'));
            var imageLink = e.target.getAttribute('data-iurl');

            if (!imageLink) {
                imageLink = e.target.getAttribute('data-src');
            }

            imageLink = unescape(imageLink);
            console.log(imageLink);
            //console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && unescape(e.target.getAttribute('data-src')).indexOf(unescape(o[1][2][0])) >= 0));
//            console.log(e.target.getAttribute(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && o[1][2][0] == e.target.getAttribute('data-iurl'))[1][3][0]);
//
//            console.log("barakend");

            Android.catchHref(AF_initDataChunkQueue.
                find(o => o.key == "ds:1").data()[10].
                find(o => o[1] != null && o[1][2] != null && imageLink.indexOf(unescape(o[1][2][0])) >= 0)
                [1][3][0]);
        });
    }
)();