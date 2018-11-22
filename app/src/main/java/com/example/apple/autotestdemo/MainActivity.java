package com.example.apple.autotestdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Timer;
import java.util.TimerTask;

/**
 * webView页面
 * Created by QZD on 2016/7/13.
 */
public class MainActivity extends Activity {
    private WebView webview;
    private String target;
    private boolean isLoading;
    private long lastRequestTime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.webview);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        /**
         * 监听WebView的加载状态    分别为 ： 加载的 前 中 后期
         * */
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isLoading = true;
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                isLoading = false;


            }


        });
        webview.loadUrl(Constant.url);

        requestJsoupData(Constant.url);
    }

    private void requestJsoupData(final String url) {
        //这里需要放在子线程中完成，否则报这个错android.os.NetworkOnMainThreadException
        new Thread(new Runnable() {
            @Override
            public void run() {
                jsoupListData(url);
            }
        }).start();
    }


    private void jsoupListData(String url) {

        try {//捕捉异常

            Document document = Jsoup.connect(url).get();//这里可用get也可以post方式，具体区别请自行了解
            Element content_left = document.getElementById("content_left");//左边所有条目

            Elements blank = content_left.getElementsByAttribute("target");
            for (Element element : blank) {

                String text = element.text();
                if (text.startsWith(Constant.test)) {
                    this.target = element.attr("href");
                    loadUrl();
                    return;
                }
            }

            Element page = document.getElementById("page");
            Element nextPage = page.select("a[href]").last();
            String text = nextPage.text();
            String href = nextPage.attr("href");

            if (!TextUtils.isEmpty(text) && text.startsWith("下一页")) {
                nextPage.text("next page");
                if (!TextUtils.isEmpty(href)) {
                    loadUrl(href);
                }
            }

            Log.i("lijing", "text == " + text + "   href == " + href);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadUrl(String url) {

        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            url = "https://www.baidu.com" + url;
        }

        final String finalUrl = url;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webview.loadUrl(finalUrl);
            }
        });


        while (isLoading) {
            SystemClock.sleep(50);
        }
        requestJsoupData(finalUrl);

    }


    private int i = 0;

    private void loadUrl() {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i % 2 == 0) {
                            webview.loadUrl(target);
                        } else {
                            webview.goBack();
                        }
                        i++;
                    }
                });
            }
        }, 0, 2000);

    }
}

