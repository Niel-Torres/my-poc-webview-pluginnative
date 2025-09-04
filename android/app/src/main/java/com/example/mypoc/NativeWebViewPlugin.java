package com.example.mypoc;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;           // 👈 C7 usa esta anotación
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NativeWebView")
public class NativeWebViewPlugin extends Plugin {

  private FrameLayout container;
  private WebView webView;

  @Override
  public void load() {
    super.load();
    container = new FrameLayout(getActivity());
    container.setBackgroundColor(Color.TRANSPARENT);
    container.setVisibility(View.GONE);

    getActivity().runOnUiThread(() -> {
      getActivity().addContentView(
        container,
        new FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.WRAP_CONTENT,
          FrameLayout.LayoutParams.WRAP_CONTENT
        )
      );
    });
  }

  @PluginMethod
  public void create(PluginCall call) {
    String url = call.getString("url");
    if (url == null) { call.reject("url required"); return; }

    int x = (int) Math.round(call.getDouble("x", 0.0));
    int y = (int) Math.round(call.getDouble("y", 0.0));
    int w = (int) Math.round(call.getDouble("width", 0.0));
    int h = (int) Math.round(call.getDouble("height", 0.0));

    getActivity().runOnUiThread(() -> {
      if (webView == null) {
        webView = new WebView(getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          webView.getSettings().setSafeBrowsingEnabled(true);
        }
        webView.setWebViewClient(new WebViewClient(){});
        webView.setBackgroundColor(Color.WHITE);

        container.addView(
          webView,
          new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
        );
      }
      setLayout(x, y, w, h);
      container.setVisibility(View.VISIBLE);
      webView.loadUrl(url);
      call.resolve();
    });
  }

  @PluginMethod
  public void setRect(PluginCall call) {
    int x = (int) Math.round(call.getDouble("x", 0.0));
    int y = (int) Math.round(call.getDouble("y", 0.0));
    int w = (int) Math.round(call.getDouble("width", 0.0));
    int h = (int) Math.round(call.getDouble("height", 0.0));
    getActivity().runOnUiThread(() -> { setLayout(x,y,w,h); call.resolve(); });
  }

  @PluginMethod public void show(PluginCall call)   { getActivity().runOnUiThread(() -> { if (container!=null) container.setVisibility(View.VISIBLE); call.resolve(); }); }
  @PluginMethod public void hide(PluginCall call)   { getActivity().runOnUiThread(() -> { if (container!=null) container.setVisibility(View.GONE);   call.resolve(); }); }
  @PluginMethod public void destroy(PluginCall call){ getActivity().runOnUiThread(() -> {
    if (webView != null) { container.removeView(webView); webView.destroy(); }
    webView = null;
    if (container != null) container.setVisibility(View.GONE);
    call.resolve();
  });}

  private void setLayout(int x, int y, int w, int h) {
    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
    if (lp == null) lp = new FrameLayout.LayoutParams(w, h); else { lp.width = w; lp.height = h; }
    lp.leftMargin = x; lp.topMargin = y;
    container.setLayoutParams(lp);
    container.requestLayout();
  }

  @Override protected void handleOnPause()  { super.handleOnPause();  if (webView != null) webView.onPause(); }
  @Override protected void handleOnResume() { super.handleOnResume(); if (webView != null) webView.onResume(); }
  @Override protected void handleOnDestroy(){ super.handleOnDestroy(); if (webView != null) { webView.destroy(); webView = null; } }
}
