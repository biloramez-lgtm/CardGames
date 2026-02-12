package com.example.tasalicool.network

import com.google.gson.Gson
import java.io.Serializable

data class NetworkMessage(
    val messageId: String = generateId(),
    val playerId: String,
    val gameType: String,
    val action: GameAction,

    // بيانات مرنة (بطاقة – ورق – سكورات – رسالة...)
    val payload: String? = null,

    // اسم اللاعب (اختياري – يفيد بالماساتج)
    val playerName: String? = null,

    // لمن الرسالة؟ (null = للجميع)
    val targetPlayerId: String? = null,

    // رقم الجولة (لمنع التعارض)
    val round: Int? = null,

    // وقت الإرسال
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {

    companion object {
        private val gson = Gson()

        fun toJson(message: NetworkMessage): String {
            return gson.toJson(message)
        }

        fun fromJson(json: String): NetworkMessage {
            return gson.fromJson(json, NetworkMessage::class.java)
        }

        private fun generateId(): String {
            return System.currentTimeMillis().toString() + (1000..9999).random()
        }
    }
}

enum class GameAction {
    JOIN,
    LEAVE,
    START_GAME,

    // توزيع أوراق
    DEAL_CARDS,

    // لعب ورقة
    PLAY_CARD,

    // مزامنة الحالة
    SYNC_STATE,
    UPDATE_GAME_STATE,

    // سكورات
    UPDATE_SCORE,

    // ماسج دردشة
    MESSAGE
}

// دالة مساعدة عامة
fun generateId(): String {
    return System.currentTimeMillis().toString() + (1000..9999).random()
}
