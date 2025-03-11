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

// This fixes error styling on accessible autocomplete for country selection
// Everything below should be added to play-frontend-hmrc eventually and then this can all be removed
//
function updateAccessibleAutocompleteStyling(originalSelect) {
    // =====================================================
    // Polyfill autocomplete once loaded
    // =====================================================
    var checkForLoad = setInterval(checkForAutocompleteLoad, 50);
    var parentForm = upTo(originalSelect, 'form');

    function polyfillAutocomplete() {
        var combo = parentForm.querySelector('[role="combobox"]');

        // =====================================================
        // Update autocomplete once loaded with fallback's aria attributes
        // Ensures hint and error are read out before usage instructions
        // =====================================================
        if (originalSelect && originalSelect.getAttribute('aria-describedby') > "") {
            if (parentForm) {
                if (combo) {
                    combo.setAttribute('aria-describedby', originalSelect.getAttribute('aria-describedby') + ' ' + combo.getAttribute('aria-describedby'));
                }
            }
        }
        if (originalSelect && originalSelect.getAttribute('autocomplete') > "") {
            if (parentForm) {
                if (combo) {
                    combo.setAttribute('autocomplete', originalSelect.getAttribute('autocomplete'));
                }
            }
        }
        // =====================================================
        // Update autocomplete once loaded with error styling if needed
        // This won't work if the autocomplete css is loaded after the frontend library css because
        // the autocomplete's border will override the error class's border (they are both the same specificity)
        // but we can use the class assigned to build a more specific rule
        // =====================================================
        setErrorClass();

        function setErrorClass() {
            if (originalSelect && originalSelect.classList.contains("govuk-select--error")) {
                if (parentForm) {
                    if (combo) {
                        combo.classList.add("govuk-input--error");
                        // Also set up an event listener to check for changes to input so we know when to repeat the copy
                        combo.addEventListener('focus', function () {
                            setErrorClass()
                        });
                        combo.addEventListener('blur', function () {
                            setErrorClass()
                        });
                        combo.addEventListener('change', function () {
                            setErrorClass()
                        });
                    }
                }
            }
        }

        // =====================================================
        // Ensure when user replaces valid answer with a non-valid answer, then valid answer is not retained
        // =====================================================
        var holdSubmit = true;
        parentForm.addEventListener('submit', function (e) {
            if (holdSubmit) {
                e.preventDefault()
                if (originalSelect.querySelectorAll('[selected]').length > 0 || originalSelect.value > "") {

                    var resetSelect = false;

                    if (originalSelect.value) {
                        if (combo.value != originalSelect.querySelector('option[value="' + originalSelect.value + '"]').text) {
                            resetSelect = true;
                        }
                    }
                    if (resetSelect) {
                        originalSelect.value = "";
                        if (originalSelect.querySelectorAll('[selected]').length > 0) {
                            originalSelect.querySelectorAll('[selected]')[0].removeAttribute('selected');
                        }
                    }
                }

                holdSubmit = false;
                //parentForm.submit();
                HTMLFormElement.prototype.submit.call(parentForm); // because submit buttons have id of "submit" which masks the form's natural form.submit() function
            }
        })

    }

    function checkForAutocompleteLoad() {
        if (parentForm.querySelector('[role="combobox"]')) {
            clearInterval(checkForLoad)
            polyfillAutocomplete();
        }
    }
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

var countrySelect = document.querySelector('select#country');
if (countrySelect !== null) {
    HMRCAccessibleAutocomplete.enhanceSelectElement({
        defaultValue: '',
        selectElement: countrySelect,
        showAllValues: true,
        autoSelect: false,
        preserveNullOptions: true,
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

    updateAccessibleAutocompleteStyling(countrySelect);
}
