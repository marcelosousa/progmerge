{
  Backlog parent = backlog;
  if ((backlog == null || backlog.getParent()) == null)
  {
    return null;
  }
  else
  {
    while (!(parent instanceof Product))
    {
      if (parent == null)
      {
        return null;
      }
      if (parent instanceof Iteration && parent.isStandAlone())
      {
        return null;
      }
      parent = parent.getParent();
    }
    return (Product) parent;
  }
}