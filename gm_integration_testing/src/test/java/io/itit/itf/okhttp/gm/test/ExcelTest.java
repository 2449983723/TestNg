package io.itit.itf.okhttp.gm.test;

import io.itit.itf.okhttp.FastHttpClient;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.gm.util.ConfigureUtil;
import io.itit.itf.okhttp.gm.util.ExcelReader;
import io.itit.itf.okhttp.gm.util.ExcleUtil;
import io.itit.itf.okhttp.gm.util.GmUtil;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by  on 2017/9/5.
 */
public class ExcelTest {
    static ExcleUtil excleUtil;
    ExcelReader ex;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Logger logger = LoggerFactory.getLogger(StationTest.class);
	
	private static String stationUrl = null;
	private static String cookie = null;
	private static Response response = null;
	private static String group_id = null;
	private static int address_id = 0;
	private static int uid = 0;
	
	private void login() {
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
	
	private void readExcleTestData() {
        String excelFilePath = "test.xlsx";
        String sheetName = "Sheet1";
        ex = new ExcelReader(excelFilePath, sheetName);
        try {
            ExcleUtil.setExcleFile(excelFilePath, sheetName);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private JSONObject sendGetRequest(String url, String params) {
		try {
			response = FastHttpClient.get().url(stationUrl + url + "?" + params)
					.addHeader("cookie", "sessionid=" + cookie + ";" + "group_id=" + group_id)
					.build().execute();
			logger.info("===============");
			String responseString = response.string();
			logger.info("response.string: "+ responseString);
			JSONObject resObj = JSONObject.fromObject(responseString);
			logger.info("=================");
			return resObj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
    @BeforeTest
    public void init() {
    	login();
    	readExcleTestData();
    }

    @Test(dataProvider = "dp")
    public void testAllExcel(Object... obj) throws Exception {
        String url = "";
        String requestType = "";
        String parameters = "";
        String except = "";
        String error_tip = "";

    	url = (String) obj[0];
    	requestType = (String) obj[1];
    	parameters = (String) obj[2];
    	except = (String) obj[3];
    	error_tip = (String) obj[4];
    	
        System.out.println("url=" + url + "  requestType=" + 
        		requestType + "  parameters=" + parameters + "  except=" + except);
        if (requestType.contentEquals("get")) {
        	JSONObject resObj = sendGetRequest(url, parameters);
			assertEquals(resObj.getString("msg"), except, error_tip);
        }
        
    }

    @DataProvider
    public Object[][] dp() {
        Object[][] sheetData2 = ex.getSheetData2();

        return sheetData2;
    }
}