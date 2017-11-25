package io.itit.itf.okhttp.gm.test;

import java.util.ArrayList;
import java.util.List;

import org.testng.TestNG;

public class MainTest {
	public static void main(String[] args) {
		TestNG testng = new TestNG();

		//***第一种方式***//
		//testng.setTestClasses(new Class[] { BshopTest.class, StationTest.class });

		//***第二种方式***//
		List<String> suites = new ArrayList<String>();
		suites.add("./testng-bshop.xml");
		suites.add("./testng-station.xml");
		testng.setTestSuites(suites);

		testng.run();
	}
}
