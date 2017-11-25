package io.itit.itf.okhttp.gm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.itit.itf.okhttp.FastHttpClient;
import io.itit.itf.okhttp.Response;
import net.sf.json.JSONObject;

public class GmUtil {
	private static Logger logger = LoggerFactory.getLogger(GmUtil.class);
	private static String cookie = null;
	private static Response response = null;

	/**
	 * 登入Bshop，返回Cookie
	 * 
	 * @return cookie
	 */
	public static String bshopLogin() {
		
		String urlStr = ConfigureUtil.getValueByKey("bshopUrl")+ "/login";
		String userName =  ConfigureUtil.getValueByKey("bshopUserName");
		String pwd =  ConfigureUtil.getValueByKey("bshopPwd");
		try {
			response = FastHttpClient.post().url(urlStr).addParams("username", userName)
					.addParams("password", pwd).build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			if(retObj.getInt("code") != 0){
				logger.error("登入Bshop失败");
				return null;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		cookie = response.headers().get("Set-Cookie").substring("sessionid=".length(),
				response.headers().get("Set-Cookie").indexOf(";"));
		return cookie;
	}

	/***
	 * 退出bshop登入
	 * 
	 */
	public static int bshopLogout() {
		String urlStr = ConfigureUtil.getValueByKey("bshopUrl")+ "/logout";
		int code = 0;
		try {
			response = FastHttpClient.get().url(urlStr).addHeader("cookie", "sessionid=" + cookie).build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			code = retObj.getInt("code");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return code;
	}
	
	/***
	 * 登入Station
	 * @return cookie
	 */
	public static String stationLogin(){
		String urlStr = ConfigureUtil.getValueByKey("stationUrl")+ "/station/login";
		String userName =  ConfigureUtil.getValueByKey("stationName");
		String pwd =  ConfigureUtil.getValueByKey("stationPwd");
		try {
			response = FastHttpClient.post().url(urlStr).
					addParams("username", userName).
					addParams("password", pwd).
					addParams("this_is_the_login_form", "1").
					build().
					execute();
			cookie = response.headers().get("Set-Cookie").substring("sessionid=".length(),
					response.headers().get("Set-Cookie").indexOf(";"));
			logger.info("Login station system");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return cookie;
	}
	
	/**
	 * 退出Station
	 * 
	 */
	public static void stationLogout(){
		String urlStr = ConfigureUtil.getValueByKey("stationUrl")+ "/logout";
		try {
			response = FastHttpClient.get().url(urlStr).
					build().
					execute();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info("Logout station system");
	}
}
