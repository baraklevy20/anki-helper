(
	function () {
	    //udocument.body.innerHTML = document.body.innerHTML.replace(/gt-baf-entry-clickable/g, 'gt-baf-entry-clickable-disabled')

        document.addEventListener('click', function (e) {
            var currentNode = e.target;
            var word = null;
            var wordType = null;

            while (currentNode != null && currentNode.className != null && currentNode.className.split(' ').indexOf('result') < 0) {
                currentNode = currentNode.parentNode;
            }

            if (currentNode != null && currentNode.className != null) {
                word = currentNode.childNodes[1].firstChild.getElementsByClassName('translation')[0].innerHTML;

                // Search for the word type
                var allWords = document.getElementsByClassName('gt-baf-entry-clickable');
                var found;

                // Find the element
                for (var i = 0; i < allWords.length; i++) {
                    var splitWord = allWords[i].firstChild.firstChild.childNodes;

                    for (var j = 0; j < splitWord.length; j++) {
                        if (splitWord[j].innerHTML.toLowerCase() === word.toLowerCase()) {
                            found = allWords[i];
                            break;
                        }
                    }
                }

                // The word may not always be found. e.g. if you search for "The dog"
                if (found) {
                    wordType = found.parentNode.previousSibling.innerText;
                }
            } else {
                currentNode = e.target;

                while (currentNode.className != null && currentNode.className.split(' ').indexOf('gt-baf-entry-clickable') < 0) {
                    currentNode = currentNode.parentNode;
                }

                // Get word
                word = currentNode.firstChild.firstChild.firstChild.innerHTML;

                if (currentNode.firstChild.firstChild.childNodes.length == 2) {
                    word += ' ' + currentNode.firstChild.firstChild.childNodes[1].innerHTML;
                }

                // Get word type
                wordType = currentNode.parentNode.previousSibling.innerText;
            }

            Android.catchGermanWord(word, wordType);
        });
    }
)();