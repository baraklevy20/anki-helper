(
	function () {
        document.addEventListener('click', function (e) {
            // This gets the entire source code
//            Android.catchHref(document.documentElement.outerHTML);

//            console.log("barak");
//            console.log(AF_initDataChunkQueue);
//            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1"));
//            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data());
//            console.log(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10]);
//            console.log(e.target.getAttribute('data-iurl'));
//            console.log(e.target.getAttribute(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && o[1][2][0] == e.target.getAttribute('data-iurl'))));
//            console.log(e.target.getAttribute(AF_initDataChunkQueue.find(o => o.key == "ds:1").data()[10].find(o => o[1] != null && o[1][2] != null && o[1][2][0] == e.target.getAttribute('data-iurl'))[1][3][0]);
//
//            console.log("barakend");

            Android.catchHref(AF_initDataChunkQueue.
                find(o => o.key == "ds:1").data()[10].
                find(o => o[1] != null && o[1][2] != null && o[1][2][0] == e.target.getAttribute('data-iurl'))
                [1][3][0]);
        });
    }
)();