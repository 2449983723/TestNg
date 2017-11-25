package io.itit.itf.okhttp.gm.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

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


public class BshopTest {
	private static String bshopUrl = null;
	private static String cookie = null;
	private static Logger logger = LoggerFactory.getLogger(BshopTest.class);
	private static Response response = null;
	private static int user_id = 0;
	private static String station_id = null;
	private static List<String> categoryList = null;
	private static JSONObject googsObj = new JSONObject();
	private static JSONArray sku_ids = null;
	private static JSONArray salemenu_ids = null;

	@BeforeClass
	public static void setUp() {
		bshopUrl = ConfigureUtil.getValueByKey("bshopUrl");
		cookie = GmUtil.bshopLogin();
	}

	/**
	 * 获取SID用户
	 * 
	 */
	@Test (priority = 0)
	public void getAccount() {
		try {
			String urlStr = bshopUrl+ "/user/account";
			response = FastHttpClient.get().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			String msg = retObj.getString("msg");
		    assertEquals(msg, "ok","获取SID用户信息失败");
			user_id = ((JSONObject) retObj.getJSONObject("data").getJSONArray("addresses").get(0)).getInt("id");
			station_id = retObj.getJSONObject("data").getString("station_id");
			logger.info("user_id: " +user_id + "; station_id: " + station_id);
		} catch (Exception e) {
			logger.error("getAccount: "+ e.getMessage());
			fail("获取SID用户出错");
		}
	}
	
	/***
	 * 设置SID用户信息，默认选择第一个
	 * 
	 */
	@Test (priority = 1)
	public void setAccount() {
		try {
			String urlStr = bshopUrl+ "/user/address/set";
			response = FastHttpClient.post().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.addParams("address_id",String.valueOf(user_id))
					.build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			String msg = retObj.getString("msg");
		    assertEquals(msg, "ok","设置用户信息失败");
		    logger.info("设置KID商户: " + user_id);
		} catch (Exception e) {
			logger.error("setAccount: " + e.getMessage());
			fail("设置SID用户出错");
		}
	}
	
