int a = 8;
int audioDeviceBufferCount = 9;
int audioDeviceBufferSize = 512;
int audioDeviceSimultaneousSources = 16;
int b = 8;
int depth = 16;
boolean disableAudio = false;
Lwjgl3DisplayMode fullscreenMode;
int g = 8;
int gles30ContextMajorVersion = 3;
int gles30ContextMinorVersion = 2;
HdpiMode hdpiMode = HdpiMode.Logical;
Color initialBackgroundColor = Color.BLACK;
String preferencesDirectory = ".prefs/";
Files.FileType preferencesFileType = FileType.External;
int r = 8;
int samples = 0;
int stencil = 0;
String title = "";
boolean useGL30 = false;
boolean vSyncEnabled = true;
boolean windowDecorated = true;
int windowHeight = 480;
Lwjgl3WindowListener windowListener;
boolean windowResizable = true;
int windowWidth = 640;
int windowX = -1;
int windowY = -1;
int windowMaxHeight = -1;
int windowMaxWidth = -1;
int windowMinHeight = -1;
int windowMinWidth = -1;
boolean initialVisible = true;
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
  copy.windowMinWidth = config.windowMinWidth;
  copy.windowMinHeight = config.windowMinHeight;
  copy.windowMaxWidth = config.windowMaxWidth;
  copy.windowMaxHeight = config.windowMaxHeight;
  copy.windowResizable = config.windowResizable;
  copy.windowDecorated = config.windowDecorated;
  copy.windowListener = config.windowListener;
  copy.fullscreenMode = config.fullscreenMode;
  copy.vSyncEnabled = config.vSyncEnabled;
  copy.title = config.title;
  copy.initialBackgroundColor = config.initialBackgroundColor;
  copy.initialVisible = config.initialVisible;
  copy.preferencesDirectory = config.preferencesDirectory;
  copy.preferencesFileType = config.preferencesFileType;
  copy.hdpiMode = config.hdpiMode;
  return copy;
}
