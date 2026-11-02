package com.example.data

import com.example.model.IslandEventItem
import com.example.model.IslandEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe, event-driven state controller for the Dynamic Island queue and real service diagnostics.
 * Avoids any background polling loops; updates strictly when system notifications, battery events,
 * or user actions occur.
 */
object IslandStateController {

    private val _eventQueue = MutableStateFlow<List<IslandEventItem>>(emptyList())
    val eventQueue: StateFlow<List<IslandEventItem>> = _eventQueue.asStateFlow()

    private val _selectedQueueIndex = MutableStateFlow(0)
    val selectedQueueIndex: StateFlow<Int> = _selectedQueueIndex.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    // Real-time service & lifecycle flags (never faked)
    private val _isNotificationListenerConnected = MutableStateFlow(false)
    val isNotificationListenerConnected: StateFlow<Boolean> = _isNotificationListenerConnected.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(false)
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    private val _isIslandWindowVisible = MutableStateFlow(false)
    val isIslandWindowVisible: StateFlow<Boolean> = _isIslandWindowVisible.asStateFlow()

    private val _isDeviceLocked = MutableStateFlow(false)
    val isDeviceLocked: StateFlow<Boolean> = _isDeviceLocked.asStateFlow()

    // Dismissal callback registered by NexaNotificationListenerService when connected
    var onDismissSystemNotification: ((String) -> Unit)? = null

    fun setListenerConnected(connected: Boolean) {
        _isNotificationListenerConnected.value = connected
    }

    fun setOverlayServiceRunning(running: Boolean) {
        _isOverlayServiceRunning.value = running
        if (!running) {
            _isIslandWindowVisible.value = false
        }
    }

    fun setIslandWindowVisible(visible: Boolean) {
        _isIslandWindowVisible.value = visible
    }

    fun setDeviceLocked(locked: Boolean) {
        _isDeviceLocked.value = locked
        if (locked) {
            // Collapse expanded card when screen locks for security
            _isExpanded.value = false
        }
    }

    fun setExpanded(expanded: Boolean) {
        _isExpanded.value = expanded
    }

    fun toggleExpanded() {
        _isExpanded.update { !it }
    }

    /**
     * Enqueues or updates a real notification/event with duplicate prevention and optional same-app grouping.
     */
    fun postOrUpdateEvent(
        item: IslandEventItem,
        groupSameApp: Boolean = true,
        autoExpand: Boolean = false
    ) {
        // Ignore empty notifications unless it's a special system state (charging, call, timer)
        if (item.eventType == IslandEventType.NOTIFICATION &&
            item.title.isBlank() && item.message.isBlank()
        ) {
            return
        }

        _eventQueue.update { currentList ->
            val mutable = currentList.toMutableList()

            // 1. Check exact key match (update in place without creating duplicates)
            val existingKeyIdx = mutable.indexOfFirst { it.key == item.key }
            if (existingKeyIdx >= 0) {
                val previous = mutable[existingKeyIdx]
                // If content is identical within 1.5s, keep existing timestamp to avoid jitter
                if (previous.title == item.title &&
                    previous.message == item.message &&
                    previous.eventType == item.eventType &&
                    (item.timestamp - previous.timestamp) < 1500L
                ) {
                    return@update currentList
                }
                mutable[existingKeyIdx] = item.copy(groupCount = previous.groupCount)
                return@update mutable
            }

            // 2. Check duplicate content from the same package within 2 seconds
            val recentDuplicate = mutable.any {
                it.packageName == item.packageName &&
                    it.title == item.title &&
                    it.message == item.message &&
                    kotlin.math.abs(item.timestamp - it.timestamp) < 2000L
            }
            if (recentDuplicate) {
                return@update currentList
            }

            // 3. Optional grouping by same application for standard notifications
            if (groupSameApp && item.eventType == IslandEventType.NOTIFICATION) {
                val sameAppIdx = mutable.indexOfFirst {
                    it.packageName == item.packageName &&
                        it.eventType == IslandEventType.NOTIFICATION
                }
                if (sameAppIdx >= 0) {
                    val existing = mutable[sameAppIdx]
                    val newCount = existing.groupCount + 1
                    val updatedItem = item.copy(
                        groupCount = newCount
                    )
                    mutable.removeAt(sameAppIdx)
                    mutable.add(0, updatedItem)
                    return@update mutable.take(15)
                }
            }

            // 4. Insert highest-priority or newest item at the front of the queue (cap at 15)
            mutable.add(0, item)
            mutable.take(15)
        }

        _selectedQueueIndex.value = 0
        if (autoExpand || item.eventType == IslandEventType.INCOMING_CALL) {
            _isExpanded.value = true
        }
    }

    fun removeEventByKey(key: String) {
        _eventQueue.update { current ->
            val filtered = current.filterNot { it.key == key }
            filtered
        }
        val size = _eventQueue.value.size
        if (size == 0) {
            _selectedQueueIndex.value = 0
            _isExpanded.value = false
        } else if (_selectedQueueIndex.value >= size) {
            _selectedQueueIndex.value = size - 1
        }
    }

    fun dismissCurrentEvent(notifySystemListener: Boolean = true) {
        val list = _eventQueue.value
        val idx = _selectedQueueIndex.value.coerceIn(0, (list.size - 1).coerceAtLeast(0))
        val target = list.getOrNull(idx) ?: return
        removeEventByKey(target.key)
        if (notifySystemListener) {
            onDismissSystemNotification?.invoke(target.key)
        }
    }

    fun selectNextEvent() {
        val size = _eventQueue.value.size
        if (size > 1) {
            _selectedQueueIndex.update { (it + 1) % size }
        }
    }

    fun selectPreviousEvent() {
        val size = _eventQueue.value.size
        if (size > 1) {
            _selectedQueueIndex.update { if (it - 1 < 0) size - 1 else it - 1 }
        }
    }

    fun clearAllEvents() {
        _eventQueue.value = emptyList()
        _selectedQueueIndex.value = 0
        _isExpanded.value = false
    }
}
