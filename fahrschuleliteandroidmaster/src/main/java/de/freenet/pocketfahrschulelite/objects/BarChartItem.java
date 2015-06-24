package de.freenet.pocketfahrschulelite.objects;

import java.util.Date;

public class BarChartItem {

	public int value;
	public boolean passed;
	public Date date;
	
	public BarChartItem() {
		this.value = 0;
		this.passed = false;
		this.date = new Date();
	}
	
	public BarChartItem(int value, boolean passed, Date date) {
		this.value = value;
		this.passed = passed;
		this.date = date;
	}
}
