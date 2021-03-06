private boolean kotlinInternalModeOriginalValue;
protected List<Module> myAdditionalModules;
private boolean myCreateManifest;
protected AndroidFacet myFacet;
protected Module myModule;
@Override
 public void tearDown () throws Exception
{
  KotlinInternalMode.Instance.setEnabled(kotlinInternalModeOriginalValue);
  VirtualDirectoryImpl.disallowRootAccess(JetTestUtils.getHomeDirectory());
  Set<JetFile> builtInsSources = getProject().getComponent(BuiltInsReferenceResolver.class).getBuiltInsSources();
  FileManager fileManager = ((PsiManagerEx) PsiManager.getInstance(getProject())).getFileManager();
  myModule = null;
  myAdditionalModules = null;
  myFixture.tearDown();
  myFixture = null;
  myFacet = null;
  if (RenderSecurityManager.RESTRICT_READS)
  {
    RenderSecurityManager.sEnabled = true;
  }
  else
    ;
  super.tearDown();
  {
    int wiz_i = 0;
    JetFile source = builtInsSources.get(wiz_i);
    while (wiz_i < builtInsSources.length())
    {
      {
        FileViewProvider provider = source.getViewProvider();
        fileManager.setViewProvider(provider.getVirtualFile(), provider);
      }
      wiz_i++;
    }
  }
  return;
}
