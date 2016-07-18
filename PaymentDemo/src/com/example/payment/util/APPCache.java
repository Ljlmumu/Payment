package com.example.payment.util;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

/**
 * 项目名称 : PaymentDemo<br>
 * 创建人 : dgs<br>
 * 创建时间 : 2016-7-18上午10:57:58<br>
 * 版本 :	[v1.0]<br>
 * 类描述 : 用一段话描述类<br>
 */
public class APPCache {

	/**
	 * 微信支付发起Activity缓存
	 */
	public static Map<String,Activity> payActivity = new HashMap<String,Activity>();
}
