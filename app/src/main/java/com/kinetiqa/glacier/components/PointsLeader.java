package com.kinetiqa.glacier.components;


public class PointsLeader {

    private String username;
	private long points;

	public PointsLeader(String username, long points) {
		this.username = username;
		this.points = points;
	}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

}
