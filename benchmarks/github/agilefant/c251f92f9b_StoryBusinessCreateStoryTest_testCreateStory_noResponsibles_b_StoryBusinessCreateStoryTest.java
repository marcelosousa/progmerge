{
  Story tmp = new Story();
  Iteration blog = new Iteration();
  Project proj = new Project();
  blog.setParent(proj);
  expect(backlogBusiness.retrieve(5)).andReturn(blog);
  Capture<Story> capturedStory = new Capture<Story>();
  expect(storyDAO.create(EasyMock.capture(capturedStory))).andReturn(88);
  expect(storyDAO.get(88)).andReturn(tmp);
  storyRankBusiness.rankToBottom(tmp, blog);
  storyRankBusiness.rankToBottom(tmp, proj);
  blheBusiness.updateHistory(blog.getId());
  iheBusiness.updateIterationHistory(blog.getId());
  Story returnedStory = new Story();
  returnedStory.setId(88);
  expect(storyDAO.get(88)).andReturn(returnedStory);
  Story dataItem = new Story();
  dataItem.setName("Foofaa");
  dataItem.setDescription("Foofaa");
  dataItem.setStoryPoints(22);
  dataItem.setState(StoryState.STARTED);
  storyHierarchyBusiness.moveToBottom(returnedStory);
  labelBusiness.createStoryLabels(null, 88);
  replayAll();
  Story actual = this.storyBusiness.create(dataItem, 5, null, null, null);
  verifyAll();
  assertEquals(actual.getClass(), Story.class);
  assertEquals(blog, capturedStory.getValue().getBacklog());
  assertEquals(dataItem.getName(), capturedStory.getValue().getName());
  assertEquals(dataItem.getDescription(), capturedStory.getValue().getDescription());
  assertEquals(dataItem.getStoryPoints(), capturedStory.getValue().getStoryPoints());
  assertEquals(dataItem.getState(), capturedStory.getValue().getState());
}