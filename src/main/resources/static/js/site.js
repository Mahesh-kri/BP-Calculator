// BP Calculator - client side behaviour
(function () {
    'use strict';

    var root = document.documentElement;
    var toggle = document.getElementById('themeToggle');

    function currentTheme() {
        return root.getAttribute('data-bs-theme') || 'light';
    }

    function applyTheme(theme) {
        root.setAttribute('data-bs-theme', theme);
        try {
            localStorage.setItem('bp-theme', theme);
        } catch (e) {
            /* storage unavailable - ignore */
        }
        if (toggle) {
            toggle.setAttribute('aria-pressed', theme === 'dark' ? 'true' : 'false');
        }
    }

    if (toggle) {
        // sync button state with the theme set by the inline head script
        toggle.setAttribute('aria-pressed', currentTheme() === 'dark' ? 'true' : 'false');
        toggle.addEventListener('click', function () {
            applyTheme(currentTheme() === 'dark' ? 'light' : 'dark');
        });
    }
})();
