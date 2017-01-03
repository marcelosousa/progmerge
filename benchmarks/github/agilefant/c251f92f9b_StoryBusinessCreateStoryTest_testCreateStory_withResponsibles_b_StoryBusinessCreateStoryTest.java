{
  User user1 = new User();
  User user2 = new User();
  Story tmp = new Story();
  List<String> labels = new ArrayList<String>();
  Backlog blog = new Project();
  expect(backlogBusiness.retrieve(5)).andReturn(blog);
  expect(userDAO.get(2)).andReturn(user1);
  expect(userDAO.get(23)).andReturn(user2);
  Capture<Story> capturedStory = new Capture<Story>();
  expect(storyDAO.create(EasyMock.capture(capturedStory))).andReturn(88);
  expect(storyDAO.get(88)).andReturn(tmp);
  storyRankBusiness.rankToBottom(EasyMock.isA(Story.class), EasyMock.isA(Backlog.class));
  blheBusiness.updateHistory(blog.getId());
  Story returnedStory = new Story();
  returnedStory.setId(88);
  expect(storyDAO.get(88)).andReturn(returnedStory);
  storyHierarchyBusiness.moveToBottom(returnedStory);
  labelBusiness.createStoryLabels(labels, 88);
  replayAll();
  Story actual = this.storyBusiness.create(new Story(), 5, null, new HashSet<Integer>(Arrays.asList(2, 23)), labels);
  verifyAll();
  assertSame(actual, returnedStory);
  assertTrue(capturedStory.getValue().getResponsibles().contains(user1));
  assertTrue(capturedStory.getValue().getResponsibles().contains(user2));
  assertEquals(blog, capturedStory.getValue().getBacklog());
}