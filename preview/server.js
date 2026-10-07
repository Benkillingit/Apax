/*
 * Static server for the Apax browser preview (port 3000).
 *
 * Serves:
 *   - preview/web      the preview page (mirrors MainActivity's UI)
 *   - $APAX_WASM_DIR   the WebAssembly build of the real native core,
 *                      compiled at container start (outside the repo)
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const WEB_DIR = path.join(__dirname, 'web');
const GEN_DIR = process.env.APAX_WASM_DIR || '/tmp/apax-web';
const PORT = Number(process.env.PORT || 3000);

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.wasm': 'application/wasm',
  '.json': 'application/json; charset=utf-8',
};

function reply(res, status, body, type) {
  res.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store' });
  res.end(body);
}

const server = http.createServer((req, res) => {
  const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  const requested = pathname === '/' ? 'index.html' : pathname.replace(/^\/+/, '');

  // Generated WebAssembly first, then the page itself.
  let file = path.join(WEB_DIR, requested);
  if (!fs.existsSync(file)) {
    file = path.join(GEN_DIR, requested);
  }

  // path.join() normalizes "..", so this also rejects traversal attempts.
  if (!file.startsWith(WEB_DIR) && !file.startsWith(GEN_DIR)) {
    return reply(res, 403, 'Forbidden', 'text/plain; charset=utf-8');
  }

  fs.readFile(file, (err, data) => {
    if (err) {
      return reply(res, 404, 'Not found', 'text/plain; charset=utf-8');
    }
    reply(res, 200, data, MIME_TYPES[path.extname(file)] || 'application/octet-stream');
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Apax preview listening on http://0.0.0.0:${PORT}`);
});
