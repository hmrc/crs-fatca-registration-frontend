// =====================================================
// Back link mimics browser back functionality
// =====================================================
// store referrer value to cater for IE - https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/10474810/  */
var docReferrer = document.referrer
// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
    window.history.replaceState(null, null, window.location.href);
}

// handle back click
var backLink = document.querySelector('.govuk-back-link');
if (backLink !== null) {
    backLink.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        window.history.back();
    });
}

// Find first ancestor of el with tagName
// or undefined if not found
function upTo(el, tagName) {
    tagName = tagName.toLowerCase();

    while (el && el.parentNode) {
        el = el.parentNode;
        if (el.tagName && el.tagName.toLowerCase() == tagName) {
            return el;
        }
    }

    return null;
}

var countryySelect = document.querySelector('select#country');
if (countrySelect !== null) {
    var options = countrySelect.querySelectorAll("option");
    for (var i = 0; i < options.length; i++) {
        var option = options[i];
        var dataText = option.getAttribute('data-text');
        if (dataText) {
            option.text = dataText;
        }
    }
    setTimeout(function(){
        HMRCAccessibleAutocomplete.enhanceSelectElement({
            defaultValue: '',
            selectElement: countrySelect,
            showAllValues: true,
            autoSelect: false,
            templates: {
                suggestion: function (suggestion) {
                    if (suggestion) {
                        return suggestion.split(':')[0];
                    }
                    return suggestion;
                },
                inputValue: function (suggestion) {
                    if (suggestion) {
                        return suggestion.split(':')[0];
                    }
                    return suggestion;
                }
            }
        });
    }, 100)
}

