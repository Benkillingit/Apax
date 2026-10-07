# Agent notes — Apax

Only non-obvious things about this repo and its sandbox setup.

## What this project is

- Native **Android** app: Java UI (`app/src/main/java/com/apax/core/`) plus a C++ reasoning
  core reached over JNI (`app/src/main/cpp/`). `minSdk 30`, `arm64-v8a` only (Walmart 100011886 tablet).
- It has **no web or server component** — nothing in the app listens on a port, and an APK cannot
  render in a browser. That is why the sandbox runs the preview harness described below.
- **No credentials and no external services**: no API keys, no network calls, no remote database.
  Everything is on-device. `/run/base44/app.env` is not wired in because nothing needs it.

## Running it in the sandbox

```
docker compose -f docker-compose.base44.yml up -d --build     # preview on port 3000
```

- This stack does **not** build the Android app. An APK needs the Android SDK + NDK + CMake 3.22.1
  (the Android Studio toolchain), which is not installed here.
- Instead `preview/` compiles the app's **real** native core to WebAssembly with Emscripten and
  serves a page that mirrors `activity_main.xml`, so the core can be exercised in the browser:
  - `preview/wasm/apax_web.cpp` — one exported function per native method the Java layer calls;
    mirrors `apax_jni_bridge.cpp`, including the lazy singleton that creates and initializes
    `ApaxCore` on first use.
  - `preview/wasm/android_log_shim/android/log.h` — shim for `<android/log.h>`, supplied with `-I`
    so `app/src/main/cpp/apax_core.cpp` compiles unchanged. Log lines go to the browser console.
  - `preview/web/` — the page. Its text and colors are copied from `activity_main.xml`,
    `strings.xml`, `values/themes.xml` and `values-night/themes.xml`.
  - Compiled WebAssembly lands in `/tmp/apax-web` (outside the repo) and is rebuilt from the
    bind-mounted C++ on every container start.

## Editing

- `app/src/main/cpp/apax_core.cpp` — takes effect after
  `docker compose -f docker-compose.base44.yml restart preview` (WebAssembly is compiled at
  container start; there is no watcher).
- `preview/web/*` — plain static files; just reload the page.
- Compose gotcha: inside `command:`, escape the container shell's own variables as `$$VAR`
  (e.g. `$$APAX_WASM_DIR`). A single `$` is interpolated by Compose from the host environment
  and silently becomes empty.

## Verifying it works

- `curl -fsS http://localhost:3000/` serves the page; `/apax_core.wasm` is the compiled core.
- In the preview, **Start Camera** / **Start Audio** drive the real `processVideoFrame` /
  `processAudioData`. The monospace panels show the core's own strings — `APAX CORE STATUS`,
  `Video: 1280x720, Brightness: N, Format: 0`, `Audio: Avg amplitude: N, Peak: N, Rate: 44100Hz` —
  and the frame counters under `CONTINUOUS I/O STATUS` should climb.
- On a real device (not possible here): `./gradlew assembleDebug`, then install on the tablet.

## Behaviour worth knowing

- `ApaxCameraService.processFrames()` does not use CameraX despite the CameraX dependencies —
  it feeds synthetic `1280x720` frames (`128 ± 25`). The preview reproduces that behaviour.
- The preview's audio path uses the real microphone when the browser allows it and falls back to
  synthesised PCM samples otherwise (the footer line says which).
