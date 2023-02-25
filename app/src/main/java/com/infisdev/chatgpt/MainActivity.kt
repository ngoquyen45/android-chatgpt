package com.infisdev.chatgpt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.LinearProgressIndicator


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

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        linearProgressIndicator = findViewById(R.id.linearProgressIndicator)

        webView.apply {
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
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            val jsInterface = MyJavaScriptInterface(applicationContext)
            webView.addJavascriptInterface(jsInterface, "Android")
            loadUrl("https://chat.openai.com")
        }

        setupOnBack()
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
        if (!url.contains(CHAT)) return

        // Loại bỏ phần thừa của web
        webView.evaluateJavascript(
            "window.getComputedStyle(document.body,null).getPropertyValue('background-color')"
        ) { value -> changeNavigationBarColor(value) }

        webView.evaluateJavascript(
            """var element = document.querySelector('#__next > div.overflow-hidden.w-full.h-full.relative > div.dark.hidden.bg-gray-900.md\\:fixed.md\\:inset-y-0.md\\:flex.md\\:w-\\[260px\\].md\\:flex-col > div > div > nav > a:nth-child(5)');
            element.addEventListener('click', function() {
                setTimeout(function() {
                    Android.showToast("The element was clicked!");
                }, 2000);
            });
        """.trimIndent()
        ) { value -> Log.d("Evaluate Javascript", value) }

        webView.evaluateJavascript(
            """setInterval(function() {
            var element_hide = document.querySelector('#__next > div.overflow-hidden.w-full.h-full.relative > div > main > div.absolute.bottom-0.left-0.w-full.border-t.md\\:border-t-0.dark\\:border-white\\/20.md\\:border-transparent.md\\:dark\\:border-transparent.md\\:bg-vert-light-gradient.bg-white.dark\\:bg-gray-800.md\\:\\!bg-transparent.dark\\:md\\:bg-vert-dark-gradient > div');
            if (element_hide) { element_hide.style.display = 'none'; }
            var element_padding = document.querySelector('#__next > div.overflow-hidden.w-full.h-full.relative > div > main > div.absolute.bottom-0.left-0.w-full.border-t.md\\:border-t-0.dark\\:border-white\\/20.md\\:border-transparent.md\\:dark\\:border-transparent.md\\:bg-vert-light-gradient.bg-white.dark\\:bg-gray-800.md\\:\\!bg-transparent.dark\\:md\\:bg-vert-dark-gradient > form');
            if (element_padding) { element_padding.style.paddingBottom = '0.5rem'; }
        }, 1000);""".trimIndent()
        ) { value -> Log.d("Evaluate Javascript", value) }

        webView.evaluateJavascript(
            """setInterval(function() {
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
        }, 1000);""".trimIndent()
        ) { value -> Log.d("Evaluate Javascript", value) }
    }

    private fun changeNavigationBarColor(color: String) {
        if (color == "rgba(0, 0, 0, 0)") {
            changeNavigationBarColor(Color.WHITE)
        } else {
            changeNavigationBarColor(ContextCompat.getColor(applicationContext, R.color.status_bar_color))
        }
    }

    private fun changeNavigationBarColor(white: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = white
    }

    class MyJavaScriptInterface internal constructor(var mContext: Context) {
        @JavascriptInterface
        fun showToast(message: String?) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        const val CHAT = "chat.openai.com/chat"
    }
}
