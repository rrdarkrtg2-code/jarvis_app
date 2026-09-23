package com.jarvis.assistant.ai

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)

object ToolRegistry {

    val tools = listOf(
        ToolDefinition(
            name = "open_app",
            description = "Opens an installed Android application by name (e.g. YouTube, Instagram, WhatsApp, Chrome, Settings).",
            parameters = mapOf("app_name" to "String: Name of the application")
        ),
        ToolDefinition(
            name = "tap_button",
            description = "Taps or clicks a button, icon, post, or text on the current screen by its visible name.",
            parameters = mapOf("text" to "String: Text or button label to tap on (e.g. Comments, Follow, Search)")
        ),
        ToolDefinition(
            name = "type_text",
            description = "Types text into the active input box or comment field on screen.",
            parameters = mapOf("text" to "String: Text content to type")
        ),
        ToolDefinition(
            name = "scroll_down",
            description = "Scrolls down on the current screen or feed.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "scroll_up",
            description = "Scrolls up on the current screen or feed.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "read_screen",
            description = "Inspects and reads what is currently visible on the phone screen.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "create_website",
            description = "Generates a complete, beautiful HTML/CSS/JS website file and opens it for the user.",
            parameters = mapOf(
                "title" to "String: Title of the website",
                "html_code" to "String: Complete HTML, CSS and JS code for the website"
            )
        ),
        ToolDefinition(
            name = "create_folder",
            description = "Creates a new folder in device storage.",
            parameters = mapOf("folder_name" to "String: Name of the folder to create")
        ),
        ToolDefinition(
            name = "search_web",
            description = "Searches the web via browser for queries.",
            parameters = mapOf("query" to "String: The search query")
        ),
        ToolDefinition(
            name = "search_youtube",
            description = "Searches YouTube for videos or music.",
            parameters = mapOf("query" to "String: Search terms")
        ),
        ToolDefinition(
            name = "get_battery",
            description = "Gets current battery percentage and charging state.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "toggle_flashlight",
            description = "Turns device flashlight/torch on or off.",
            parameters = mapOf("enable" to "Boolean: true to turn on, false to turn off")
        ),
        ToolDefinition(
            name = "create_reminder",
            description = "Creates a local reminder with title and minutes from now.",
            parameters = mapOf(
                "title" to "String: Reminder message",
                "minutes_from_now" to "Int: Duration in minutes"
            )
        ),
        ToolDefinition(
            name = "store_memory",
            description = "Saves important facts or user preferences to long-term memory.",
            parameters = mapOf(
                "category" to "String: fact, preference, or project",
                "content" to "String: Detail to remember"
            )
        )
    )

    fun getToolPromptDescription(): String {
        val sb = StringBuilder()
        sb.append("You have access to the following Android tools. If the user intent requires performing an action on the phone, respond with a JSON block in the exact format:\n")
        sb.append("```json\n{\"tool\": \"tool_name\", \"arguments\": {\"arg\": \"value\"}}\n```\n")
        sb.append("Available tools:\n")
        for (t in tools) {
            sb.append("- ${t.name}: ${t.description} Params: ${t.parameters}\n")
        }
        sb.append("If no tool is required, provide a warm, charismatic, loving, and intelligent response in character as J.A.R.V.I.S.\n")
        return sb.toString()
    }
}
