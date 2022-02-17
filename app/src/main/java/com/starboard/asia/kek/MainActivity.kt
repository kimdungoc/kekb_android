package com.starboard.asia.kek

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    private lateinit var webView: WebView
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var overlayView: View
    private var handler: Handler? = null
    private val whiteIPAddresses = listOf(
        "133.242.249.145",
        "68.183.204.44"
    );
    private val whiteHosts = listOf("mpembed.com", "m2r.life")
    private val useIPRegistration = false

    /// Callback will be invoked after 3 minutes no interacted with the WebView.
    private val runnable = Runnable {
        webView.loadUrl(KEK_URL)
        progressBar.visibility = View.VISIBLE
        overlayView.visibility = View.VISIBLE
        textView.visibility = View.GONE
        webView.visibility = View.GONE
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        overlayView = findViewById(R.id.overlayView)

        webView.setOnTouchListener(this)

        // ---- Setting WebView ----
        webView.settings.apply {
            javaScriptEnabled = true
        }

        webView.webViewClient = KekWebViewClient()

        webView.loadUrl(KEK_URL)
        // ---- End Setting WebView ----

        Log.d(TAG, "IP address: ${Utils.getIPAddress(true)}")

        /// IP address restrictions
        val ipAddress = Utils.getIPAddress(true);
        if (whiteIPAddresses.contains(ipAddress) || !useIPRegistration) {
            textView.setOnClickListener(this)
        }
        
        Log.d(TAG, "userAgentString: ${webView.settings.userAgentString}")
    }

    /// If user didn't interact with WebView within 3 minutes,
    /// the WebView will reload the original page and `タッチして覗いてみよう` text
    /// should display.
    private fun startTimer() {
        handler = Handler(Looper.getMainLooper())
        handler!!.postDelayed(runnable, LIMIT_INTERACT_MILLI)
    }

    /// If user interact with WebView within 3 minutes, stop the timer.
    private fun stopTimer() {
        handler?.removeCallbacks(runnable)
    }

    /// `タッチして覗いてみよう` click event.
    override fun onClick(v: View?) {
        if (v?.id == R.id.textView) {
            startTimer()
            textView.visibility = View.GONE
            overlayView.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }
    /// WebView touch event.
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {
        if (v?.id == R.id.webView) {
            when (motionEvent?.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "ACTION_DOWN")
                    stopTimer()
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "ACTION_UP")
                    startTimer()
                }
            }
        }

        return false
    }

    /// WebViewClient class.
    private inner class KekWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG,"onPageStarted: $url")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d(TAG,"onPageFinished: $url")
            progressBar.visibility = View.GONE
            webView.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            Log.d(TAG, "onLoadResource: $url")
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            Log.d(TAG,"onReceivedError: $error")
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Log.d(TAG, "shouldOverrideUrlLoading: ${view?.url}, $request")

            // Domain restrictions
            val host = Uri.parse(view?.url).host
            if (view?.url == KEK_URL || whiteHosts.contains(host)) {
                // This is my web site, so do not override; let my WebView load the page.
                return false
            }

            // Reload the original URL if there is an disallowed url.
            view?.loadUrl(KEK_URL)
            progressBar.visibility = View.VISIBLE
            overlayView.visibility = View.VISIBLE
            textView.visibility = View.GONE
            webView.visibility = View.GONE
            return true
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            Log.d(TAG, "onReceivedHttpError")
        }
    }

    companion object {
        private const val KEK_URL = "https://my.matterport.com/show/?m=UUwYUiCdfTa"
        private const val LIMIT_INTERACT_MILLI: Long = 180000 // 3 minutes
        private const val TAG = "KEKB"
    }
}