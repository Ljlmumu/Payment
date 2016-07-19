package com.example.payment;

import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.payment.alipay.AliPayService;
import com.example.payment.alipay.PayEntity;
import com.example.payment.alipay.PayResult;
import com.example.payment.wxapi.WeChatPayService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 相匹配的msg.what
     */
    private int payFlag = 11;
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 最后需要支付的金额
     */
    private String fastAmount;
    /**
     * 不同类型的订单
     */
    int type = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //模拟调起支付,实际项目中参照
        //findViewById(R.id.btn_alipay).setOnClickListener(this);
        //findViewById(R.id.btn_wechatpay).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_alipay:
                String subject = "测试名称";
                AliPayService.pay(new PayEntity(orderId, subject, "测试商品不描述", fastAmount), mHandler, payFlag, this);
                break;
            case R.id.btn_wechatpay:
                String body = "测试商品不描述";
                WeChatPayService weChatPay = new WeChatPayService(this, type, orderId, body, fastAmount);
                weChatPay.pay();
                break;
            default:
                break;
        }
    }

    /**
     * 支付宝支付异步通知
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == payFlag) {
                String resultStatus = new PayResult((String) msg.obj).getResultStatus();
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if ("9000".equals(resultStatus)) {
                    showShortToast("支付成功!");
                    //后续业务处理
                } else if ("8000".equals(resultStatus)) { //"8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    showShortToast("支付结果确认中");
                } else { //其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                    showShortToast("支付失败");
                }
            }
        }

        ;
    };

    private void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
