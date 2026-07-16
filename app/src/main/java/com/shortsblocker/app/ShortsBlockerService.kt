package com.shortsblocker.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

/**
 * Watches the YouTube app's screen state. Whenever it detects that the
 * Shorts player is on screen, it performs a "back" action to exit it,
 * then keeps checking so Shorts can't just reopen instantly.
 *
 * This service only reads structural info (view IDs / class names) of
 * the currently visible screen while YouTube is in the foreground. It
 * does not log, store, or transmit anything.
 */
class ShortsBlockerService : AccessibilityService() {

    companion object {
        private const val TAG = "ShortsBlocker"
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val MIN_ACTION_INTERVAL_MS = 1200L
        private const val MAX_SCAN_DEPTH = 40

        // Substrings seen in YouTube's internal view IDs for the Shorts
        // player over various app versions. Matched case-insensitively
        // against the view's resource-id name, e.g. "reel_recycler".
        private val SHORTS_ID_MARKERS = listOf(
            "reel_recycler",
            "reel_player",
            "reel_watch",
            "reel_progress",
            "shorts_container",
            "shorts_player"
        )
    }

    private var lastActionTime = 0L
    private var lastDumpTime = 0L

    // TEMPORARY: set to true to log every view ID seen on the YouTube
    // screen, so we can find the real Shorts identifiers for this
    // YouTube version. Turn back off once detection is fixed.
    private val DEBUG_DUMP_IDS = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() != YOUTUBE_PACKAGE) return

        val now = System.currentTimeMillis()

        if (DEBUG_DUMP_IDS && now - lastDumpTime > 2000) {
            lastDumpTime = now
            val root = rootInActiveWindow
            if (root != null) {
                val ids = LinkedHashSet<String>()
                collectIds(root, ids, depth = 0)
                Log.d("ShortsBlockerDebug", "---- view IDs on screen (${ids.size}) ----")
                for (id in ids) Log.d("ShortsBlockerDebug", id)
                root.recycle()
            }
        }

        if (now - lastActionTime < MIN_ACTION_INTERVAL_MS) return

        val root = rootInActiveWindow ?: return
        try {
            if (containsShorts(root, depth = 0)) {
                lastActionTime = now
                Log.d(TAG, "Shorts detected — backing out")
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(applicationContext, "Shorts blocked", Toast.LENGTH_SHORT).show()
            }
        } finally {
            root.recycle()
        }
    }

    private fun collectIds(node: AccessibilityNodeInfo?, out: MutableSet<String>, depth: Int) {
        if (node == null || depth > MAX_SCAN_DEPTH) return
        val idPart = node.viewIdResourceName ?: "(no-id)"
        val classPart = node.className ?: ""
        val textPart = node.text?.toString()?.take(30) ?: ""
        val descPart = node.contentDescription?.toString()?.take(30) ?: ""
        if (idPart != "(no-id)" || textPart.isNotBlank() || descPart.isNotBlank()) {
            out.add("id=$idPart | class=$classPart | text=$textPart | desc=$descPart")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectIds(child, out, depth + 1)
            child.recycle()
        }
    }

    private fun containsShorts(node: AccessibilityNodeInfo?, depth: Int): Boolean {
        if (node == null || depth > MAX_SCAN_DEPTH) return false

        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (SHORTS_ID_MARKERS.any { viewId.contains(it) }) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = containsShorts(child, depth + 1)
            child.recycle()
            if (found) return true
        }
        return false
    }

    override fun onInterrupt() {
        // Required override; nothing to clean up.
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Shorts Blocker service connected")
    }
}
