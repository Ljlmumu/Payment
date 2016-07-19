package com.example.payment.alipay;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * 创建人 : skyCracks<br>
 * 创建时间 : 2016-7-18上午11:20:08<br>
 * 版本 :	[v1.0]<br>
 * 类描述 : 支付宝支付实现服务端操作及后续调起支付<br>
 */
public class AliPayService {
	
	/**
	 * 支付, 唯一主要方法
	 * @param payEntity 支付商品对象
	 * @param mHandler  支付结果回调
	 * @param payFlag	mHandler what
	 * @param activity  调起支付Activity
	 */
	public static void pay(PayEntity payEntity, final Handler mHandler, final int payFlag, final Activity activity){
		String orderInfo = getOrderInfo(payEntity);
		String sign = getSign(orderInfo, AliPayConstans.RSA_PRIVATE); // 签名
		try { 
			sign = URLEncoder.encode(sign, "UTF-8"); // 仅需对sign 做URL编码
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&sign_type=\"RSA\"";
		new Thread(){
			@Override
			public void run() {
				PayTask alipay = new PayTask(activity); // 构造PayTask 对象
				String result = alipay.pay(payInfo, true); // 调用支付接口，获取支付结果
				Message msg = new Message();
				msg.what = payFlag;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		}.start();
	}
	
	/**
	 * 获取签名
	 */
	public static String getSign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature.getInstance("SHA1WithRSA");

			signature.initSign(priKey);
			signature.update(content.getBytes("UTF-8"));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 创建订单信息
	 * @param payEntity 订单支付实体
	 */
	private static String getOrderInfo(PayEntity payEntity){
		StringBuffer orderInfo = new StringBuffer();
		// 签约合作者身份ID
		orderInfo.append("partner=" + "\"" + AliPayConstans.PARTNER + "\"");
		// 签约卖家支付宝账号
		orderInfo.append("&seller_id=" + "\"" + AliPayConstans.SELLER + "\"");
		// 商户网站唯一订单号
		orderInfo.append("&out_trade_no=" + "\"" + payEntity.getOut_trade_no() + "\"");
		// 商品名称
		orderInfo.append("&subject=" + "\"" + payEntity.getSubject() + "\"");
		// 商品详情
		orderInfo.append("&body=" + "\"" + payEntity.getBody() + "\"");
		// 商品金额
		orderInfo.append("&total_fee=" + "\"" + payEntity.getTotal_fee() + "\"");
		// 服务器异步通知页面路径
		orderInfo.append("&notify_url=" + "\"" + AliPayConstans.NOTIFY_URL  + "\"");
		// 服务接口名称， 固定值
		orderInfo.append("&service=\"mobile.securitypay.pay\"");
		// 支付类型， 固定值
		orderInfo.append("&payment_type=\"1\"");
		// 参数编码， 固定值
		orderInfo.append("&_input_charset=\"utf-8\"");
		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo.append("&it_b_pay=\"30m\"");
		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo.append("&return_url=\"m.alipay.com\"");
		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";
		return orderInfo.toString();
	}
}
