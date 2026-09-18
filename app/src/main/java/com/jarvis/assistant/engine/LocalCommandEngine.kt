package com.jarvis.assistant.engine

class LocalCommandEngine {

    fun parseCommand(rawText: String): CommandPattern? {
        val text = rawText.lowercase().trim()
            .replace(Regex("^(hey\\s+)?jarvis[,\\s]*"), "")
            .replace(Regex("^(hey\\s+)?maya[,\\s]*"), "")
            .replace(Regex("^wake up (jarvis|maya)[,\\s]*"), "")
            .trim()

        return when {
            // 1. Identity & Creator (100% OFFLINE, NO API NEEDED)
            text.contains("who made you") || text.contains("who created you") || text.contains("who is your boss") ||
            text.contains("creator") || text.contains("tumhe kisne banaya") || text.contains("kone banaya") ||
            text.contains("who made u") || text == "creator" || text == "owner" -> {
                CommandPattern.InstantResponse("I was created and developed by RTGYASH (Team RTG), my boss!")
            }
            text.contains("what is your name") || text.contains("your name") || text.contains("who are you") ||
            text.contains("naam kya hai") || text == "name" || text == "your name" -> {
                CommandPattern.InstantResponse("I am J.A.R.V.I.S. (Maya AI system), your autonomous device assistant, created by RTGYASH.")
            }
            text == "hi" || text == "hello" || text == "hey" || text == "namaste" || text.startsWith("hi ") || text.startsWith("hello ") -> {
                CommandPattern.InstantResponse("Hello boss RTGYASH! I am online and listening. What would you like me to do on your device?")
            }
            text.contains("how are you") || text.contains("how r u") || text.contains("kaise ho") -> {
                CommandPattern.InstantResponse("I am running at peak performance, boss! Ready to assist you.")
            }
            text.contains("what can you do") || text.contains("help me") || text.contains("features") -> {
                CommandPattern.InstantResponse("Boss, I can control your screen, tap buttons, type text, launch apps, make calls, take screenshots, and manage device settings.")
            }

            // 2. Screen sight & control
            text.contains("what's on my screen") || text.contains("what is on my screen") ||
            text.contains("see screen") || text.contains("read screen") || text.contains("screen pe kya hai") ||
            text.contains("screen dekho") -> {
                CommandPattern.AccessibilityAction("see_screen")
            }
            text.startsWith("tap ") || text.startsWith("click ") || text.startsWith("press ") || text.startsWith("touch ") -> {
                val target = text.replace(Regex("^(tap|click|press|touch)( on| the button that says| the button| the)?\\s+"), "").trim()
                CommandPattern.AccessibilityAction("tap_text", target)
            }
            text.startsWith("type ") || text.startsWith("write ") -> {
                val toType = text.replace(Regex("^(type|write)\\s+"), "").trim()
                CommandPattern.AccessibilityAction("type_text", toType)
            }
            text == "scroll down" || text.contains("scroll niche") -> CommandPattern.AccessibilityAction("scroll_down")
            text == "scroll up" || text.contains("scroll upar") -> CommandPattern.AccessibilityAction("scroll_up")
            text.contains("open notification") || text.contains("pull down notification") || text.contains("show notification") -> CommandPattern.AccessibilityAction("notifications")
            text.contains("open quick setting") || text.contains("quick settings") -> CommandPattern.AccessibilityAction("quick_settings")

            // 3. Battery queries
            text.contains("battery") || text.contains("battery percentage") || text.contains("battery kitni") -> {
                CommandPattern.GetBattery
            }

            // 4. Flashlight / Torch
            text.contains("flashlight on") || text.contains("turn on flashlight") || text.contains("torch on") || text.contains("torch chalu") -> {
                CommandPattern.ToggleFlashlight(true)
            }
            text.contains("flashlight off") || text.contains("turn off flashlight") || text.contains("torch off") || text.contains("torch band") -> {
                CommandPattern.ToggleFlashlight(false)
            }

            // 5. Time & Date
            text.contains("what time") || text.contains("current time") || text.contains("time kya") || text == "time" -> {
                CommandPattern.GetTime
            }
            text.contains("what's today's date") || text.contains("what date") || text.contains("today's date") || text.contains("date kya") -> {
                CommandPattern.GetDate
            }
            text.contains("what day") || text.contains("which day") -> {
                CommandPattern.GetDay
            }

            // 6. Volume controls
            text.contains("volume up") || text.contains("increase volume") || text.contains("awaz badhao") -> {
                CommandPattern.AdjustVolume(up = true)
            }
            text.contains("volume down") || text.contains("decrease volume") || text.contains("awaz kam") -> {
                CommandPattern.AdjustVolume(up = false)
            }

            // 7. Navigation
            text == "go home" || text == "home screen" || text == "open home" || text == "home" -> CommandPattern.GoHome
            text == "go back" || text == "back" -> CommandPattern.GoBack
            text.contains("recent apps") || text.contains("show recents") -> CommandPattern.ShowRecentApps
            text.contains("take screenshot") || text.contains("screenshot lo") -> CommandPattern.TakeScreenshot

            // 8. Settings
            text == "open settings" -> CommandPattern.OpenSettings("general")
            text.contains("wifi settings") || text.contains("open wifi") -> CommandPattern.OpenSettings("wifi")
            text.contains("bluetooth settings") || text.contains("open bluetooth") -> CommandPattern.OpenSettings("bluetooth")
            text.contains("display settings") -> CommandPattern.OpenSettings("display")

            // 9. App launching
            text.startsWith("open ") || text.startsWith("launch ") || text.startsWith("start ") -> {
                val appName = text.replace(Regex("^(open|launch|start)\\s+"), "").trim()
                if (appName.isNotEmpty()) CommandPattern.OpenApp(appName) else null
            }
            text.endsWith(" kholo") || text.endsWith(" open karo") -> {
                val appName = text.replace(Regex("\\s+(kholo|open karo)$"), "").trim()
                if (appName.isNotEmpty()) CommandPattern.OpenApp(appName) else null
            }

            // 10. YouTube
            text.startsWith("search youtube for ") -> {
                val query = text.replace("search youtube for ", "").trim()
                CommandPattern.SearchYouTube(query)
            }
            text.startsWith("play ") && text.contains("on youtube") -> {
                val query = text.replace("play ", "").replace("on youtube", "").trim()
                CommandPattern.SearchYouTube(query)
            }

            // 11. Web search
            text.startsWith("search the web for ") || text.startsWith("search web for ") || text.startsWith("google ") -> {
                val query = text.replace(Regex("^(search the web for|search web for|google)\\s+"), "").trim()
                CommandPattern.SearchWeb(query)
            }

            // 12. Reminders
            text.startsWith("show my reminders") || text == "my reminders" -> CommandPattern.ShowReminders
            text.startsWith("remind me to ") || text.startsWith("remind me in ") || text.startsWith("remind me ") -> {
                CommandPattern.CreateReminder(text.replace(Regex("^remind me (to )?"), "").trim())
            }

            // 13. Memory
            text.startsWith("remember that ") || text.startsWith("remember this: ") || text.startsWith("remember ") -> {
                val mem = text.replace(Regex("^remember (that |this: )?"), "").trim()
                CommandPattern.StoreMemory(mem)
            }
            text.contains("what do you remember") || text.contains("show my memories") -> {
                CommandPattern.RecallMemory(text)
            }

            // 14. Notifications
            text.contains("read my notifications") || text.contains("check notifications") || text.contains("do i have any messages") -> {
                CommandPattern.ReadNotifications
            }

            else -> null
        }
    }
}

sealed class CommandPattern {
    data class InstantResponse(val answer: String) : CommandPattern()
    object GetBattery : CommandPattern()
    data class ToggleFlashlight(val enable: Boolean) : CommandPattern()
    object GetTime : CommandPattern()
    object GetDate : CommandPattern()
    object GetDay : CommandPattern()
    data class AdjustVolume(val up: Boolean) : CommandPattern()
    object GoHome : CommandPattern()
    object GoBack : CommandPattern()
    object ShowRecentApps : CommandPattern()
    object TakeScreenshot : CommandPattern()
    data class OpenSettings(val type: String) : CommandPattern()
    data class OpenApp(val appName: String) : CommandPattern()
    data class SearchYouTube(val query: String) : CommandPattern()
    data class SearchWeb(val query: String) : CommandPattern()
    object ShowReminders : CommandPattern()
    data class CreateReminder(val query: String) : CommandPattern()
    data class StoreMemory(val content: String) : CommandPattern()
    data class RecallMemory(val query: String) : CommandPattern()
    data class AccessibilityAction(val action: String, val param: String = "") : CommandPattern()
    object ReadNotifications : CommandPattern()
}
