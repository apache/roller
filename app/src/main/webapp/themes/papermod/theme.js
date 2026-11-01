/*
  PaperMod v8+
  License: MIT https://github.com/adityatelange/hugo-PaperMod/blob/master/LICENSE
  Copyright (c) 2020 nanxiaobei and adityatelange
  Copyright (c) 2021-2026 adityatelange

  Ported to Apache Roller from layouts/_partials/head.html and footer.html.
  Loaded synchronously in <head> so the theme is applied before first paint.
*/
(function () {
    var html = document.documentElement;
    var darkQuery = window.matchMedia('(prefers-color-scheme: dark)');

    function storedTheme() {
        try {
            return localStorage.getItem('pref-theme');
        } catch (e) {
            return null;
        }
    }

    function applyTheme() {
        var pref = storedTheme();
        if (pref === 'dark' || pref === 'light') {
            html.dataset.theme = pref;
        } else {
            html.dataset.theme = darkQuery.matches ? 'dark' : 'light';
        }
    }

    applyTheme();

    // "auto": keep following the OS setting until the reader picks a theme
    darkQuery.addEventListener('change', function () {
        if (!storedTheme()) {
            applyTheme();
        }
    });

    document.addEventListener('DOMContentLoaded', function () {
        var toggle = document.getElementById('theme-toggle');
        if (toggle) {
            toggle.addEventListener('click', function () {
                var next = html.dataset.theme === 'dark' ? 'light' : 'dark';
                html.dataset.theme = next;
                try {
                    localStorage.setItem('pref-theme', next);
                } catch (e) {
                    // storage unavailable; the choice lasts for this page only
                }
            });
        }

        var menu = document.getElementById('menu');
        if (menu) {
            var scrollPosition = localStorage.getItem('menu-scroll-position');
            if (scrollPosition) {
                menu.scrollLeft = parseInt(scrollPosition, 10);
            }
            menu.addEventListener('scroll', function () {
                localStorage.setItem('menu-scroll-position', menu.scrollLeft);
            });
        }

        var topLink = document.getElementById('top-link');
        if (topLink) {
            window.addEventListener('scroll', function () {
                var scrollThreshold = window.innerHeight;
                if (document.body.scrollTop > scrollThreshold || document.documentElement.scrollTop > scrollThreshold) {
                    topLink.classList.remove('hidden');
                } else {
                    topLink.classList.add('hidden');
                }
            });
            topLink.addEventListener('click', function (e) {
                e.preventDefault();
                var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
                window.scrollTo({ top: 0, behavior: reduce ? 'auto' : 'smooth' });
                history.replaceState(null, '', ' ');
            });
        }
    });
})();
