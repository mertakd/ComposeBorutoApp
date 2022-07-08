package com.example.borutoapp.util

object Constants {

    //ethernet bağlıysa ethernet ip ni wifi bağlıysa wifi ip ni base url e koyacaksın.kendi ip ni öğrenmek için terminale ipconfig yaz.

    //const val BASE_URL = "http://192.168.56.1:8080"


    //eski wifi http://192.168.1.39:8080
    //yeni wifi 192.168.1.33
    //const val BASE_URL = "http://10.0.2.2:8080" //tüm emulatörlerde açar

    const val BASE_URL = "http://192.168.1.35:8080" //wifi ip

    const val DETAILS_ARGUMENT_KEY = "heroId"

    const val BORUTO_DATABASE = "boruto_database"
    const val HERO_DATABASE_TABLE = "hero_table"
    const val HERO_REMOTE_KEYS_DATABASE_TABLE = "hero_remote_keys_table"

    const val PREFERENCES_NAME = "boruto_preferences"
    const val PREFERENCES_KEY = "on_boarding_completed"

    const val ON_BOARDING_PAGE_COUNT = 3
    const val LAST_ON_BOARDING_PAGE = 2

    const val ITEMS_PER_PAGE = 3
    const val ABOUT_TEXT_MAX_LINES = 12

    const val MIN_BACKGROUND_IMAGE_HEIGHT = 0.4f




















}