(
	function () {
        document.addEventListener('click', function (e) {
            Android.catchHref(e.target.parentNode.getAttribute('href'));
        });
    }
)();