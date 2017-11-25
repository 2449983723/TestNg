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

public class StationTest {
	private static Logger logger = LoggerFactory.getLogger(StationTest.class);

	private static String stationUrl = null;
	private static String cookie = null;
	private static Response response = null;
	private static String group_id = null;
	private static int address_id = 0;
	private static int uid = 0;
	private static String beginDate = null;
	private static String startTime = null;
	private static String endTime = null;
	private static String service_time_id = null;
	private static JSONArray buyList = new JSONArray();

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
	public void searchCustomer() {
		logger.info("获取下单用户");
		String urlStr =  stationUrl + "/station/order/customer/search";
		try {
			response = FastHttpClient.get().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";" + "group_id=" + group_id).build().execute();
			JSONObject resObj = JSONObject.fromObject(response.string());
			assertEquals(resObj.getString("msg"), "ok", "获取用户列表失败");

			JSONArray customerList = resObj.getJSONObject("data").getJSONArray("list");
			urlStr = stationUrl +"/station/check_unpay";
			for (int i = 0; i < customerList.size(); i++) {
				address_id = customerList.getJSONObject(i).getInt("address_id");
				uid = customerList.getJSONObject(i).getInt("id");
				response = FastHttpClient.get().url(urlStr)
						.addHeader("cookie", "sessionid=" + cookie + ";" + "group_id=" + group_id)
						.addParams("address_id", String.valueOf(address_id)).build().execute();
				resObj = JSONObject.fromObject(response.string());
				if (resObj.getInt("code") != 0) {
					continue;
				} else {
					break;
				}
			}
		} catch (Exception e) {
			logger.error("获取用户列表出错: "+ e.getMessage());
			fail("获取用户列表出错");
		}
		logger.info("下单用户id: " + address_id);
	}

	@Test(priority = 1)
	public void getServiceTime() {
		try {
			String urlStr = stationUrl + "/station/order/service_time";
			logger.info("查询指定商户的运营时间  " + address_id);
			// 查询运营时间
			response = FastHttpClient.get().url(urlStr)
					.addHeader("cookie", "sessionid=" + cookie + ";" + "group_id=" + group_id)
					.addParams("address_id", String.valueOf(address_id)).build().execute();

			JSONObject resObj = JSONObject.fromObject(response.string());
			assertEquals(resObj.getString("msg"), "ok", "获取用户收货时间失败");

			JSONArray service_time_list = resObj.getJSONObject("data").getJSONArray("service_time");
			JSONObject service_time = service_time_list.getJSONObject(0);
			service_time_id = service_time.getString("_id");

			JSONObject receive_time_limit = service_time.getJSONObject("receive_time_limit");
			int s_span_time = receive_time_limit.getInt("s_span_time");
			Date date = new Date();
			DateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
			beginDate = formate.format(new Date(date.getTime() + s_span_time * 24 * 60 * 60 * 1000));

			startTime = receive_time_limit.getString("start");
			endTime = receive_time_limit.getString("end");

		} catch (Exception e) {
			logger.error("获取用户运行时间错误: " + e.getMessage());
			fail("获取用户运行时间错误");
		}
	}

	@Test(priority = 2)
	public void searchGoods() {
		//搜索商品查询关键词
		String[] searchSkus = new String[] { "c", "d", "g",  "j", "r"};
		logger.info("搜索下单商品");
		// 限制最大下单商品数
		int orderNum = 15;
		// 查询商品
		try {
			String urlStr = stationUrl + "/station/skus/addr";
			OK:
			for (String sku : searchSkus) {
				response = FastHttpClient.get().url(urlStr)
						.addHeader("cookie", "sessionid=" + cookie + ";" + "group_id=" + group_id)
						.addParams("address_id", String.valueOf(address_id)).addParams("offset", "0")
						.addParams("limit", "10").addParams("search_text", sku).addParams("fetch_category", "1")
						.addParams("active", "1").addParams("time_config_id", service_time_id).build().execute();

				JSONObject buyObj = null;

				JSONObject retJson = JSONObject.fromObject(response.string());
				assertEquals(retJson.getString("msg"), "ok", "查询下单商品失败");

				JSONArray spusList = retJson.getJSONArray("data");
				String sku_id = null;
				String spu_id = null;
				String sale_price = null;
				int amount = 1;
				JSONObject obj = null;
				int stocks = 0;
				int sale_num_least = 0;

				for (int j = 0; j < spusList.size(); j++) {
					obj = spusList.getJSONObject(j);
					stocks = obj.getInt("stocks");
					sale_num_least = obj.getInt("sale_num_least");
					if ((stocks > 0 && sale_num_least < stocks) || stocks == -99999) {
						buyObj = new JSONObject();
						sku_id = obj.getString("id");
						spu_id = obj.getString("spu_id");
						sale_price = obj.getString("sale_price");
						amount = obj.getInt("sale_num_least");
						buyObj.put("spu_id", spu_id);
						buyObj.put("sku_id", sku_id);
						buyObj.put("unit_price", sale_price);
						buyObj.put("amount", amount * 3);  //商品下单数量
						buyObj.put("spu_remark", "");
						buyList.add(buyObj);
					}
					if (buyList.size() >= orderNum) {
						break OK;
					}
				}
			}
			logger.info("下单商品列表: " + buyList.toString());
		} catch (Exception e) {
			logger.error("搜索下单商品出错: " + e.getMessage());
			fail("搜索下单商品出错");
		}
	}

	@Test(priority = 3)
	public void creatOrder() {
		// 开始下单
		logger.info("开始下单");
		if (buyList.size() > 0) {
			try {
				String urlStr = stationUrl + "/station/order/create";
				response = FastHttpClient.post().url(urlStr)
						.addHeader("cookie", "sessionid=" + cookie + ";" + "group_id=" + group_id)
						.addParams("details", buyList.toString()).addParams("address_id", String.valueOf(address_id))
						.addParams("uid", String.valueOf(uid))
						.addParams("receive_begin_time", beginDate + " " + startTime)
						.addParams("receive_end_time", beginDate + " " + endTime)
						.addParams("time_config_id", service_time_id).addParams("force", "1").build().execute();
				JSONObject resObj = JSONObject.fromObject(response.string());
				assertEquals(resObj.getString("msg"), "ok", "下单失败");
			} catch (Exception e) {
				logger.error("下单出错 : " + e.getMessage());
				fail("Station下单出错");
			}
		}
	}

	@AfterClass
	public static void tearDown() {
		GmUtil.stationLogout();
	}

}
