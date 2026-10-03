// Session examples: Run buttons, live request log, keyboard navigation, zoom.
(function () {
  var base = document.querySelector('link[rel=stylesheet]').getAttribute('href').replace(/css\/app\.css.*$/, '');

  function esc(s) {
    return String(s).replace(/[&<>"]/g, function (c) { return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c]; });
  }

  // ---- request log ----
  function refreshLog() {
    fetch(base + 'examples/requests').then(function (r) { return r.json(); }).then(function (rows) {
      var body = document.getElementById('log-body');
      if (!body) return;
      if (!rows.length) { body.innerHTML = '<tr><td colspan="5" class="muted">No requests yet. Press Run.</td></tr>'; return; }
      body.innerHTML = rows.map(function (r) {
        var cls = r.status >= 500 ? 'bad' : r.status >= 400 ? 'warn' : 'ok';
        var sqlCls = r.sql >= 10 ? 'bad' : '';
        return '<tr><td>' + esc(r.time) + '</td><td><code>' + esc(r.method + ' ' + r.uri) + '</code></td>' +
          '<td class="' + cls + '">' + r.status + '</td><td class="' + sqlCls + '"><b>' + r.sql + '</b></td><td>' + r.ms + '</td></tr>';
      }).join('');
    }).catch(function () {});
  }

  // ---- run buttons ----
  document.querySelectorAll('.ex-run-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var out = document.getElementById('result-' + btn.dataset.idx);
      var opts = {method: btn.dataset.method, headers: {}};
      if (btn.dataset.body) { opts.body = btn.dataset.body; opts.headers['Content-Type'] = btn.dataset.ctype; }
      out.hidden = false;
      out.textContent = 'Running...';
      fetch(base + btn.dataset.url.replace(/^\//, ''), opts).then(function (resp) {
        var location = resp.headers.get('Location');
        return resp.text().then(function (text) {
          var shown = text;
          try { shown = JSON.stringify(JSON.parse(text), null, 2); } catch (e) { shown = text.replace(/\s+/g, ' ').slice(0, 300); }
          var ok = String(resp.status) === btn.dataset.status;
          out.innerHTML = '<b class="' + (ok ? 'ok' : 'bad') + '">HTTP ' + resp.status + '</b> ' +
            (ok ? '(as expected)' : '(expected ' + btn.dataset.status + ')') +
            (location ? ' &middot; Location: <code>' + esc(location) + '</code> <button type="button" class="danger" id="undo-' + btn.dataset.idx + '">Undo</button>' : '') +
            '<pre>' + esc(shown.slice(0, 2500)) + '</pre>';
          if (location) {
            document.getElementById('undo-' + btn.dataset.idx).addEventListener('click', function () {
              fetch(base + location.replace(/^\//, ''), {method: 'DELETE'}).then(function (d) {
                out.innerHTML += '<div>Undo: DELETE returned ' + d.status + '</div>';
                refreshLog();
              });
            });
          }
          refreshLog();
        });
      }).catch(function (e) { out.textContent = 'Request failed: ' + e; });
    });
  });

  var clear = document.getElementById('log-clear');
  if (clear) clear.addEventListener('click', function () {
    fetch(base + 'examples/requests/clear', {method: 'POST'}).then(refreshLog);
  });

  // ---- reveal ----
  var reveal = document.getElementById('reveal-btn');
  if (reveal) reveal.addEventListener('click', function () {
    document.getElementById('reveal').hidden = false;
    reveal.hidden = true;
  });

  // ---- keyboard: left and right arrows move one example at a time ----
  document.addEventListener('keydown', function (ev) {
    if (ev.target && /INPUT|TEXTAREA|SELECT/.test(ev.target.tagName)) return;
    var link = ev.key === 'ArrowRight' ? document.getElementById('ex-next')
             : ev.key === 'ArrowLeft' ? document.getElementById('ex-prev') : null;
    if (link) window.location = link.href;
  });

  // ---- zoom for the projector (remembered if the browser allows it) ----
  var size = 100;
  try { size = parseInt(localStorage.getItem('ex-zoom') || '100', 10); } catch (e) {}
  function applyZoom() {
    document.body.style.fontSize = (16 * size / 100) + 'px';
    try { localStorage.setItem('ex-zoom', String(size)); } catch (e) {}
  }
  applyZoom();
  var zin = document.getElementById('zoom-in'), zout = document.getElementById('zoom-out');
  if (zin) zin.addEventListener('click', function () { size = Math.min(size + 15, 200); applyZoom(); });
  if (zout) zout.addEventListener('click', function () { size = Math.max(size - 15, 70); applyZoom(); });

  refreshLog();
  setInterval(refreshLog, 3000);
})();
