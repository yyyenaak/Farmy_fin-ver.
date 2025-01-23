package com.uniproject.farmyapp;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class gpt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpt);

        WebView webView = findViewById(R.id.web_view);

        // WebView 설정
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // JavaScript 활성화
        webSettings.setDomStorageEnabled(true); // DOM Storage 활성화 (필요 시)

        // WebViewClient를 설정하여 외부 브라우저가 아닌 WebView 내에서 URL이 열리도록 설정
        webView.setWebViewClient(new WebViewClient());

        // 외부 URL 로드
        webView.loadUrl("https://chatgpt.com/g/g-vCcW2Nq1m-farmy"); // 원하는 외부 URL로 변경
    }
}
