package com.snda.everbox.fileviewer;

public class EverBoxItem implements Comparable<Object> {
	private String caption;
	private int id;
	private int type;

	public EverBoxItem(int paramInt1, String paramString, int paramInt2) {
		setCaption(paramString);
		setId(paramInt1);
		setType(paramInt2);
	}

	public String getCaption() {
		return this.caption;
	}

	public int getId() {
		return this.id;
	}

	public int getType() {
		return this.type;
	}

	public void setCaption(String paramString) {
		this.caption = paramString;
	}

	public void setId(int paramInt) {
		this.id = paramInt;
	}

	public void setType(int paramInt) {
		this.type = paramInt;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}

