package com.example.mypoc;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.example.mypoc.NativeWebViewPlugin;

public class MainActivity extends BridgeActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    registerPlugin(NativeWebViewPlugin.class); // 👈 antes del super
    super.onCreate(savedInstanceState);
  }
}
