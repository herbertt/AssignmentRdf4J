package com.tenforce.assignment;

import java.util.List;

public class Email {

	private int id;
	private String from;
	private String name;
	private String title;
	private String body;
	private List<String> to = null;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "ID: " + getId() + " From: " + getFrom() + " TO: " + getTo().get(0) + " Title: " + getTitle() + " "
				+ getBody();
	}

}
