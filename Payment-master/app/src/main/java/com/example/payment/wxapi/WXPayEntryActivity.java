package com.example.payment.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.payment.R;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
	
    private IWXAPI api;
    
    /**
	 * 支付回调
	 */
	private static final int PAY_CALLBACK_REQUESTCODE = 101;
	/**
	 * 支付类型
	 */
	private String type;
	/**
	 * 订单号
	 */
	private String orderId;
	/**
	 * 支付费用
	 */
	private String fee;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        String data = getIntent().getStringExtra("_wxapi_payresp_extdata");
        String[] str = data.split("#");
        type = str[0];
        orderId = str[1];
        fee = str[2];
    	api = WXAPIFactory.createWXAPI(this, WeChatConstans.APP_ID);
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		
	}

	@Override
	public void onResp(BaseResp resp) {
		// 支付结果回调...
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if(resp.errCode == 0){//支付成功
				
			}else{
				WXPayEntryActivity.this.finish();
			}
		}else{
			WXPayEntryActivity.this.finish();
		}
	}

	
}