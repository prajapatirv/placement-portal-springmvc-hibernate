// Architecture Demo tab: one iframe, two prebuilt pages. Switching a tab swaps the iframe and the full-screen link.
(function () {
  var frame = document.getElementById('demo-frame');
  var full = document.getElementById('demo-full');
  var tabs = document.querySelectorAll('.demo-tab');
  tabs.forEach(function (tab) {
    tab.addEventListener('click', function () {
      tabs.forEach(function (t) {
        var on = t === tab;
        t.setAttribute('aria-selected', on ? 'true' : 'false');
        t.classList.toggle('secondary', !on);
      });
      var src = tab.getAttribute('data-src');
      frame.src = src;
      frame.title = tab.textContent;
      full.href = src;
    });
  });
})();
