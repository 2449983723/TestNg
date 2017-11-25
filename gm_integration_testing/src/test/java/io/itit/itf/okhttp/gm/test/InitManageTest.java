package io.itit.itf.okhttp.gm.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Iterator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.itit.itf.okhttp.FastHttpClient;
import io.itit.itf.okhttp.Response;
import io.itit.itf.okhttp.gm.util.ConfigureUtil;
import net.sf.json.JSONObject;

public class InitManageTest {
	private static Logger logger = LoggerFactory.getLogger(InitManageTest.class);
	private static String manageUrl = null;
	private static Response response = null;
	private static Integer group_id = null;

	@BeforeClass
	public static void setUp() {

		manageUrl = ConfigureUtil.getValueByKey("manageUrl");
		logger.info(manageUrl);
		try {
			response = FastHttpClient.get().url(manageUrl).build().execute();

			String csrftoken = response.header("Set-Cookie").split("csrftoken=")[1].split(";")[0];
			response = FastHttpClient.post().url(manageUrl)
					.addHeader("Content-Type", "application/x-www-form-urlencoded")
					.addHeader("Upgrade-Insecure-Requests", "1")
					.addHeader("Cookie",
							"pgv_pvi=8013440000; group_id=0; csrftoken=wTHhHggvCj9oAudvoTmag6ZWIqL3Pdkz; Hm_lvt_950fd6e8467597acbd76f0c9f9771090=1510902770,1510905512; sessionid=wgwuryhtjwfvp1n02w7o954qu7wxorxa")
					.addHeader("Referer", "http://manage.dev.guanmai.cn:10080/").addParams("csrftoken", csrftoken)
					.addParams("username", "superman").addParams("password", "qilingxiaobaiyin")
					.addParams("this_is_the_login_form", "1").addParams("next", "/").build().execute();
			int code = response.code();
			logger.info(response.header("Set-Cookie") + "\n");
			if (code == 302) {
				logger.info(response.header("Set-Cookie") + "\n" + response.string() + "----");
			}
		} catch (Exception e) {
			logger.error("获取登入信息错误: " + e.getMessage());
			fail("获取基本信息失败");
		}

	}

	@Test(priority = 0)
	public void createMaGroupTest() {
		String url = manageUrl + "/admin/partner";
		try {
			response = FastHttpClient.post().url(url).
					addParams("manager","").
					addParams("is_valid","1").
					addParams("description","测试加盟商—李铭").
					addParams("station","").
					addParams("name","测试加盟商—李铭").
					build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			assertEquals(retObj.getInt("code") , 0,"创建加盟商失败");
			JSONObject data = retObj.getJSONObject("data");
	
			
			@SuppressWarnings("unchecked")
			Iterator<String> it = data.keys();   
	        while(it.hasNext()){  
	             String key = it.next();
	             JSONObject obj = data.getJSONObject(key);
	             if(obj.getString("name").equals("测试加盟商—李铭")){
	            	 group_id = obj.getInt("id");
	             }
	        }  
		} catch (Exception e) {
			logger.info("创建加盟商出现问题: " + e.getMessage());
			fail("创建加盟商出现问题: " + e.getMessage());
		}
	}

}
