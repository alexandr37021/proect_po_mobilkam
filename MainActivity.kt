package com.example.smarthome


import androidx.compose.ui.text.style.TextAlign
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
// МОДЕЛИ ДАННЫХ
data class User(
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String = "",
    val settings: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class RoomModel(  // Изменили имя
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val icon: String = "🏠",
    val deviceCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class Device(
    val id: Long = 0,
    val userId: Long,
    val roomId: Long,
    val name: String,
    val type: String,
    val status: String = "off",
    val config: String = "{}",
    val isOnline: Boolean = false,
    val powerConsumption: Double = 0.0,
    val protocol: String = "local",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Scenario(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String,
    val isActive: Boolean = true,
    val triggers: String = "[]",
    val actions: String = "[]",
    val conditions: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Event(
    val id: Long = 0,
    val userId: Long,
    val deviceId: Long?,
    val eventType: String,
    val data: String = "{}",
    val timestamp: Long = System.currentTimeMillis()
)

data class Notification(
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String = "info",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CloudSyncStatus(
    val isSyncing: Boolean = false,
    val lastSync: Long = 0,
    val syncError: String? = null,
    val progress: Float = 0f
)
// === ROOM DATABASE ENTITIES ===

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "settings")
    val settings: String = "{}",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String = "🏠",

    @ColumnInfo(name = "device_count")
    val deviceCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "room_id")
    val roomId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "status")
    val status: String = "off",

    @ColumnInfo(name = "config")
    val config: String = "{}",

    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,

    @ColumnInfo(name = "power_consumption")
    val powerConsumption: Double = 0.0,

    @ColumnInfo(name = "protocol")
    val protocol: String = "local",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "triggers")
    val triggers: String = "[]",

    @ColumnInfo(name = "actions")
    val actions: String = "[]",

    @ColumnInfo(name = "conditions")
    val conditions: String = "[]",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "device_id")
    val deviceId: Long?,

    @ColumnInfo(name = "event_type")
    val eventType: String,

    @ColumnInfo(name = "data")
    val data: String = "{}",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "type")
    val type: String = "info",

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

// === ROOM DAOs ===

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE user_id = :userId")
    fun getRoomsByUser(userId: Long): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomById(roomId: Long): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: RoomEntity)

    @Update
    suspend fun update(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    suspend fun delete(roomId: Long)

    @Query("DELETE FROM rooms")
    suspend fun deleteAll()

    @Query("UPDATE rooms SET device_count = :count WHERE id = :roomId")
    suspend fun updateDeviceCount(roomId: Long, count: Int)
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices WHERE user_id = :userId")
    fun getDevicesByUser(userId: Long): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE room_id = :roomId AND user_id = :userId")
    fun getDevicesByRoom(userId: Long, roomId: Long): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceById(deviceId: Long): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun delete(deviceId: Long) : Int

    @Query("DELETE FROM devices")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM devices WHERE room_id = :roomId")
    suspend fun countDevicesInRoom(roomId: Long): Int
}

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenarios WHERE user_id = :userId")
    fun getScenariosByUser(userId: Long): Flow<List<ScenarioEntity>>

    @Query("SELECT * FROM scenarios WHERE id = :scenarioId")
    suspend fun getScenarioById(scenarioId: Long): ScenarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scenario: ScenarioEntity)

    @Update
    suspend fun update(scenario: ScenarioEntity)

    @Query("DELETE FROM scenarios WHERE id = :scenarioId")
    suspend fun delete(scenarioId: Long)

    @Query("DELETE FROM scenarios")
    suspend fun deleteAll()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getEventsByUser(userId: Long): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY timestamp DESC")
    fun getNotificationsByUser(userId: Long): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    fun getUnreadCount(userId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Update
    suspend fun update(notification: NotificationEntity)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}

// === ROOM DATABASE ===

