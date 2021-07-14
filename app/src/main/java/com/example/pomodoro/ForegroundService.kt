package com.example.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ForegroundService : Service() {


    private var isServiceStarted = false //флаг, определяет запущен ли сервис или нет, чтобы не стартовать повторно.

    private var notificationManager: NotificationManager? = null //мы будем обращаться к NotificationManager, когда
    // нам нужно показать нотификацию или обновить её состояние. Это системный класс, мы можем влиять на отображение
    // нотификаций только через него. Отсюда некоторые ограничения. Например, мы не сможем обновлять нотификацию чаще,
    // чем 1 раз в секунду, NotificationManager просто не даст нам такой возможности. Но можете попробовать.

    private var job: Job? = null //тут будет хранится Job нашей корутины, в которой мы запускаем обновление
    // секундомера в нотификации. Мы сможен вызвать job?.cancel(), чтобы остановить корутину, когда сервис будет завершать свою работу.

    //private val builder by lazy { - Notification Builder понадобиться нам всякий раз когда мы будем обновлять нотификацию,
    // но некоторые значения Builder остаются неизменными. Поэтому мы создаем Builder при первом обращении к нему с этими параметрами.
    // Теперь при каждом повторном обращении к builder он вернет нам готовую реализацию.


    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro")
            .setGroup("Pomodoro")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(getPendingIntent())  //при нажатии на нотификацию мы будем возвращаться в MainActivity.
            .setSilent(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
    }

    //В onCreate() создаём экземпляр NotificationManager
    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    //В onStartCommand() обрабатываем Intent. Этот метод вызывается когда сервис запускается.
    // Мы будем передавать параметры для запуска и остановки сервиса через Intent.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    //В processCommand() получаем данные из Intent и определяем что делаем дальше: стартуем или останавливаем сервис.
    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val startTimeNotification = intent?.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                commandStart(startTimeNotification)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }
//    Если получили команду на старт сервиса:
//moveToStartedState() - вызываем startForegroundService() или startService() в зависимости от текущего API.
// Почему мы это делаем внутри сервиса? Т.к. метод startForeground() будет выдавать ошибку если будет вызываться на
// другом контексте, отличном от контекста в startForegroundService() или startService(). Почему мы вызываем разные
// методы в зависимости от API? В Android O (API 26) произошли существенные изменения в регулировании Services системой.
// Одно из главных изменений в том, что Foreground Service, который не в белом списке или который явно не сообщает
// пользователю о своей работе, не будет запускаться в фоновом потоке после закрытия Activity. Другими словами,
// вы должны создать notification, к которому вы прикрепляете Foreground Service, чтобы сервис продолжал работу.
// И вы должны запускать сервис с помощью нового метода  startForegroundService() (а не с помощью startService()).
// И, после создания сервиса, у вас есть пять секунд чтобы вызвать метод startForeground() запущенной службы и показать
// видимое пользователю уведомление. Иначе система останавливает сервис и показывает ANR
//startForegroundAndShowNotification() - создаем канал, если API >= Android O. Создаем нотификацию и вызываем startForeground()
//continueTimer(startTime) - продолжаем отсчитывать секундомер. Тут мы запускаем корутину, которую кэнсельнем,
// когда сервис будет стопаться. В корутине каждую секунду обновляем нотификацию. И как уже было сказано, о
// бновлять чаще будет проблематично.

    private fun commandStart(startTimeNotification: Long) {
        isServiceStarted = startTimeNotification == 0L
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(startTimeNotification)
        } finally {
            isServiceStarted = true
        }
    }

    //continueTimer(startTime) - продолжаем отсчитывать секундомер.
    // Тут мы запускаем корутину, которую кэнсельнем, когда сервис будет стопаться.
    // В корутине каждую секунду обновляем нотификацию. И как уже было сказано,
    // обновлять чаще будет проблематично.
    private fun continueTimer(startTimeNotification: Long) {
        job = GlobalScope.launch(Dispatchers.Main) {
            if (startTimeNotification != 0L) {
                while (true) {
                    notificationManager?.notify(
                        NOTIFICATION_ID,
                        getNotification((startTimeNotification - System.currentTimeMillis()).displayTime()
                        )
                    )
                    delay(INTERVAL)
                }
            }
        }
    }

    //commandStop() - останавливаем обновление секундомера job?.cancel(),
    // убираем сервис из форегроунд стейта stopForeground(true), и останавливаем сервис stopSelf()
    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        try {
            job?.cancel() //Мы сможен вызвать job?.cancel(), чтобы остановить корутину, когда сервис будет завершать свою работу.
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }
    //moveToStartedState() - вызываем startForegroundService() или startService() в зависимости от текущего API.
    // Почему мы это делаем внутри сервиса? Т.к. метод startForeground() будет выдавать ошибку если будет вызываться
    // на другом контексте, отличном от контекста в startForegroundService() или startService(). Почему мы вызываем
    // разные методы в зависимости от API? В Android O (API 26) произошли существенные изменения в регулировании
    // Services системой. Одно из главных изменений в том, что Foreground Service, который не в белом списке или
    // который явно не сообщает пользователю о своей работе, не будет запускаться в фоновом потоке после закрытия Activity.
    // Другими словами, вы должны создать notification, к которому вы прикрепляете Foreground Service,
    // чтобы сервис продолжал работу. И вы должны запускать сервис с помощью нового метода  startForegroundService()
    // (а не с помощью startService()). И, после создания сервиса, у вас есть пять секунд чтобы вызвать метод startForeground()
    // запущенной службы и показать видимое пользователю уведомление. Иначе система останавливает сервис и показывает ANR
    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    //startForegroundAndShowNotification() - создаем канал, если API >= Android O. Создаем нотификацию и вызываем startForeground()
    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }


    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}