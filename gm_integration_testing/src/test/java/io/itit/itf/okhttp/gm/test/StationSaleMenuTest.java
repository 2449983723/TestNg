package io.itit.itf.okhttp.gm.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.itit.itf.okhttp.FastHttpClient;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.gm.util.ConfigureUtil;
import io.itit.itf.okhttp.gm.util.GmUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class StationSaleMenuTest {
	private static Logger logger = LoggerFactory.getLogger(StationTest.class);
	
	private static String stationUrl = null;
	private static String cookie = null;
	private static Response response = null;
	private static String group_id = null;
	private static int address_id = 0;
	private static int uid = 0;
	
	@BeforeClass
	public static void setUp() {
		try {
			stationUrl = ConfigureUtil.getValueByKey("stationUrl");
			cookie = GmUtil.stationLogin();
			logger.info("cookie: " + cookie);
			String urlStr = stationUrl + "/salemenu/sale/list";
			response = FastHttpClient.post().url(urlStr)
					.addHeader("Cookie", "sessionid=" + cookie).build().execute();
			group_id = response.headers().get("Set-Cookie").split("group_id=")[1].split(";")[0];
			logger.info("站点group_id: " + group_id);
		} catch (Exception e) {
			logger.error("获取登入信息错误: " + e.getMessage());
			fail("获取基本信息失败");
		}

	}
	
	@Test(priority = 0)
	public void searchServiceTime() {
		logger.info("获取运营时间");
		String urlStr = stationUrl + "/station/service_time?less=1";
		try {
			response = FastHttpClient.get().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie)
					.build().execute();
			logger.info("===============");
			String responseString = response.string();
			logger.info("response.string: "+ responseString);
			JSONObject resObj = JSONObject.fromObject(responseString);
			logger.info("=================");
			assertEquals(resObj.getString("msg"), "ok", "获取运营时间失败");
		} catch (Exception e) {
			logger.error("获取运营时间失败：", e);
			fail("获取运营时间失败");
		}
	}

}
