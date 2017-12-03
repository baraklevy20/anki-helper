(
	function () {
	    //udocument.body.innerHTML = document.body.innerHTML.replace(/gt-baf-entry-clickable/g, 'gt-baf-entry-clickable-disabled')

        document.addEventListener('click', function (e) {
            var currentNode = e.target;
            var result = null;

            while (currentNode != null && currentNode.className != null && currentNode.className.split(' ').indexOf('result') < 0) {
                currentNode = currentNode.parentNode;
            }

            if (currentNode != null && currentNode.className != null) {
                result = currentNode.childNodes[1].firstChild.firstChild.innerHTML;
            } else {
                currentNode = e.target;

                while (currentNode.className != null && currentNode.className.split(' ').indexOf('gt-baf-entry-clickable') < 0) {
                    currentNode = currentNode.parentNode;
                }

                result = currentNode.firstChild.firstChild.firstChild.innerHTML;

                if (currentNode.firstChild.firstChild.childNodes.length == 2)
                    result += ' ' + currentNode.firstChild.firstChild.childNodes[1].innerHTML;
            }


            Android.catchGermanWord(result);
        });
    }
)();