package io.itit.itf.okhttp.gm.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureUtil {
	private static Logger logger = LoggerFactory.getLogger(ConfigureUtil.class);
	
	private static String fileName = "item.properties";

	public static String getValueByKey(String key) {
		Properties pps = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(fileName));
			pps.load(in);
			String value = pps.getProperty(key);
			return value;
		} catch (IOException e) {
			logger.error("读取配置文件信息出错 : " + e.getMessage());
			return null;
		}
	}
}
