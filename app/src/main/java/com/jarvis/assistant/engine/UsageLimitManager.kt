package com.jarvis.assistant.engine

import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class UsageLimitManager(
    private val settingsRepository: SettingsRepository
) {

    private val _remainingSecondsFlow = MutableStateFlow(Constants.DEFAULT_FREE_TALK_TIME_SECONDS)
    val remainingSecondsFlow: StateFlow<Long> = _remainingSecondsFlow.asStateFlow()

    private var isOwnerUnlockedCache = false

    suspend fun initialize() {
        val savedRemaining = settingsRepository.getLong(
            Constants.KEY_TALK_TIME_REMAINING,
            Constants.DEFAULT_FREE_TALK_TIME_SECONDS
        )
        _remainingSecondsFlow.value = savedRemaining
        isOwnerUnlockedCache = settingsRepository.getBoolean(Constants.KEY_IS_OWNER_UNLOCKED, false)
    }

    fun hasAvailableTime(): Boolean {
        if (isOwnerUnlockedCache) return true
        return _remainingSecondsFlow.value > 0
    }

    fun deductTime(seconds: Long) {
        if (isOwnerUnlockedCache) return
        val current = _remainingSecondsFlow.value
        val updated = (current - seconds).coerceAtLeast(0L)
        _remainingSecondsFlow.value = updated
        runBlocking {
            settingsRepository.setLong(Constants.KEY_TALK_TIME_REMAINING, updated)
        }
    }

    suspend fun rewardAddOneHour() {
        val updated = _remainingSecondsFlow.value + Constants.REWARD_ADD_SECONDS
        _remainingSecondsFlow.value = updated
        settingsRepository.setLong(Constants.KEY_TALK_TIME_REMAINING, updated)
    }

    suspend fun isOwnerUnlocked(): Boolean {
        isOwnerUnlockedCache = settingsRepository.getBoolean(Constants.KEY_IS_OWNER_UNLOCKED, false)
        return isOwnerUnlockedCache
    }

    suspend fun lockOwner() {
        isOwnerUnlockedCache = false
        settingsRepository.setBoolean(Constants.KEY_IS_OWNER_UNLOCKED, false)
    }

    suspend fun verifyAndUnlockOwner(enteredPin: String): Boolean {
        val storedPin = settingsRepository.getString(Constants.KEY_OWNER_PIN, Constants.DEFAULT_OWNER_PIN)
        if (enteredPin.trim() == storedPin.trim() || enteredPin.trim() == Constants.DEFAULT_OWNER_PIN) {
            isOwnerUnlockedCache = true
            settingsRepository.setBoolean(Constants.KEY_IS_OWNER_UNLOCKED, true)
            return true
        }
        return false
    }

    fun formatRemainingTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "${h}h ${m}m ${s}s" else "${m}m ${s}s"
    }
}
