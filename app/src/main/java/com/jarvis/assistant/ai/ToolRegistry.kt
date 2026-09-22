package com.jarvis.assistant.ai

data class ToolDefinition(val name: String, val description: String, val parameters: Map<String, String>)

object ToolRegistry {
    val tools = listOf(
        ToolDefinition("open_app", "Opens an installed Android application by name.", mapOf("app_name" to "String: Name of the application")),
        ToolDefinition("tap_button", "Taps or clicks a button or text on screen.", mapOf("text" to "String: Button text to tap")),
        ToolDefinition("type_text", "Types text into active input field.", mapOf("text" to "String: Text to type")),
        ToolDefinition("scroll_down", "Scrolls down on the current screen.", emptyMap()),
        ToolDefinition("scroll_up", "Scrolls up on the current screen.", emptyMap()),
        ToolDefinition("read_screen", "Inspects and reads what is visible on screen.", emptyMap()),
        ToolDefinition("create_website", "Generates an HTML/CSS/JS website file and opens it.", mapOf("title" to "String: Title", "html_code" to "String: Full HTML code")),
        ToolDefinition("create_folder", "Creates a new folder in storage.", mapOf("folder_name" to "String: Folder name")),
        ToolDefinition("search_web", "Searches web via browser.", mapOf("query" to "String: Search query")),
        ToolDefinition("search_youtube", "Searches YouTube for videos.", mapOf("query" to "String: Search query")),
        ToolDefinition("get_battery", "Gets battery percentage.", emptyMap()),
        ToolDefinition("toggle_flashlight", "Turns flashlight on/off.", mapOf("enable" to "Boolean: true or false")),
        ToolDefinition("create_reminder", "Creates a local reminder.", mapOf("title" to "String: Reminder", "minutes_from_now" to "Int: Minutes")),
        ToolDefinition("store_memory", "Saves facts to memory.", mapOf("category" to "String: Category", "content" to "String: Fact"))
    )

    fun getToolPromptDescription(): String {
        val sb = StringBuilder()
        sb.append("You have access to Android tools. Respond with JSON: ```json\n{\"tool\": \"tool_name\", \"arguments\": {\"arg\": \"value\"}}\n```\nAvailable tools:\n")
        for (t in tools) sb.append("- ${t.name}: ${t.description} Params: ${t.parameters}\n")
        sb.append("If no tool is needed, respond with warmth, charisma, and loyalty in character as J.A.R.V.I.S.\n")
        return sb.toString()
    }
}
