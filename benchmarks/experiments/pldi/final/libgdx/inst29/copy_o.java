static Lwjgl3ApplicationConfiguration copy (Lwjgl3ApplicationConfiguration config)
{
  Lwjgl3ApplicationConfiguration copy = new Lwjgl3ApplicationConfiguration();
  copy.disableAudio = config.disableAudio;
  copy.audioDeviceSimultaneousSources = config.audioDeviceSimultaneousSources;
  copy.audioDeviceBufferSize = config.audioDeviceBufferSize;
  copy.audioDeviceBufferCount = config.audioDeviceBufferCount;
  copy.useGL30 = config.useGL30;
  copy.gles30ContextMajorVersion = config.gles30ContextMajorVersion;
  copy.gles30ContextMinorVersion = config.gles30ContextMinorVersion;
  copy.r = config.r;
  copy.g = config.g;
  copy.b = config.b;
  copy.a = config.a;
  copy.depth = config.depth;
  copy.stencil = config.stencil;
  copy.samples = config.samples;
  copy.windowX = config.windowX;
  copy.windowY = config.windowY;
  copy.windowWidth = config.windowWidth;
  copy.windowHeight = config.windowHeight;
  copy.windowResizable = config.windowResizable;
  copy.windowDecorated = config.windowDecorated;
  copy.windowListener = config.windowListener;
  copy.fullscreenMode = config.fullscreenMode;
  copy.vSyncEnabled = config.vSyncEnabled;
  copy.title = config.title;
  copy.initialBackgroundColor = config.initialBackgroundColor;
  copy.preferencesDirectory = config.preferencesDirectory;
  copy.preferencesFileType = config.preferencesFileType;
  copy.hdpiMode = config.hdpiMode;
  return copy;
}