@Database(
    entities = [
        UserEntity::class,
        RoomEntity::class,
        DeviceEntity::class,
        ScenarioEntity::class,
        EventEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartHomeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun deviceDao(): DeviceDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun eventDao(): EventDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: SmartHomeDatabase? = null

        fun getDatabase(context: Context): SmartHomeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartHomeDatabase::class.java,
                    "smarthome_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
// VIEWMODEL - ОБНОВЛЕННАЯ ВЕРСИЯ
class SmartHomeViewModel : ViewModel() {
    // Database
    private lateinit var database: SmartHomeDatabase
    private lateinit var userDao: UserDao
    private lateinit var roomDao: RoomDao
    private lateinit var deviceDao: DeviceDao
    private lateinit var scenarioDao: ScenarioDao
    private lateinit var eventDao: EventDao
    private lateinit var notificationDao: NotificationDao

    // State
    private val _darkThemeEnabled = MutableStateFlow(false)
    val darkThemeEnabled: StateFlow<Boolean> = _darkThemeEnabled

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _syncStatus = MutableStateFlow("Синхронизировано")
    val syncStatus: StateFlow<String> = _syncStatus

    private val _cloudSyncStatus = MutableStateFlow(CloudSyncStatus())
    val cloudSyncStatus: StateFlow<CloudSyncStatus> = _cloudSyncStatus

    private val _rooms = MutableStateFlow<List<RoomModel>>(emptyList())
    val rooms: StateFlow<List<RoomModel>> = _rooms

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices

    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())
    val scenarios: StateFlow<List<Scenario>> = _scenarios

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount

    // Инициализация базы данных
    fun initializeDatabase(context: Context) {
        database = SmartHomeDatabase.getDatabase(context)
        userDao = database.userDao()
        roomDao = database.roomDao()
        deviceDao = database.deviceDao()
        scenarioDao = database.scenarioDao()
        eventDao = database.eventDao()
        notificationDao = database.notificationDao()

        // Инициализация тестовых данных при первом запуске
        viewModelScope.launch(Dispatchers.IO) {
            val userCount = userDao.getUserByEmail("test@example.com")
            if (userCount == null) {
                initializeTestData()
            }
        }

        // Запускаем обновление Flow данных
        // Запускаем обновление Flow данных
        viewModelScope.launch(Dispatchers.IO) {
            // Подписывается на обновления комнат
            roomDao.getRoomsByUser(userId = 1).collect { roomEntities ->
                _rooms.value = roomEntities.map { it.toRoomModel() }  // Исправлено toRoom() на toRoomModel()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            deviceDao.getDevicesByUser(userId = 1).collect { deviceEntities ->
                _devices.value = deviceEntities.map { it.toDevice() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            scenarioDao.getScenariosByUser(1).collect { scenarioEntities ->
                _scenarios.value = scenarioEntities.map { it.toScenario() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            eventDao.getEventsByUser(1).collect { eventEntities ->
                _events.value = eventEntities.map { it.toEvent() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.getNotificationsByUser(1).collect { notificationEntities ->
                _notifications.value = notificationEntities.map { it.toNotification() }
            }
        }

        viewModelScope.launch(context = Dispatchers.IO) {
            notificationDao.getUnreadCount(userId = 1).collect { count ->
                _unreadNotificationsCount.value = count
            }
        }
    }

    private suspend fun initializeTestData() {
        // Создаем тестового пользователя
        val testUser = UserEntity(
            id = 1,
            email = "test@example.com",
            password = "password",
            name = "Тестовый Пользователь"
        )
        userDao.insert(testUser)

        // Создаем комнаты
        val rooms = listOf(
            RoomEntity(id = 1, userId = 1, name = "Все комнаты", deviceCount = 5),
            RoomEntity(id = 2, userId = 1, name = "Гостиная", icon = "🛋️", deviceCount = 2),
            RoomEntity(id = 3, userId = 1, name = "Спальня", icon = "🛏️", deviceCount = 1),
            RoomEntity(id = 4, userId = 1, name = "Кухня", icon = "🍳", deviceCount = 1),
            RoomEntity(id = 5, userId = 1, name = "Ванная", icon = "🚿", deviceCount = 1),
            RoomEntity(id = 6, userId = 1, name = "Прихожая", icon = "🚪", deviceCount = 0)
        )
        rooms.forEach { roomDao.insert(it) }

        // Создаем устройства
        val devices = listOf(
            DeviceEntity(
                id = 1, userId = 1, roomId = 2,
                name = "Умная лампа", type = "Свет",
                status = "on", isOnline = true, powerConsumption = 0.05,
                protocol = "mqtt", config = """{"topic":"home/livingroom/light1","broker":"tcp://localhost:1883"}"""
            ),
            DeviceEntity(
                id = 2, userId = 1, roomId = 3,
                name = "Кондиционер", type = "Климат",
                status = "cool", isOnline = true, powerConsumption = 1.5,
                protocol = "http", config = """{"baseUrl":"http://ac.example.com","apiKey":"secret123"}"""
            ),
            DeviceEntity(
                id = 3, userId = 1, roomId = 2,
                name = "Телевизор", type = "Развлечения",
                status = "off", isOnline = false, powerConsumption = 0.0
            ),
            DeviceEntity(
                id = 4, userId = 1, roomId = 4,
                name = "Умная розетка", type = "Энергия",
                status = "on", isOnline = true, powerConsumption = 0.8,
                protocol = "mqtt", config = """{"topic":"home/kitchen/socket1"}"""
            ),
            DeviceEntity(
                id = 5, userId = 1, roomId = 3,
                name = "Обогреватель", type = "Климат",
                status = "off", isOnline = true, powerConsumption = 0.0
            )
        )
        devices.forEach { deviceDao.insert(it) }

        // Создаем сценарии
        val scenarios = listOf(
            ScenarioEntity(
                id = 1, userId = 1,
                name = "Утренний режим",
                description = "Плавное включение света",
                triggers = """[{"type":"time","condition":"equals","value":"07:00"}]""",
                actions = """[{"deviceId":"1","actionType":"turn_on","value":"100"}]""",
                conditions = """[{"type":"weekday","operator":"in","value":"1,2,3,4,5"}]"""
            ),
            ScenarioEntity(
                id = 2, userId = 1,
                name = "Вечерний режим",
                description = "Приглушенный свет",
                triggers = """[{"type":"time","condition":"equals","value":"21:00"}]""",
                actions = """[{"deviceId":"1","actionType":"turn_on","value":"50"}]"""
            )
        )
        scenarios.forEach { scenarioDao.insert(it) }

        // Создаем уведомления
        val notifications = listOf(
            NotificationEntity(
                id = 1, userId = 1,
                title = "Новое устройство",
                message = "Умная лампа подключена",
                type = "info"
            ),
            NotificationEntity(
                id = 2, userId = 1,
                title = "Высокое потребление",
                message = "Кондиционер потребляет 1.5 кВт/ч",
                type = "warning"
            )
        )
        notifications.forEach { notificationDao.insert(it) }

        // Создаем события
        val events = listOf(
            EventEntity(id = 1, userId = 1, deviceId = 1, eventType = "device_turned_on", data = """{"value":"100"}"""),
            EventEntity(id = 2, userId = 1, deviceId = 2, eventType = "device_status_changed", data = """{"status":"cool"}"""),
            EventEntity(id = 3, userId = 1, deviceId = null, eventType = "scenario_activated", data = """{"scenario":"Утренний режим"}""")
        )
        events.forEach { eventDao.insert(it) }
    }


    suspend fun login(email: String, password: String): Boolean {
        return viewModelScope.async(context = Dispatchers.IO) {
            val userEntity = userDao.getUser(email, password)
            userEntity?.let {
                _currentUser.value = it.toUser()  // ИСПРАВЛЕНО: update -> value =
                true
            } ?: false
        }.await()
    }

    suspend fun register(email: String, password: String, name: String = ""): Boolean {
        return viewModelScope.async(Dispatchers.IO) {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) return@async false

            val newUserId = 1L
            val newUser = UserEntity(
                id = newUserId,
                email = email,
                password = password,
                name = name
            )
            userDao.insert(newUser)
            _currentUser.value =  newUser.toUser()
            true
        }.await()
    }

    fun logout() {
        _currentUser.value = null  // ИСПРАВЛЕНО: update -> value =
    }

    // Тема
    fun toggleDarkTheme(enabled: Boolean) {
        _darkThemeEnabled.value = enabled  // ИСПРАВЛЕНО: update -> value =
    }

    // DEVICE OPERATIONS
    suspend fun addDevice(
        name: String,
        type: String,
        roomId: Long,
        protocol: String = "local",
        config: String = "{}"
    ): Device {
        return viewModelScope.async(Dispatchers.IO) {
            val newId = System.currentTimeMillis()
            val newDevice = DeviceEntity(
                id = newId,
                userId = 1,
                roomId = roomId,
                name = name,
                type = type,
                protocol = protocol,
                config = config,
                isOnline = true,
                status = "on"
            )

            deviceDao.insert(newDevice)

            updateRoomDeviceCount(roomId)

            addEvent(
                deviceId = newId,
                eventType = "device_added",
                data = """{"name":"$name"}"""
            )

            addNotification(
                title = "Новое устройство",
                message = "Устройство '$name' успешно добавлено",
                type = "info"
            )

            newDevice.toDevice()
        }.await()
    }

    suspend fun updateDevice(
        deviceId: Long,
        name: String? = null,
        type: String? = null,
        roomId: Long? = null,
        status: String? = null,
        isOnline: Boolean? = null,
        powerConsumption: Double? = null,
        config: String? = null
    ): Boolean {
        return viewModelScope.async(Dispatchers.IO) {
            val device = deviceDao.getDeviceById(deviceId) ?: return@async false
            val oldRoomId = device.roomId

            val updatedDevice = device.copy(
                name = name ?: device.name,
                type = type ?: device.type,
                roomId = roomId ?: device.roomId,
                status = status ?: device.status,
                isOnline = isOnline ?: device.isOnline,
                powerConsumption = powerConsumption ?: device.powerConsumption,
                config = config ?: device.config,
                updatedAt = System.currentTimeMillis()
            )

            deviceDao.update(updatedDevice)

            if (oldRoomId != updatedDevice.roomId) {
                updateRoomDeviceCount(oldRoomId)
                updateRoomDeviceCount(updatedDevice.roomId)
            }

            addEvent(
                deviceId = deviceId,
                eventType = "device_updated",
                data = """{"status":"${status ?: device.status}"}"""
            )

            true
        }.await()
    }

    suspend fun deleteDevice(deviceId: Long): Boolean {
        return viewModelScope.async(Dispatchers.IO) {
            val device = deviceDao.getDeviceById(deviceId) ?: return@async false
            val success = deviceDao.delete(deviceId) > 0

            if (success) {
                updateRoomDeviceCount(roomId = device.roomId)

                addEvent(
                    deviceId = deviceId,
                    eventType = "device_removed",
                    data = """{"name":"${device.name}"}"""  // Исправлено: ] -> }
                )
            }
            success  // Добавлено
        }.await()
    }

    suspend fun getDevicesByRoom(roomId: Long): List<Device> {
        return viewModelScope.async(Dispatchers.IO) {
            deviceDao.getDevicesByRoom(1, roomId)
                .firstOrNull()
                ?.map { it.toDevice() }
                ?: emptyList()
        }.await()
    }

    private suspend fun updateRoomDeviceCount(roomId: Long) {
        val count = deviceDao.countDevicesInRoom(roomId)
        roomDao.updateDeviceCount(roomId, count)
    }

    // SCENARIO OPERATIONS
    suspend fun addScenario(
        name: String,
        description: String,
        triggers: String = "[]",
        actions: String = "[]",
        conditions: String = "[]"
    ): Scenario {
        return viewModelScope.async(Dispatchers.IO) {
            val newId = System.currentTimeMillis()
            val newScenario = ScenarioEntity(
                id = newId,
                userId = 1,
                name = name,
                description = description,
                triggers = triggers,
                actions = actions,
                conditions = conditions
            )

            scenarioDao.insert(newScenario)

            addEvent(
                deviceId = null,
                eventType = "scenario_created",
                data = """{"name":"$name"}"""
            )

            addNotification(
                title = "Новый сценарий",
                message = "Сценарий '$name' создан",
                type = "info"
            )

            newScenario.toScenario()
        }.await()
    }

    suspend fun updateScenario(
        scenarioId: Long,
        name: String? = null,
        description: String? = null,
        isActive: Boolean? = null,
        triggers: String? = null,
        actions: String? = null,
        conditions: String? = null
    ): Boolean {
        return viewModelScope.async(Dispatchers.IO) {
            val scenario = scenarioDao.getScenarioById(scenarioId) ?: return@async false

            val updatedScenario = scenario.copy(
                name = name ?: scenario.name,
                description = description ?: scenario.description,
                isActive = isActive ?: scenario.isActive,
                triggers = triggers ?: scenario.triggers,
                actions = actions ?: scenario.actions,
                conditions = conditions ?: scenario.conditions,
                updatedAt = System.currentTimeMillis()
            )

            scenarioDao.update(updatedScenario)

            addEvent(
                deviceId = null,
                eventType = if (isActive == true) "scenario_activated" else "scenario_deactivated",
                data = """{"name":"${name ?: scenario.name}"}"""
            )

            true
        }.await()
    }

    suspend fun deleteScenario(scenarioId: Long): Boolean {
        return viewModelScope.async(Dispatchers.IO) {  // Исправлено: viewModeIscope -> viewModelScope
            val scenario = scenarioDao.getScenarioById(scenarioId) ?: return@async false

            try {
                scenarioDao.delete(scenarioId)  // Убрана проверка > 0
                addEvent(
                    deviceId = null,
                    eventType = "scenario_deleted",
                    data = """{"name":"${scenario.name}"}"""  // Исправлены кавычки
                )
                true  // Добавлено возвращаемое значение
            } catch (e: Exception) {
                false
            }
        }.await()
    }

    // EVENT OPERATIONS
    private suspend fun addEvent(deviceId: Long?, eventType: String, data: String = "{}") {
        val newEvent = EventEntity(
            userId = 1,
            deviceId = deviceId,
            eventType = eventType,
            data = data,
            timestamp = System.currentTimeMillis()
        )
        eventDao.insert(newEvent)
    }

    // NOTIFICATION OPERATIONS
    // NOTIFICATION OPERATIONS
    private suspend fun addNotification(title: String, message: String, type: String = "info") {  // Добавлено {
        val newNotification = NotificationEntity(
            userId = 1,
            title = title,
            message = message,
            type = type,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insert(newNotification)
    }  // Это закрывающая скобка для метода

    suspend fun markNotificationAsRead(notificationId: Long): Boolean {
        return viewModelScope.async(Dispatchers.IO) {  // Исправлено: viewNodeIScope -> viewModelScope
            try {
                notificationDao.markAsRead(notificationId)  // Убрано > 0
                true
            } catch (e: Exception) {
                false
            }
        }.await()
    }

    suspend fun clearNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.deleteAll()
        }
    }

    // UTILITY FUNCTIONS
    suspend fun calculateTotalPowerConsumption(): Double {
        return viewModelScope.async(Dispatchers.IO) {
            devices.value.sumOf { it.powerConsumption }
        }.await()
    }

    suspend fun getActiveDevicesCount(): Int {
        return viewModelScope.async(Dispatchers.IO) {
            devices.value.count { it.isOnline }
        }.await()
    }

    suspend fun getActiveScenariosCount(): Int {
        return viewModelScope.async(Dispatchers.IO) {
            scenarios.value.count { it.isActive }
        }.await()
    }

    suspend fun getUnreadNotificationsCount(): Int {
        return viewModelScope.async(Dispatchers.IO) {
            notificationDao.getUnreadCount(1).firstOrNull() ?: 0
        }.await()
    }

    suspend fun getFilteredEvents(filter: String): List<Event> {
        return when (filter) {
            "Устройства" -> events.value.filter { it.deviceId != null }
            "Сценарии" -> events.value.filter { it.eventType.contains("scenario") }
            "Система" -> events.value.filter { it.deviceId == null && !it.eventType.contains("scenario") }
            else -> events.value
        }
    }

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value =   online
        viewModelScope.launch(Dispatchers.IO) {
            addNotification(
                title = "Сеть",
                message = if (online) "Подключение к интернету восстановлено" else "Отсутствует подключение к интернету",
                type = if (online) "info" else "warning"
            )
        }
    }

    suspend fun analyzeEnergyConsumption(): Map<String, Any> {
        val totalPower = calculateTotalPowerConsumption()
        val activeDevices = getActiveDevicesCount()
        val recommendations = mutableListOf<String>()

        if (totalPower > 2.0) {
            recommendations.add("Высокое энергопотребление. Отключите неиспользуемые устройства")
        }

        val allDevices = devices.value
        if (allDevices.any { it.type == "Кондиционер" && it.powerConsumption > 1.0 }) {
            recommendations.add("Кондиционер потребляет много энергии. Установите температуру на 24°C")
        }

        val highPowerDevices = allDevices.filter { it.powerConsumption > 0.5 }
        if (highPowerDevices.size > 2) {
            recommendations.add("Много устройств с высоким потреблением. Используйте в разное время")
        }

        val efficiencyScore = when {
            totalPower == 0.0 -> 100
            totalPower < 0.5 -> 90
            totalPower < 1.0 -> 75
            totalPower < 2.0 -> 60
            else -> 40
        }

        return mapOf(
            "totalPower" to totalPower,
            "activeDevices" to activeDevices,
            "recommendations" to recommendations,
            "efficiencyScore" to efficiencyScore
        )
    }

    fun sendNotification(title: String, message: String, type: String = "info") {
        viewModelScope.launch(Dispatchers.IO) {
            addNotification(title, message, type) // вызывает приватную
        }
    }
}

// Extension функции для конвертации Entity <-> Model
private fun UserEntity.toUser(): User = User(
    id = id,
    email = email,
    password = password,
    name = name,
    settings = settings,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun RoomEntity.toRoomModel(): RoomModel = RoomModel(  // Изменили имя метода
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    deviceCount = deviceCount,
    createdAt = createdAt
)

private fun DeviceEntity.toDevice(): Device = Device(
    id = id,
    userId = userId,
    roomId = roomId,
    name = name,
    type = type,
    status = status,
    config = config,
    isOnline = isOnline,
    powerConsumption = powerConsumption,
    protocol = protocol,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun ScenarioEntity.toScenario(): Scenario = Scenario(
    id = id,
    userId = userId,
    name = name,
    description = description,
    isActive = isActive,
    triggers = triggers,
    actions = actions,
    conditions = conditions,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun EventEntity.toEvent(): Event = Event(
    id = id,
    userId = userId,
    deviceId = deviceId,
    eventType = eventType,
    data = data,
    timestamp = timestamp
)

private fun NotificationEntity.toNotification(): Notification = Notification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    timestamp = timestamp
)

// Обратные конвертации (если понадобятся)
private fun User.toUserEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    password = password,
    name = name,
    settings = settings,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun Device.toDeviceEntity(): DeviceEntity = DeviceEntity(
    id = id,
    userId = userId,
    roomId = roomId,
    name = name,
    type = type,
    status = status,
    config = config,
    isOnline = isOnline,
    powerConsumption = powerConsumption,
    protocol = protocol,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Добавьте этот код перед class MainActivity
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Devices : Screen("devices")
    object DeviceDetail : Screen("device_detail/{deviceId}") {
        fun createRoute(deviceId: Long) = "device_detail/$deviceId"
    }
    object Scenarios : Screen("scenarios")
    object ScenarioDetail : Screen("scenario_detail/{scenarioId}") {
        fun createRoute(scenarioId: Long) = "scenario_detail/$scenarioId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
    object AddDevice : Screen("add_device")
    object AddScenario : Screen("add_scenario")
    object EditDevice : Screen("edit_device/{deviceId}") {
        fun createRoute(deviceId: Long) = "edit_device/$deviceId"
    }
    object EditScenario : Screen("edit_scenario/{scenarioId}") {
        fun createRoute(scenarioId: Long) = "edit_scenario/$scenarioId"
    }
    object Sync : Screen("sync")
    object Notifications : Screen("notifications")
    object EnergyAnalytics : Screen("energy_analytics")
    object DeviceIntegration : Screen("device_integration")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SmartHomeViewModel = viewModel()

            // Инициализация базы данных при первом запуске
            LaunchedEffect(Unit) {
                viewModel.initializeDatabase(applicationContext)
            }

            SmartHomeTheme(
                darkTheme = viewModel.darkThemeEnabled.collectAsState().value
            ) {
                SmartHomeApp()
            }
        }
    }
}

@Composable
fun SmartHomeApp() {
    val navController = rememberNavController()
    val viewModel: SmartHomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController, viewModel) }
        composable(Screen.Register.route) { RegisterScreen(navController, viewModel) }
        composable(Screen.Main.route) { MainScreen(navController, viewModel) }
        composable(Screen.Devices.route) { DevicesScreen(navController, viewModel) }
        composable(
            Screen.DeviceDetail.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: 0L
            DeviceDetailScreen(navController, viewModel, deviceId)
        }
        composable(Screen.Scenarios.route) { ScenariosScreen(navController, viewModel) }
        composable(
            Screen.ScenarioDetail.route,
            arguments = listOf(navArgument("scenarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val scenarioId = backStackEntry.arguments?.getLong("scenarioId") ?: 0L
            ScenarioDetailScreen(navController, viewModel, scenarioId)
        }
        composable(Screen.History.route) { HistoryScreen(navController, viewModel) }
        composable(Screen.Settings.route) { SettingsScreen(navController, viewModel) }
        composable(Screen.AddDevice.route) { AddDeviceScreen(navController, viewModel) }
        composable(Screen.AddScenario.route) { AddScenarioScreen(navController, viewModel) }
        composable(
            Screen.EditDevice.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: 0L
            EditDeviceScreen(navController, viewModel, deviceId)
        }
        composable(
            Screen.EditScenario.route,
            arguments = listOf(navArgument("scenarioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val scenarioId = backStackEntry.arguments?.getLong("scenarioId") ?: 0L
            EditScenarioScreen(navController, viewModel, scenarioId)
        }
        composable(Screen.Sync.route) { SyncScreen(navController, viewModel) }
        composable(Screen.Notifications.route) { NotificationsScreen(navController, viewModel) }
        composable(Screen.EnergyAnalytics.route) { EnergyAnalyticsScreen(navController, viewModel) }
        composable(Screen.DeviceIntegration.route) { DeviceIntegrationScreen(navController, viewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var email by remember { mutableStateOf("test@example.com") }
    var password by remember { mutableStateOf("password") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Авторизация") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Умный Дом Пульт",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        if (viewModel.login(email, password)) {
                            navController.navigate(Screen.Main.route)
                        } else {
                            errorMessage = "Неверный email или пароль"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(text = "Войти", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нет аккаунта?",
                modifier = Modifier.clickable { navController.navigate(Screen.Register.route) },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердите пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Пароли не совпадают"
                        return@Button
                    }

                    viewModel.viewModelScope.launch {
                        if (viewModel.register(email, password, name)) {
                            navController.navigate(Screen.Main.route)
                        } else {
                            errorMessage = "Пользователь с таким email уже существует"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Зарегистрироваться", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var selectedRoom by remember { mutableStateOf(1L) }

    // Используем collectAsState для получения значений из StateFlow
    val rooms by viewModel.rooms.collectAsState(initial = emptyList())
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val scenarios by viewModel.scenarios.collectAsState(initial = emptyList())
    val events by viewModel.events.collectAsState(initial = emptyList())
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState(initial = 0)
    val currentUser by viewModel.currentUser.collectAsState()

    // Вычисляемые значения
    val totalPower by remember { derivedStateOf { devices.sumOf { it.powerConsumption } } }
    val activeDevicesCount by remember { derivedStateOf { devices.count { it.isOnline } } }
    val activeScenariosCount by remember { derivedStateOf { scenarios.count { it.isActive } } }

    // Анализ энергопотребления
    val energyAnalysis by remember { derivedStateOf {
        val totalPowerVal = totalPower
        val recommendations = mutableListOf<String>()

        if (totalPowerVal > 2.0) {
            recommendations.add("Высокое энергопотребление. Отключите неиспользуемые устройства")
        }

        if (devices.any { it.type == "Кондиционер" && it.powerConsumption > 1.0 }) {
            recommendations.add("Кондиционер потребляет много энергии. Установите температуру на 24°C")
        }

        val highPowerDevices = devices.filter { it.powerConsumption > 0.5 }
        if (highPowerDevices.size > 2) {
            recommendations.add("Много устройств с высоким потреблением. Используйте в разное время")
        }

        val efficiencyScore = when {
            totalPowerVal == 0.0 -> 100
            totalPowerVal < 0.5 -> 90
            totalPowerVal < 1.0 -> 75
            totalPowerVal < 2.0 -> 60
            else -> 40
        }

        mapOf(
            "totalPower" to totalPowerVal,
            "activeDevices" to activeDevicesCount,
            "recommendations" to recommendations,
            "efficiencyScore" to efficiencyScore
        )
    } }

    val efficiencyScore = (energyAnalysis["efficiencyScore"] as? Int) ?: 0
    val recommendations = (energyAnalysis["recommendations"] as? List<String>) ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Умный Дом Пульт") },
                actions = {
                    Box(
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Notifications.route) }
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text("Быстрые действия", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Добавить устройство",
                        icon = Icons.Default.Add,
                        onClick = { navController.navigate(Screen.AddDevice.route) }
                    )
                    ActionCard(
                        title = "Аналитика энергии",
                        icon = Icons.Default.Eco,
                        onClick = { navController.navigate(Screen.EnergyAnalytics.route) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Интеграция",
                        icon = Icons.Default.Cloud,
                        onClick = { navController.navigate(Screen.DeviceIntegration.route) }
                    )
                    ActionCard(
                        title = "Создать сценарий",
                        icon = Icons.Default.Star,
                        onClick = { navController.navigate(Screen.AddScenario.route) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Комнаты", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(rooms) { room ->
                val deviceCount = devices.count { it.roomId == room.id }
                RoomItem(
                    room = room,
                    isSelected = selectedRoom == room.id,
                    deviceCount = deviceCount,
                    onSelect = { selectedRoom = room.id }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            efficiencyScore >= 80 -> Color(0xFFE8F5E8)
                            efficiencyScore >= 60 -> Color(0xFFFFF2E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Eco, contentDescription = "Эко-режим",
                                tint = when {
                                    efficiencyScore >= 80 -> Color.Green
                                    efficiencyScore >= 60 -> Color(0xFFFF9800)
                                    else -> Color.Red
                                })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Эко-режим • $efficiencyScore%", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Текущее потребление: ${"%.2f".format(totalPower)} кВт/ч")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Рекомендации:", fontWeight = FontWeight.Bold)
                        if (recommendations.isNotEmpty()) {
                            recommendations.take(2).forEach { recommendation ->
                                Text("• $recommendation", fontSize = 12.sp)
                            }
                        } else {
                            Text("• Оптимальное энергопотребление", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Энергопотребление",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Всего: ${"%.2f".format(totalPower)} кВт/ч",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Активных устройств: $activeDevicesCount",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Общая статистика",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$activeDevicesCount",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("онлайн", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$activeScenariosCount",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("активных", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${rooms.size - 1}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("комнат", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2E0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "История",
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Последние события",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val recentEvents = events.take(3)
                        if (recentEvents.isNotEmpty()) {
                            recentEvents.forEach { event ->
                                Text(
                                    text = "• ${event.eventType.replace("_", " ")}",
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text("Событий пока нет", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var selectedRoom by remember { mutableStateOf(1L) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deviceToDelete by remember { mutableStateOf<Device?>(null) }

    // Используем StateFlow данные
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val rooms by viewModel.rooms.collectAsState(initial = emptyList())

    val filteredDevices by remember(selectedRoom, searchQuery, devices) {
        derivedStateOf {
            val filtered = if (selectedRoom == 1L) {
                devices
            } else {
                devices.filter { it.roomId == selectedRoom }
            }

            filtered.filter { device ->
                searchQuery.isEmpty() ||
                        device.name.contains(searchQuery, ignoreCase = true) ||
                        device.type.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Устройства") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddDevice.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                modifier = Modifier.padding(16.dp),
                onSearch = { searchQuery = it }
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rooms.forEach { room ->
                            val deviceCount = devices.count { it.roomId == room.id }
                            FilterChip(
                                text = "${room.name} ($deviceCount)",
                                isSelected = selectedRoom == room.id,
                                onClick = { selectedRoom = room.id }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(filteredDevices) { device ->
                    DeviceCard(
                        device = device,
                        onEdit = { navController.navigate(Screen.EditDevice.createRoute(device.id)) },
                        onDelete = {
                            deviceToDelete = device
                            showDeleteDialog = true
                        },
                        onToggle = { isEnabled ->
                            viewModel.viewModelScope.launch {
                                viewModel.updateDevice(
                                    deviceId = device.id,
                                    isOnline = isEnabled,
                                    status = if (isEnabled) "on" else "off"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (filteredDevices.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Устройства не найдены", color = Color.Gray)
                            if (searchQuery.isNotEmpty()) {
                                Text("Попробуйте изменить запрос поиска", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить устройство?") },
            text = { Text("Вы уверены, что хотите удалить устройство \"${deviceToDelete?.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        deviceToDelete?.let { device ->
                            viewModel.viewModelScope.launch {
                                viewModel.deleteDevice(device.id)
                            }
                        }
                        showDeleteDialog = false
                        deviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenariosScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var scenarioToDelete by remember { mutableStateOf<Scenario?>(null) }

    val scenarios by viewModel.scenarios.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Сценарии") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddScenario.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(scenarios) { scenario ->
                ScenarioCard(
                    scenario = scenario,
                    onEdit = { navController.navigate(Screen.EditScenario.createRoute(scenario.id)) },
                    onDelete = {
                        scenarioToDelete = scenario
                        showDeleteDialog = true
                    },
                    onToggle = { isActive ->
                        viewModel.viewModelScope.launch {
                            viewModel.updateScenario(scenario.id, isActive = isActive)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить сценарий?") },
            text = { Text("Вы уверены, что хотите удалить сценарий \"${scenarioToDelete?.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        scenarioToDelete?.let { scenario ->
                            viewModel.viewModelScope.launch {
                                viewModel.deleteScenario(scenario.id)
                            }
                        }
                        showDeleteDialog = false
                        scenarioToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var selectedFilter by remember { mutableStateOf("Все события") }
    val events by viewModel.events.collectAsState(initial = emptyList())

    val filteredEvents by remember(selectedFilter, events) {
        derivedStateOf {
            when (selectedFilter) {
                "Устройства" -> events.filter { it.deviceId != null }
                "Сценарии" -> events.filter { it.eventType.contains("scenario") }
                "Система" -> events.filter { it.deviceId == null && !it.eventType.contains("scenario") }
                else -> events
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("История") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Все события", "Устройства", "Сценарии", "Система").forEach { filter ->
                    FilterChip(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredEvents) { event ->
                    EventCard(event = event)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var ecoMode by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Добавьте это состояние для темы
    val darkThemeEnabled by viewModel.darkThemeEnabled.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val devices by viewModel.devices.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройки") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Настройки профиля",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Email: ${currentUser?.email ?: "Неизвестно"}",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Устройств: ${devices.size}",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Статус: ${if (isOnline) "Онлайн" else "Оффлайн"}",
                            fontSize = 16.sp,
                            color = if (isOnline) Color.Green else Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Внешний вид",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (darkThemeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Тема"
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (darkThemeEnabled) "Тёмная тема" else "Светлая тема",
                                    fontSize = 16.sp
                                )
                            }
                            Switch(
                                checked = darkThemeEnabled,
                                onCheckedChange = {
                                    viewModel.toggleDarkTheme(it)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (darkThemeEnabled)
                                "Используется тёмная тема"
                            else
                                "Используется светлая тема",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Синхронизация",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Облачная синхронизация", fontSize = 16.sp)
                            Switch(
                                checked = isOnline,
                                onCheckedChange = { viewModel.setOnlineStatus(it) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = syncStatus,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Sync.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Управление синхронизацией")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Настройки системы",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Eco, contentDescription = "Эко-режим")
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Эко-режим", fontSize = 16.sp)
                            }
                            Switch(checked = ecoMode, onCheckedChange = { ecoMode = it })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Энергосбережение и оптимизация",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Уведомления", fontSize = 16.sp)
                            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Опасная зона",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.clearNotifications()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text("Очистить кэш")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.clearNotifications()  // Исправлено cleanNotifications -> clearNotifications
                                    viewModel.sendNotification("Сброс", "Настройки сброшены", "warning")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Сбросить настройки")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Выйти")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Выйти из аккаунта")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var showSyncDialog by remember { mutableStateOf(false) }
    val syncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val scenarios by viewModel.scenarios.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Синхронизация") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                        contentDescription = "Статус",
                        tint = if (isOnline) Color.Green else Color.Red
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOnline) "Подключено к облаку" else "Оффлайн режим",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (syncStatus.isSyncing) {  // Исправлено: syncStatus.isEmpty -> syncStatus.isSyncing
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Синхронизация...", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = syncStatus.progress,  // Убраны фигурные скобки {}
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(syncStatus.progress * 100).toInt()}%",  // Исправлена строка
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Статистика", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    val lastSync = if (syncStatus.lastSync > 0) {
                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(Date(syncStatus.lastSync))
                    } else "Никогда"

                    StatItem("Последняя синхронизация", lastSync)
                    StatItem("Устройств для синхронизации", devices.size.toString())
                    StatItem("Сценариев для синхронизации", scenarios.size.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Управление синхронизацией", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showSyncDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isOnline && !syncStatus.isSyncing
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Синхронизировать")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Синхронизировать с облаком")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.sendNotification(  // Используйте sendNotification вместо addNotification
                                title = "Облако",
                                message = "Данные загружены из облака",
                                type = "info"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isOnline,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Загрузить из облака")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.sendNotification(  // Используйте sendNotification вместо addNotification
                                title = "Резервная копия",
                                message = "Резервная копия создана",
                                type = "info"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isOnline,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("Создать резервную копию")
                    }
                }
            }
        }
    }

    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text("Синхронизация") },
            text = { Text("Вы уверены, что хотите синхронизировать данные с облаком?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSyncDialog = false
                    }
                ) {
                    Text("Синхронизировать")
                }
            },
            dismissButton = {
                Button(onClick = { showSyncDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    val notifications by viewModel.notifications.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Уведомления") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Нет уведомлений",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Уведомлений нет", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = {
                                viewModel.viewModelScope.launch {
                                    viewModel.markNotificationAsRead(notification.id)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification, onMarkAsRead: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(notification.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!notification.isRead) {
                    onMarkAsRead()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when (notification.type) {
                "warning" -> Color(0xFFFFF2E0)
                "error" -> Color(0xFFFFEBEE)
                else -> if (notification.isRead) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyAnalyticsScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    val devices by viewModel.devices.collectAsState(initial = emptyList())

    val energyAnalysis by remember(devices) {
        derivedStateOf {
            val totalPower = devices.sumOf { it.powerConsumption }
            val activeDevices = devices.count { it.isOnline }
            val recommendations = mutableListOf<String>()

            if (totalPower > 2.0) {
                recommendations.add("Высокое энергопотребление. Отключите неиспользуемые устройства")
            }

            if (devices.any { it.type == "Кондиционер" && it.powerConsumption > 1.0 }) {
                recommendations.add("Кондиционер потребляет много энергии. Установите температуру на 24°C")
            }

            val highPowerDevices = devices.filter { it.powerConsumption > 0.5 }
            if (highPowerDevices.size > 2) {
                recommendations.add("Много устройств с высоким потреблением. Используйте в разное время")
            }

            val efficiencyScore = when {
                totalPower == 0.0 -> 100
                totalPower < 0.5 -> 90
                totalPower < 1.0 -> 75
                totalPower < 2.0 -> 60
                else -> 40
            }

            mapOf(
                "totalPower" to totalPower,
                "activeDevices" to activeDevices,
                "recommendations" to recommendations,
                "efficiencyScore" to efficiencyScore
            )
        }
    }

    val efficiencyScore = (energyAnalysis["efficiencyScore"] as? Int) ?: 0
    val recommendations = (energyAnalysis["recommendations"] as? List<String>) ?: emptyList()
    val totalPower = (energyAnalysis["totalPower"] as? Double) ?: 0.0
    val activeDevices = (energyAnalysis["activeDevices"] as? Int) ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика энергии") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Оценка энергоэффективности", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        efficiencyScore >= 80 -> Color.Green
                                        efficiencyScore >= 60 -> Color.Yellow
                                        else -> Color.Red
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$efficiencyScore%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when {
                                efficiencyScore >= 80 -> "Отличная эффективность"
                                efficiencyScore >= 60 -> "Хорошая эффективность"
                                else -> "Требует оптимизации"
                            },
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Статистика потребления", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        StatItem("Общее потребление", "${"%.2f".format(totalPower)} кВт/ч")
                        StatItem("Активных устройств", activeDevices.toString())
                        StatItem("Всего устройств", devices.size.toString())
                        StatItem("Среднее на устройство", "${"%.2f".format(totalPower / devices.size.coerceAtLeast(1))} кВт/ч")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Рекомендации", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (recommendations.isNotEmpty()) {
                            recommendations.forEach { recommendation ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text("• ", fontWeight = FontWeight.Bold)
                                    Text(recommendation, modifier = Modifier.weight(1f))
                                }
                            }
                        } else {
                            Text("Ваше энергопотребление оптимально!")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Потребление по устройствам", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        devices
                            .sortedByDescending { it.powerConsumption }
                            .forEach { device ->
                                DevicePowerItem(device)
                            }
                    }
                }
            }
        }
    }
}

// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ
@Composable
fun RoomItem(room: RoomModel, isSelected: Boolean, deviceCount: Int, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = room.icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = room.name,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
            Text(
                text = deviceCount.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun ActionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(100.dp)
            .width(90.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember { mutableStateOf(device.isOnline) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) Color.Green else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = device.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${device.type} • ${getRoomName(device.roomId)}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        if (device.powerConsumption > 0) {
                            Text(
                                text = "Энергия: ${"%.2f".format(device.powerConsumption)} кВт/ч",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Switch(checked = isEnabled, onCheckedChange = {
                    isEnabled = it
                    onToggle(it)
                })
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onEdit() }
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDelete() }
                        .padding(4.dp),
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
private fun getRoomName(roomId: Long): String {
    return when (roomId) {
        2L -> "Гостиная"
        3L -> "Спальня"
        4L -> "Кухня"
        5L -> "Ванная"
        6L -> "Прихожая"
        else -> "Все комнаты"
    }
}

@Composable
fun ScenarioCard(
    scenario: Scenario,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var isActive by remember { mutableStateOf(scenario.isActive) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = scenario.name, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scenario.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Switch(checked = isActive, onCheckedChange = {
                    isActive = it
                    onToggle(it)
                })
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onEdit() }
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDelete() }
                        .padding(4.dp),
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(event.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            event.eventType.contains("error") -> Color.Red
                            event.eventType.contains("added") -> Color.Green
                            event.eventType.contains("removed") -> Color.Red
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.eventType.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier, onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchText,
        onValueChange = {
            searchText = it
            onSearch(it)
        },
        placeholder = { Text("Поиск устройств...") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DevicePowerItem(device: Device) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(device.name)
        Text("${"%.2f".format(device.powerConsumption)} кВт/ч",
            color = if (device.powerConsumption > 0.5) Color.Red else Color.Gray)
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        NavItem("Главная", Icons.Default.Home, Screen.Main.route, navController)
        NavItem("Устройства", Icons.Default.Lightbulb, Screen.Devices.route, navController)
        NavItem("Сценарии", Icons.Default.Star, Screen.Scenarios.route, navController)
        NavItem("Энергия", Icons.Default.Eco, Screen.EnergyAnalytics.route, navController)
        NavItem("Настройки", Icons.Default.Settings, Screen.Settings.route, navController)
    }
}

@Composable
fun NavItem(title: String, icon: ImageVector, route: String, navController: NavHostController) {
    Column(
        modifier = Modifier
            .clickable {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SmartHomeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF2196F3),
        secondary = Color(0xFF03DAC6),
        tertiary = Color(0xFF6200EE),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFF000000),
        onSurface = Color(0xFF000000)
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFF90CAF9),
        secondary = Color(0xFF03DAC6),
        tertiary = Color(0xFFBB86FC),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFFFFFFF)
    )

    val colorScheme = if (darkTheme) darkColors else lightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

// УПРОЩЕННЫЕ ЭКРАНЫ
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(navController: NavHostController, viewModel: SmartHomeViewModel, deviceId: Long) {
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val device = remember(deviceId, devices) { devices.find { it.id == deviceId } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали устройства") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (device != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Информация об устройстве", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Название: ${device.name}")
                        Text("Тип: ${device.type}")
                        Text("Статус: ${device.status}")
                        Text("Онлайн: ${if (device.isOnline) "Да" else "Нет"}")
                        Text("Потребление: ${"%.2f".format(device.powerConsumption)} кВт/ч")
                    }
                }
            } else {
                Text("Устройство не найдено")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioDetailScreen(navController: NavHostController, viewModel: SmartHomeViewModel, scenarioId: Long) {
    val scenarios by viewModel.scenarios.collectAsState(initial = emptyList())
    val scenario = remember(scenarioId, scenarios) { scenarios.find { it.id == scenarioId } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали сценария") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (scenario != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Информация о сценарии", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Название: ${scenario.name}")
                        Text("Описание: ${scenario.description}")
                        Text("Активен: ${if (scenario.isActive) "Да" else "Нет"}")
                    }
                }
            } else {
                Text("Сценарий не найден")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeviceScreen(navController: NavHostController, viewModel: SmartHomeViewModel, deviceId: Long) {
    val devices by viewModel.devices.collectAsState(initial = emptyList())
    val device = remember(deviceId, devices) { devices.find { it.id == deviceId } }

    if (device == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Устройство не найдено")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
        return
    }

    var deviceName by remember { mutableStateOf(device.name) }
    var deviceType by remember { mutableStateOf(device.type) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать устройство") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Редактирование устройства", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Название устройства") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deviceType,
                onValueChange = { deviceType = it },
                label = { Text("Тип устройства") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.updateDevice(
                                deviceId = deviceId,
                                name = deviceName,
                                type = deviceType
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = deviceName.isNotBlank() && deviceType.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScenarioScreen(navController: NavHostController, viewModel: SmartHomeViewModel, scenarioId: Long) {
    val scenarios by viewModel.scenarios.collectAsState(initial = emptyList())
    val scenario = remember(scenarioId, scenarios) { scenarios.find { it.id == scenarioId } }

    if (scenario == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Сценарий не найдено")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Назад")
            }
        }
        return
    }

    var scenarioName by remember { mutableStateOf(scenario.name) }
    var scenarioDescription by remember { mutableStateOf(scenario.description) }
    var isActive by remember { mutableStateOf(scenario.isActive) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать сценарий") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Редактирование сценария", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = scenarioName,
                onValueChange = { scenarioName = it },
                label = { Text("Название сценария") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = scenarioDescription,
                onValueChange = { scenarioDescription = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Активен")
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.updateScenario(
                                scenarioId = scenarioId,
                                name = scenarioName,
                                description = scenarioDescription,
                                isActive = isActive
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = scenarioName.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var deviceName by remember { mutableStateOf("") }
    var deviceType by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить устройство") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Добавить новое устройство", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Название устройства") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = deviceType,
                onValueChange = { deviceType = it },
                label = { Text("Тип устройства") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        if (deviceName.isNotBlank() && deviceType.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.addDevice(
                                    name = deviceName,
                                    type = deviceType,
                                    roomId = 2
                                )
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = deviceName.isNotBlank() && deviceType.isNotBlank()
                ) {
                    Text("Добавить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScenarioScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    var scenarioName by remember { mutableStateOf("") }
    var scenarioDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать сценарий") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Создать новый сценарий", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = scenarioName,
                onValueChange = { scenarioName = it },
                label = { Text("Название сценария") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = scenarioDescription,
                onValueChange = { scenarioDescription = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        if (scenarioName.isNotBlank()) {
                            viewModel.viewModelScope.launch {
                                viewModel.addScenario(
                                    name = scenarioName,
                                    description = scenarioDescription
                                )
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = scenarioName.isNotBlank()
                ) {
                    Text("Создать")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceIntegrationScreen(navController: NavHostController, viewModel: SmartHomeViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Интеграция устройств") },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Интеграция устройств", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Этот экран в разработке")
        }
    }
}