	/***
	 * 获取商品分类
	 * 
	 */
	@Test (priority = 2)
	public void getCategoryArray(){
		categoryList = new ArrayList<String>();
		try {
			String urlStr = bshopUrl+ "/product/category/get";
			response = FastHttpClient.get().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie+ ";cms_key=gm")
					.build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			int code = retObj.getInt("code");
		    assertEquals(code, 0,"获取商品分类失败");
		    JSONArray categoryArray = retObj.getJSONArray("data");
		    for(int i = 0; i < categoryArray.size(); i ++){
		    	JSONObject categoryObj = categoryArray.getJSONObject(i);
		    	JSONArray childrenArray = categoryObj.getJSONArray("children");
		    	for(int j = 0; j < childrenArray.size(); j ++){
		    		categoryList.add(childrenArray.getJSONObject(j).getString("id"));
		    	}
		    }
		} catch (Exception e) {
			logger.error("getCategoryArray: " + e.getMessage());
			fail("获取商品分类出错");
		}
	}
	
	/***
	 * 
	 * 获取商品，组成下单列表
	 * 
	 */
	@Test (priority = 3)
	public void getGoods(){
		//下单总数控制
		int count = 20;
		try {
			String urlStr = bshopUrl+ "/product/sku/get?level=2&category_id=";
			OK:
			for(String category: categoryList){
				response = FastHttpClient.get().url(urlStr + category)
						.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
						.build().execute();
				JSONObject retObj = JSONObject.fromObject(response.string());
				int code = retObj.getInt("code");
			    assertEquals(code, 0,"获取商品列表失败");
			    
			    JSONArray dataArry = retObj.getJSONArray("data");
			    for(int i = 0; i < dataArry.size(); i++){
			    	JSONArray  skuArray = dataArry.getJSONObject(i).getJSONArray("skus");
			    	for(int j = 0; j < skuArray.size(); j ++){
			    		String id = skuArray.getJSONObject(j).getString("id");
			    		int sale_num_least = skuArray.getJSONObject(j).getInt("sale_num_least");
			    		googsObj.put(id, sale_num_least * 3);
			    		if(googsObj.size() > count){
			    			break OK;
			    		}
			    	}
			    }
			}
			logger.info(googsObj.toString());
		} catch (Exception e) {
			logger.error("getGoods: " + e.getMessage());
			fail("获取下单商品列表出错");
		}
	}
	
	/***
	 * 更新购物车
	 * 
	 */
	@Test (priority = 4)
	public void updateCart(){
		logger.info("更新购物车");
		String urlStr = bshopUrl+ "/cart/update";
		JSONArray array = new JSONArray();
	    array.add(googsObj);
		try {
			response = FastHttpClient.post().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.addParams("data",googsObj.toString())
					.addParams("skus",array.toString())
					.build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			String msg = retObj.getString("msg");
		    assertEquals(msg, "ok","更新购物车失败");
		    
		} catch (Exception e) {
			logger.error("updateCart: " + e.getMessage());
			fail("更新购物车出错");
		}
	}
	
	/***
	 * 确认购物车,设置收货时间，付款方式
	 * 
	 */
	@Test (priority = 5)
	public void confirmCart(){
		String urlStr = bshopUrl+ "/order/confirm";
		try {
			response = FastHttpClient.get().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			String msg = retObj.getString("msg");
		    assertEquals(msg, "ok","确认购物车失败");
		    logger.info("确认购物车 ");
		    JSONObject orderObj = (JSONObject) retObj.getJSONObject("data").getJSONArray("orders").get(0);
		    sku_ids = orderObj.getJSONArray("sku_ids");
		    salemenu_ids = orderObj.getJSONArray("salemenu_ids");
		    logger.info("sku_ids: " + sku_ids + "; salemenu_ids: " + salemenu_ids);
		    
		    JSONObject receive_time = orderObj.getJSONObject("receive_time");
		    JSONObject receive_time_limit = receive_time.getJSONObject("receive_time_limit");
		    String defaultStart = receive_time_limit.getString("r_start");
		    String defaultEnd = receive_time_limit.getString("r_end");
		    String defaultSpanStartFlag = receive_time_limit.getString("s_span_time");
		    String defaultSpanEndFlag = receive_time_limit.getString("e_span_time");
		    String time_config_id = receive_time_limit.getString("time_config_id");
		    
		    logger.info("设置收货时间 ");
		    
		    urlStr = bshopUrl + "/order/receive_time";
		    response = FastHttpClient.post().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.addParams("defaultStart",defaultStart)
					.addParams("defaultEnd",defaultEnd)
					.addParams("defaultSpanStartFlag",defaultSpanStartFlag)
					.addParams("defaultSpanEndFlag",defaultSpanEndFlag)
					.addParams("time_config_id",time_config_id)
					.addParams("station_id",station_id)
					.build().execute();
		    
		    retObj = JSONObject.fromObject(response.string());
			msg = retObj.getString("msg");
		    assertEquals(msg, "ok","设置收货时间失败");
		    
		    logger.info("设置付款方式 ");
		    urlStr = bshopUrl + "/order/paymethod";
		    response = FastHttpClient.post().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.addParams("type",String.valueOf(2))
					.addParams("time_config_id",time_config_id)
					.addParams("station_id",station_id)
					.build().execute();
		    
		    retObj = JSONObject.fromObject(response.string());
			msg = retObj.getString("msg");
		    assertEquals(msg, "ok","设置设置付款失败");
		} catch (Exception e) {
			logger.error("confirmCart: "+e.getMessage());
			fail("确认购物车，设置收货时间，付款方式出错");
		}
	}
	
	@Test (priority = 6)
	public void submitCart(){
		String urlStr = bshopUrl + "/order/submit";
		JSONArray orders = new JSONArray();
		JSONObject order = new JSONObject();
		order.put("salemenu_ids", salemenu_ids);
		order.put("sku_ids", sku_ids);
		order.put("spu_remark", new JSONObject());
		order.put("station_id", station_id);
		orders.add(order);
		try {
			response = FastHttpClient.post().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";cms_key=gm")
					.addParams("orders",orders.toString())
					.build().execute();
			JSONObject retObj = JSONObject.fromObject(response.string());
			String msg = retObj.getString("msg");
			logger.info("提交购物车");
		    assertEquals(msg, "ok","提交购物车失败");
		} catch (Exception e) {
			logger.error("submitCart:" + e.getMessage());
			fail("提交购物车出错");
		}
	}
	
	
	@AfterClass
	public static void tearDown(){
		GmUtil.bshopLogout();
		logger.info("Logout bshop system");
	}
	
	
	
	
	
	

}
