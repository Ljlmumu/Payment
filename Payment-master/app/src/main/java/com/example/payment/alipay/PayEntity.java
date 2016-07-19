package com.example.payment.alipay;


/**
 * 支付对象实体
 */
public class PayEntity {
	/**	商户网站唯一订单号 */
	private String out_trade_no;
	/** 商品名称 */
	private String subject;
	/** 商品详情描述 */
	private String body;
	/** 商品金额费用 */
	private String total_fee;
	
	public PayEntity() {
	}
	/**
	 * @param out_trade_no 商户网站唯一订单号
	 * @param subject 商品名称
	 * @param body 商品详情描述
	 * @param total_fee 商品金额费用
	 */
	public PayEntity(String out_trade_no, String subject, String body, String total_fee) {
		this.out_trade_no = out_trade_no;
		this.subject = subject;
		this.body = body;
		this.total_fee = total_fee;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(String total_fee) {
		this.total_fee = total_fee;
	}
}
