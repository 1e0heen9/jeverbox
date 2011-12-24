package com.snda.everbox.fileviewer;

public class LocalFileItem
{
  private String caption;
  private int id;
  private int type;

  public LocalFileItem(int paramInt1, String paramString, int paramInt2)
  {
    this.id = paramInt1;
    this.caption = paramString;
    this.type = paramInt2;
  }

  public String getCaption()
  {
    return this.caption;
  }

  public int getId()
  {
    return this.id;
  }

  public int getType()
  {
    return this.type;
  }

  public void setCaption(String paramString)
  {
    this.caption = paramString;
  }

  public void setId(int paramInt)
  {
    this.id = paramInt;
  }

  public void setType(int paramInt)
  {
    this.type = paramInt;
  }
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.fileviewer.LocalFileItem
 * JD-Core Version:    0.5.4
 */