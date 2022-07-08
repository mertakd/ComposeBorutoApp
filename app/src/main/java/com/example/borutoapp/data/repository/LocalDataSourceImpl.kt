package com.example.borutoapp.data.repository

import com.example.borutoapp.data.local.BorutoDatabase
import com.example.borutoapp.domain.model.Hero
import com.example.borutoapp.domain.repository.LocalDataSource

class LocalDataSourceImpl(borutoDatabase: BorutoDatabase): LocalDataSource {

    private val heroDao = borutoDatabase.heroDao()

    override suspend fun getSelectedHero(heroId: Int): Hero {
        return heroDao.getSelectedHero(heroId = heroId) //kahramanlarımızı alıyoruz burda
    }
}

//localdatasource di modulunü yazıyoruz
//repository e localdata source u inject ediyoruz ve getSelectedHero adında fonksiyonu yazıyoruz
//sonra GetSelectedHeroUseCase sınıfını oluşturuyoruz ve Use Cases sınıfına ekliyoruz
//repository module use case i ekliyoruz