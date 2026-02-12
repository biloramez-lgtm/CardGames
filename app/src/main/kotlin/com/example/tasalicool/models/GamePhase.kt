package com.example.tasalicool.models

enum class GamePhase {

    // في انتظار اللاعبين (وضع WiFi)
    WAITING_FOR_PLAYERS,

    // مرحلة المزايدة
    BIDDING,

    // أثناء اللعب
    PLAYING,

    // انتهت الجولة
    ROUND_END,

    // انتهت اللعبة بالكامل
    GAME_OVER
}
