package com.pocketcookies.clue;

public class User {
	private String name;
	private String key;
	private int id;

	public User(String name, String key) {
		this.name = name;
		this.key = key;
	}

	public User() {
		this.name = null;
		this.key = null;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
