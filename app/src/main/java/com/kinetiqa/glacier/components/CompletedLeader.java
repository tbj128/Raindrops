package com.kinetiqa.glacier.components;


public class CompletedLeader {
	private String username;
    private int numItemsCompleted;

	public CompletedLeader(String username, int numItemsCompleted) {
		this.username = username;
		this.numItemsCompleted = numItemsCompleted;
	}

    public int getNumItemsCompleted() {
        return numItemsCompleted;
    }

    public void setNumItemsCompleted(int numItemsCompleted) {
        this.numItemsCompleted = numItemsCompleted;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
