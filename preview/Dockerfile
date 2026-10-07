# Preview-only toolchain image.
#
#  - Node serves the preview page on port 3000.
#  - Emscripten compiles the app's real native core (app/src/main/cpp) to
#    WebAssembly so it can run in the browser preview.
#
# The Android app itself is untouched by this image; nothing here ships in the APK.
FROM node:22-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends emscripten \
    && rm -rf /var/lib/apt/lists/*

# The repository is bind-mounted here at runtime so edits are picked up.
WORKDIR /src
