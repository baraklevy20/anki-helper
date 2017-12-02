(
	function () {
        document.addEventListener('click', function (e) {
            var currentNode = e.target;

            while (currentNode.className.split(' ').indexOf('gt-baf-entry-clickable') < 0)
                currentNode = currentNode.parentNode;

            var result = currentNode.firstChild.firstChild.childNodes[0].innerHTML;

            if (currentNode.firstChild.firstChild.childNodes.length == 2)
                result += ' ' + currentNode.firstChild.firstChild.childNodes[1].innerHTML;

            Android.catchGermanWord(result);
        });
    }
)();