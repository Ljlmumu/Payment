package com.example.payment.wxapi;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.conn.util.InetAddressUtils;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * 创建人 : skyCracks<br>
 * 创建时间 : 2016-7-18上午11:02:34<br>
 * 版本 : [v1.0]<br>
 * 类描述 : 微信支付实现服务端操作及后续调起支付<br>
 */
@SuppressLint("DefaultLocale")
public class WeChatPayService {

	private static final String TAG = WeChatPayService.class.getSimpleName();

	private IWXAPI api;
	private Context context;
	/** 订单类型 */
	private int type;
	/** 内部订单 */
	private String out_trade_no;
	/** 商品描述 */
	private String body;
	/** 商品金额费用, 单位是分 */
	private String total_fee;

	/**
	 * @param context
	 *            上下文环境
	 * @param out_trade_no
	 *            内部订单
	 * @param type 
	 *            订单类型(不同订单类型区分) 只有一种类型的订单时可去掉
	 * @param body
	 *            商品描述
	 * @param total_fee
	 *            商品金额费用, 单位是分
	 */
	public WeChatPayService(Context context, int type, String out_trade_no,
			String body, String total_fee) {
		this.context = context;
		this.type = type;
		this.out_trade_no = out_trade_no;
		this.body = body;
		this.total_fee = total_fee;
		this.api = WXAPIFactory.createWXAPI(context, WeChatConstans.APP_ID,
				false);
	}

	public void pay() {
		// 检测是否安装了微信
		boolean isWeChat = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
		if (isWeChat) {
			new GetPrepayIdTask().execute();
		}
	}
	
	/**
	 * 异步网络请求获取预付Id
	 */
	private class GetPrepayIdTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onPostExecute(String result) {
			// 第三步, 发送支付请求
			sendPayReq(result);
		}

		@Override
		protected String doInBackground(String... params) {
			// 网络请求获取预付Id
			String url = String.format(WeChatConstans.WECHAT_UNIFIED_ORDER);
			String entity = genEntity();
			byte[] buf = WeChatHttpClient.httpPost(url, entity);
			if (buf != null && buf.length > 0) {
				try {
					Map map = XmlUtil.doXMLParse(new String(buf));
					return (String) map.get("prepay_id");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	/**
	 * 发送支付请求
	 * @param prepayId 预付Id
	 */
	private void sendPayReq(String prepayId) {
		PayReq req = new PayReq();
		req.appId = WeChatConstans.APP_ID;
		req.partnerId = WeChatConstans.PARTNER_ID;
		req.prepayId = prepayId;
		req.nonceStr = genNonceStr();
		req.timeStamp = String.valueOf(genTimeStamp());
		req.packageValue = "Sign=WXPay";

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = genPackageSign(signParams);
		// 传递的额外信息,字符串信息,自定义格式
		// req.extData = type +"#" + out_trade_no + "#" +total_fee;
		// 微信支付结果界面对调起支付Activity的处理
		// APPCache.payActivity.put("调起支付的Activity",(调起支付的Activity)context);
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		api.registerApp(WeChatConstans.APP_ID);
		api.sendReq(req);
		// 支付完成后微信会回调 wxapi包下 WXPayEntryActivity 的public void onResp(BaseResp
		// resp)方法，所以后续操作，放在这个回调函数中操作就可以了
	}

	/**
	 * 微信支付，构建统一下单请求参数
	 */
	public String genEntity() {
		String nonceStr = genNonceStr();
		List<NameValuePair> packageParams = new ArrayList<NameValuePair>();
		// APPID
		packageParams
				.add(new BasicNameValuePair("appid", WeChatConstans.APP_ID));
		// 商品描述
		packageParams.add(new BasicNameValuePair("body", body));
		// 商户ID
		packageParams.add(new BasicNameValuePair("mch_id",
				WeChatConstans.PARTNER_ID));
		// 随机字符串
		packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
		// 回调接口地址
		packageParams.add(new BasicNameValuePair("notify_url",
				WeChatConstans.NOTIFY_URL));
		// 我们的订单号
		packageParams.add(new BasicNameValuePair("out_trade_no", out_trade_no));
		// 提交用户端ip
		packageParams.add(new BasicNameValuePair("spbill_create_ip",
				getIPAddress()));
		BigDecimal totalFeeBig = new BigDecimal(total_fee);
		int totalFee = totalFeeBig.multiply(new BigDecimal(100)).intValue();
		// 总金额，单位为 分 !
		packageParams.add(new BasicNameValuePair("total_fee", String
				.valueOf(totalFee)));
		// 支付类型， APP
		packageParams.add(new BasicNameValuePair("trade_type", "APP"));
		// 生成签名
		String sign = genPackageSign(packageParams);
		packageParams.add(new BasicNameValuePair("sign", sign));
		String xmlstring = XmlUtil.toXml(packageParams);
		try {
			return new String(xmlstring.toString().getBytes(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 生成签名
	 */
	public static String genPackageSign(List<NameValuePair> params) {
		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < params.size(); i++) {
				sb.append(params.get(i).getName());
				sb.append('=');
				sb.append(params.get(i).getValue());
				sb.append('&');
			}
			sb.append("key=");
			sb.append(WeChatConstans.PARTNER_KEY);

			String packageSign = MD5Util.getMessageDigest(
					sb.toString().getBytes("utf-8")).toUpperCase();
			return packageSign;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 微信支付调用统一下单接口，随机字符串
	 */
	public static String genNonceStr() {
		try {
			Random random = new Random();
			String rStr = MD5Util.getMessageDigest(String.valueOf(
					random.nextInt(10000)).getBytes("utf-8"));
			return rStr;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 得到本机IP地址，WIFI下获取的是局域网IP，数据流量下获取的是公网IP
	 * 
	 * @return
	 */
	public static String getIPAddress() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			// 遍历所用的网络接口
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
				Enumeration<InetAddress> inet = nif.getInetAddresses();
				// 遍历每一个接口绑定的所有ip
				while (inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ip
									.getHostAddress())) {
						return ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, "获取本地ip地址失败", e);
		}
		return null;
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

}
