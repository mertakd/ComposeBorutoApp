package com.example.borutoapp.domain.repository

import com.example.borutoapp.domain.model.Hero

interface LocalDataSource {
    suspend fun getSelectedHero(heroId: Int): Hero

    //tekrar istek atmak istemiyoruz o yüzden veriler roomdan çekiceğiz
    //HeroDao daki getSelectedHero fonksiyonunu burada kullanacağız
}