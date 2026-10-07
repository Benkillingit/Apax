/*
 * Apax browser preview controller.
 *
 * Mirrors MainActivity: the same six controls, the same readouts, the same
 * one-second refresh, and the same continuous I/O loops the Android services
 * run (ApaxCameraService / ApaxAudioService) - every sample goes into the real
 * native core compiled to WebAssembly.
 */

(() => {
  'use strict';

  const byId = (id) => document.getElementById(id);

  const elements = {
    version: byId('tv_version'),
    serviceStatus: byId('tv_service_status'),
    status: byId('tv_status'),
    continuousOutput: byId('tv_continuous_output'),
    startCamera: byId('btn_start_camera'),
    stopCamera: byId('btn_stop_camera'),
    startAudio: byId('btn_start_audio'),
    stopAudio: byId('btn_stop_audio'),
    refresh: byId('btn_refresh'),
    initialize: byId('btn_initialize'),
    footnote: byId('footnote'),
  };

  // ApaxCameraService feeds 1280x720 frames tagged as format 0.
  const FRAME_WIDTH = 1280;
  const FRAME_HEIGHT = 720;
  const FRAME_FORMAT = 0;
  const FRAME_INTERVAL_MS = 33;

  // ApaxAudioService captures PCM 16-bit mono at 44.1 kHz.
  const SAMPLE_RATE = 44100;
  const AUDIO_SAMPLES = 2048;

  const state = {
    cameraTimer: null,
    audioTimer: null,
    audioStream: null,
    audioContext: null,
    framePointer: 0,
    audioPointer: 0,
    cameraRunning: false,
    audioRunning: false,
  };

  // ---- Native core (real apax_core.cpp, compiled to WebAssembly) ------------
  // One wrapper per native method the Java layer calls via JNI.
  const core = {
    initialize: () => Module.ccall('apaxInitialize', 'number', [], []) === 1,
    version: () => Module.ccall('apaxVersion', 'string', [], []),
    statusString: () => Module.ccall('apaxStatusString', 'string', [], []),
    continuousOutput: () => Module.ccall('apaxContinuousOutput', 'string', [], []),
    processAudioData: (pointer, samples, rate) =>
      Module.ccall('apaxProcessAudioData', 'string', ['number', 'number', 'number'], [pointer, samples, rate]),
    processVideoFrame: (pointer, width, height, format) =>
      Module.ccall('apaxProcessVideoFrame', 'string', ['number', 'number', 'number', 'number'], [pointer, width, height, format]),
  };

  // ---- Readouts ------------------------------------------------------------

  function updateServiceStatus() {
    elements.serviceStatus.textContent =
      `Camera: ${state.cameraRunning ? 'RUNNING' : 'STOPPED'} | ` +
      `Audio: ${state.audioRunning ? 'RUNNING' : 'STOPPED'}`;
  }

  // Mirrors MainActivity.updateStatus()
  function updateStatus() {
    try {
      elements.status.textContent = core.statusString();
      elements.version.textContent = `Native Core Version: ${core.version()}`;
      elements.continuousOutput.textContent = core.continuousOutput();
    } catch (error) {
      elements.status.textContent = `Error: ${error.message}`;
    }
  }

  // Mirrors MainActivity.setupPeriodicUpdates()
  function startPeriodicUpdates() {
    updateStatus();
    setInterval(updateStatus, 1000);
  }

  // ---- Camera: mirrors ApaxCameraService.processFrames() -------------------

  function startCamera() {
    if (state.cameraTimer) return;

    if (!state.framePointer) {
      state.framePointer = Module._malloc(FRAME_WIDTH * FRAME_HEIGHT);
    }

    // The Android stub fills each frame with random values around mid-grey;
    // refresh a slice of the frame buffer the same way, every frame.
    const noise = new Uint8Array(4096);
    const tick = () => {
      crypto.getRandomValues(noise);
      Module.HEAP8.set(noise, state.framePointer);
      core.processVideoFrame(state.framePointer, FRAME_WIDTH, FRAME_HEIGHT, FRAME_FORMAT);
    };

    tick();
    state.cameraTimer = setInterval(tick, FRAME_INTERVAL_MS);
    state.cameraRunning = true;
    updateServiceStatus();
  }

  function stopCamera() {
    if (state.cameraTimer) {
      clearInterval(state.cameraTimer);
      state.cameraTimer = null;
    }
    state.cameraRunning = false;
    updateServiceStatus();
  }

  // ---- Audio: mirrors ApaxAudioService.recordAudio() ----------------------

  function feedAudio(shortSamples) {
    Module.HEAP16.set(shortSamples, state.audioPointer >> 1);
    core.processAudioData(state.audioPointer, shortSamples.length, SAMPLE_RATE);
  }

  // Uses the real microphone when the browser allows it, and falls back to
  // synthesised PCM samples otherwise (the preview iframe usually blocks it).
  async function requestMicrophone() {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) return null;
    try {
      return await Promise.race([
        navigator.mediaDevices.getUserMedia({ audio: true }),
        new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), 1500)),
      ]);
    } catch (error) {
      return null;
    }
  }

  function startSyntheticAudio() {
    const samples = new Int16Array(AUDIO_SAMPLES);
    const tick = () => {
      crypto.getRandomValues(samples);
      feedAudio(samples);
    };
    tick();
    state.audioTimer = setInterval(tick, FRAME_INTERVAL_MS);
  }

  function startMicrophoneAudio(stream) {
    state.audioContext = new (window.AudioContext || window.webkitAudioContext)();
    const source = state.audioContext.createMediaStreamSource(stream);
    const processor = state.audioContext.createScriptProcessor(4096, 1, 1);

    processor.onaudioprocess = (event) => {
      const channel = event.inputBuffer.getChannelData(0);
      const samples = new Int16Array(channel.length);
      for (let i = 0; i < channel.length; i++) {
        samples[i] = Math.max(-1, Math.min(1, channel[i])) * 32767;
      }
      feedAudio(samples);
    };

    source.connect(processor);
    processor.connect(state.audioContext.destination);
  }

  async function startAudio() {
    if (state.audioTimer || state.audioRunning) return;

    if (!state.audioPointer) {
      state.audioPointer = Module._malloc(AUDIO_SAMPLES * 2);
    }

    state.audioRunning = true;
    updateServiceStatus();

    const stream = await requestMicrophone();
    if (!state.audioRunning) return; // stopped while the prompt was open

    if (stream) {
      state.audioStream = stream;
      startMicrophoneAudio(stream);
      elements.footnote.textContent = 'Native core running in the browser · microphone capture active';
    } else {
      startSyntheticAudio();
      elements.footnote.textContent = 'Native core running in the browser · synthesised audio samples (microphone blocked here)';
    }
  }

  function stopAudio() {
    if (state.audioTimer) {
      clearInterval(state.audioTimer);
      state.audioTimer = null;
    }
    if (state.audioStream) {
      state.audioStream.getTracks().forEach((track) => track.stop());
      state.audioStream = null;
    }
    if (state.audioContext) {
      state.audioContext.close();
      state.audioContext = null;
    }
    state.audioRunning = false;
    updateServiceStatus();
  }

  // ---- Wiring (mirrors MainActivity.setupUI()) ----------------------------

  function wireControls() {
    elements.refresh.addEventListener('click', updateStatus);
    elements.initialize.addEventListener('click', () => {
      core.initialize();
      updateStatus();
    });
    elements.startCamera.addEventListener('click', startCamera);
    elements.stopCamera.addEventListener('click', stopCamera);
    elements.startAudio.addEventListener('click', startAudio);
    elements.stopAudio.addEventListener('click', stopAudio);
  }

  function boot() {
    elements.footnote.textContent = 'Native core running in the browser · WebAssembly build of app/src/main/cpp';
    wireControls();
    updateServiceStatus();
    startPeriodicUpdates();
  }

  // apax_core.js is loaded after this file and picks this up as its Module.
  window.Module = { onRuntimeInitialized: boot };
})();
