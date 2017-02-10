{
  runCPD("--minimum-tokens", "100", "--files", "src/test/resources/net/sourceforge/pmd/cpd/badandgood/", "--language", "c", "--format", "csv");
  String out = bufferStdout.toString("UTF-8");
  Assert.assertFalse(out.contains("Couldn't instantiate renderer"));
  Assert.assertEquals(0, Integer.parseInt(System.getProperty(CPDCommandLineInterface.STATUS_CODE_PROPERTY)));
}