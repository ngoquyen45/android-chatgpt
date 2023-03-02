package com.infisdev.chatgpt

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.android.material.progressindicator.LinearProgressIndicator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var linearProgressIndicator: LinearProgressIndicator
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var backPressedOnce = false
    private val exitToast by lazy {
        Toast.makeText(
            applicationContext,
            "Press back again to exit",
            Toast.LENGTH_SHORT
        )
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility", "CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        linearProgressIndicator = findViewById(R.id.linearProgressIndicator)

        ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnectedToInternet ->
                onNetworkChanged(isConnectedToInternet)
            }
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    linearProgressIndicator.isVisible = true
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    linearProgressIndicator.isVisible = false
                    super.onPageFinished(view, url)
                    customWeb(url)
                }
            }
            addJavascriptInterface(BridgeWebView(), "android")
            loadUrl("https://chat.openai.com")
        }
        setupOnBack()
    }

    private fun onNetworkChanged(isConnectedToInternet: Boolean) {
        if (!isConnectedToInternet)
            webView.loadUrl("file:///android_asset/html/error.html")
        else
            webView.loadUrl("https://chat.openai.com/chat")
    }

    private fun setupOnBack() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.url?.contains(CHAT) == true) {
                    handleExit()
                    return
                }
                if (webView.canGoBack()) webView.goBack()
                else {
                    handleExit()
                }
            }
        })
    }

    private fun handleExit() {
        if (backPressedOnce) finish()
        backPressedOnce = true
        exitToast.show()
        handler.postDelayed({
            backPressedOnce = false
            exitToast.cancel()
        }, 2000)
    }

    private fun customWeb(url: String) {
        webView.evaluateJavascript(
            "window.getComputedStyle(document.body,null).getPropertyValue('background-color')"
        ) { value -> changeNavigationBarColor(value) }

        if (!url.contains(CHAT)) return
        webView.evaluateJavascript(
            """setInterval(function() {
            android.updateNavigation(window.getComputedStyle(document.body,null).getPropertyValue('background-color'))
                
            var element_hide = document.querySelector('#__next > div.overflow-hidden.w-full.h-full.relative > div > main > div.absolute.bottom-0.left-0.w-full.border-t.md\\:border-t-0.dark\\:border-white\\/20.md\\:border-transparent.md\\:dark\\:border-transparent.md\\:bg-vert-light-gradient.bg-white.dark\\:bg-gray-800.md\\:\\!bg-transparent.dark\\:md\\:bg-vert-dark-gradient > div');
            if (element_hide) { element_hide.style.display = 'none'; }
            var element_padding = document.querySelector('#__next > div.overflow-hidden.w-full.h-full.relative > div > main > div.absolute.bottom-0.left-0.w-full.border-t.md\\:border-t-0.dark\\:border-white\\/20.md\\:border-transparent.md\\:dark\\:border-transparent.md\\:bg-vert-light-gradient.bg-white.dark\\:bg-gray-800.md\\:\\!bg-transparent.dark\\:md\\:bg-vert-dark-gradient > form');
            if (element_padding) { element_padding.style.paddingBottom = '0.5rem'; }
            
            var selector = document.querySelector("#__next > div.overflow-hidden.w-full.h-full.relative > div > div");
            if (!selector) return;
            if (selector.querySelector("#my-button")) return;
            var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            svg.setAttribute("stroke", "currentColor");
            svg.setAttribute("fill", "none");
            svg.setAttribute("stroke-width", "2");
            svg.setAttribute("viewBox", "0 0 24 24");
            svg.setAttribute("stroke-linecap", "round");
            svg.setAttribute("stroke-linejoin", "round");
            svg.setAttribute("class", "h-4 w-4");
            svg.setAttribute("height", "1em");
            svg.setAttribute("width", "1em");

            var polyline1 = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
            polyline1.setAttribute("points", "1 4 1 10 7 10");
            svg.appendChild(polyline1);

            var polyline2 = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
            polyline2.setAttribute("points", "23 20 23 14 17 14");
            svg.appendChild(polyline2);

            var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
            path.setAttribute("d", "M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15");
            svg.appendChild(path);

            var button = document.createElement("button");
            button.setAttribute("type", "button");
            button.setAttribute("class", "px-3");
            button.setAttribute('id', 'my-button');
            button.appendChild(svg);
            button.addEventListener('click', function() {
              location.reload();
            });
            selector.appendChild(button);
            
            var button2 = button.cloneNode(true);
            button2.style.visibility = "hidden";
            selector.insertBefore(button2, selector.getElementsByTagName('h1')[0]);
            
            
        }, 1000);""".trimIndent()
        ) { value -> Log.d("Evaluate Javascript", value) }
    }

    private fun changeNavigationBarColor(color: String) {
        synchronized(this) {
            if (color == "rgba(0, 0, 0, 0)") {
                changeNavigationBarColor(Color.WHITE)
            } else {
                changeNavigationBarColor(ContextCompat.getColor(applicationContext, R.color.status_bar_color))
            }
        }
    }

    private fun changeNavigationBarColor(colorInt: Int) {
        window.navigationBarColor = colorInt
    }

    enum class NetworkType {
        WIFI,
        CELLULAR,
        UNKNOWN
    }

    private fun getNetworkType(): NetworkType {
        val manager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = manager.getNetworkCapabilities(manager.activeNetwork)
        return when {
            caps != null && caps.hasTransport(TRANSPORT_WIFI) -> NetworkType.WIFI
            caps != null && caps.hasTransport(TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> NetworkType.UNKNOWN
        }
    }

    inner class BridgeWebView {
        @JavascriptInterface
        fun updateNavigation(value: String) {
            handler.post { changeNavigationBarColor(value) }
        }

        @JavascriptInterface
        fun openWifiSetting() {
            when(getNetworkType()) {
                NetworkType.WIFI -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                NetworkType.CELLULAR -> startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))
                NetworkType.UNKNOWN -> startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }

    companion object {
        const val CHAT = "chat.openai.com/chat"
    }
}
