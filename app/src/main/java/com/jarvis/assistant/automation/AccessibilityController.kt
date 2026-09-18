package com.jarvis.assistant.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

data class ScreenElement(
    val text: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val className: String
)

object AccessibilityController {

    private var serviceRef: AccessibilityService? = null

    fun setServiceInstance(service: AccessibilityService?) {
        serviceRef = service
    }

    fun isServiceEnabled(): Boolean = serviceRef != null

    fun performGlobal(action: Int): Boolean {
        val service = serviceRef ?: return false
        return service.performGlobalAction(action)
    }

    fun goHome(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_HOME)
    fun goBack(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_BACK)
    fun showRecents(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_RECENTS)
    fun openNotifications(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    fun openQuickSettings(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    fun takeScreenshot(): Boolean = performGlobal(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)

    fun scroll(forward: Boolean): Boolean {
        val service = serviceRef ?: return false
        val root = service.rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root) ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return scrollableNode.performAction(action)
    }

    // Vision / Screen Sight: J.A.R.V.I.S. inspects and reads the active screen
    fun seeCurrentScreen(): String {
        val service = serviceRef ?: return "Accessibility automation is disabled. Please enable J.A.R.V.I.S. in Android Accessibility Settings so I can see and control your screen, Sir."
        val root = service.rootInActiveWindow ?: return "I cannot see the screen right now. Please make sure an app window is active."

        val pkgName = root.packageName?.toString() ?: "System"
        val elements = mutableListOf<ScreenElement>()
        collectScreenElements(root, elements)

        if (elements.isEmpty()) {
            return "I am looking at , but no readable text or buttons were detected on screen."
        }

        val buttons = elements.filter { it.isClickable && it.text.isNotBlank() }.map { it.text }.distinct()
        val texts = elements.filter { !it.isClickable && it.text.isNotBlank() }.map { it.text }.distinct()
        val inputs = elements.filter { it.isEditable }

        val sb = StringBuilder()
        sb.append("Looking at ").append(pkgName.substringAfterLast('.').replaceFirstChar { it.uppercase() }).append(". ")

        if (buttons.isNotEmpty()) {
            sb.append("Buttons visible: ").append(buttons.take(6).joinToString(", ")).append(". ")
        }
        if (inputs.isNotEmpty()) {
            sb.append("Input field detected. ")
        }
        if (texts.isNotEmpty()) {
            sb.append("Screen content: ").append(texts.take(5).joinToString("; "))
        }

        return sb.toString()
    }

    // Full screen control: Click any button or element by name / text
    fun clickByText(query: String): String {
        val service = serviceRef ?: return "Accessibility is disabled. Enable J.A.R.V.I.S. in Accessibility Settings to control the screen."
        val root = service.rootInActiveWindow ?: return "Could not access screen."

        val target = query.lowercase().trim()
        val matchingNode = findNodeMatching(root, target)

        if (matchingNode != null) {
            var clickable: AccessibilityNodeInfo? = matchingNode
            while (clickable != null && !clickable.isClickable) {
                clickable = clickable.parent
            }
            val nodeToClick = clickable ?: matchingNode
            nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return "Tapped '' on your screen, Sir."
        }

        return "I could not find '' on your active screen, Sir."
    }

    // Type text into the active input field
    fun typeText(text: String): String {
        val service = serviceRef ?: return "Accessibility is disabled."
        val root = service.rootInActiveWindow ?: return "Could not access screen."

        val editable = findEditableNode(root)
        if (editable != null) {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            return "Entered text into the field, Sir."
        }

        return "No active input field was found on the screen, Sir."
    }

    // Tap exact screen coordinate (X, Y)
    fun tapCoordinate(x: Float, y: Float): String {
        val service = serviceRef ?: return "Accessibility is disabled."
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        val dispatched = service.dispatchGesture(gesture, null, null)
        return if (dispatched) "Tapped coordinates (, ), Sir." else "Could not dispatch tap gesture."
    }

    // Swipe / drag gesture across screen
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300L): String {
        val service = serviceRef ?: return "Accessibility is disabled."
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()
        val dispatched = service.dispatchGesture(gesture, null, null)
        return if (dispatched) "Swipe executed, Sir." else "Could not dispatch swipe gesture."
    }

    private fun findNodeMatching(node: AccessibilityNodeInfo?, query: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val nodeText = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""

        if (nodeText.contains(query) || desc.contains(query)) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val match = findNodeMatching(child, query)
            if (match != null) return match
        }

        return null
    }

    private fun findEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val editable = findEditableNode(child)
            if (editable != null) return editable
        }
        return null
    }

    private fun collectScreenElements(node: AccessibilityNodeInfo?, list: MutableList<ScreenElement>) {
        if (node == null) return

        val text = node.text?.toString()?.trim() ?: ""
        val desc = node.contentDescription?.toString()?.trim() ?: ""
        val effectiveText = if (text.isNotEmpty()) text else desc

        if (effectiveText.isNotEmpty() && !node.isPassword) {
            list.add(
                ScreenElement(
                    text = effectiveText,
                    isClickable = node.isClickable,
                    isEditable = node.isEditable,
                    className = node.className?.toString() ?: ""
                )
            )
        }

        for (i in 0 until node.childCount) {
            collectScreenElements(node.getChild(i), list)
        }
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val result = findScrollableNode(node.getChild(i))
            if (result != null) return result
        }
        return null
    }
}
