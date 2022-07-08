package com.example.borutoapp.data.paging_source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.borutoapp.data.local.BorutoDatabase
import com.example.borutoapp.data.remote.BorutoApi
import com.example.borutoapp.domain.model.Hero
import com.example.borutoapp.domain.model.HeroRemoteKeys
import javax.inject.Inject

@ExperimentalPagingApi
class HeroRemoteMediator(
    private val borutoApi: BorutoApi,
    private val borutoDatabase: BorutoDatabase
) : RemoteMediator<Int, Hero>() {

    private val heroDao = borutoDatabase.heroDao()
    private val heroRemoteKeysDao = borutoDatabase.heroRemoteKeysDao()

    override suspend fun initialize(): InitializeAction {
        val currentTime = System.currentTimeMillis()
        val lastUpdated = heroRemoteKeysDao.getRemoteKeys(heroId = 1)?.lastUpdated ?: 0L
        val cacheTimeout = 1440

        val diffInMinutes = (currentTime - lastUpdated) / 1000 / 60
        return if (diffInMinutes.toInt() <= cacheTimeout) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Hero>): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    prevPage
                }
                LoadType.APPEND -> { //append geri çağırma sayfalama verilerinin sonunda işleve girer
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    nextPage
                }
            }

            val response = borutoApi.getAllHeroes(page = page)
            if (response.heroes.isNotEmpty()) {
                borutoDatabase.withTransaction {
                    if (loadType == LoadType.REFRESH) { //uygulama ilk çalıştığında tetiklendiği yer yani sunucudan istekde bulunur.
                        heroDao.deleteAllHeroes() //Şimdi, bu durumda, veritabanı tablolarımızdan her şeyi kaldırmak istiyoruz
                        heroRemoteKeysDao.deleteAllRemoteKeys()
                    }
                    val prevPage = response.prevPage //verileri yeniden alıyoruz
                    val nextPage = response.nextPage
                    val keys = response.heroes.map { hero -> //anahtar nesneyi veritabanımıza kaydetmek veya kaldırmak için kullanılabilir.
                        HeroRemoteKeys(
                            id = hero.id,
                            prevPage = prevPage,
                            nextPage = nextPage,
                            lastUpdated = response.lastUpdated
                        )
                    }
                    heroRemoteKeysDao.addAllRemoteKeys(heroRemoteKeys = keys) //burda da verileri ekliyoruz
                    heroDao.addHeroes(heroes = response.heroes)
                }
            }
            MediatorResult.Success(endOfPaginationReached = response.nextPage == null)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Hero>
    ): HeroRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                heroRemoteKeysDao.getRemoteKeys(heroId = id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, Hero>
    ): HeroRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { hero ->
                heroRemoteKeysDao.getRemoteKeys(heroId = hero.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, Hero>
    ): HeroRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { hero ->
                heroRemoteKeysDao.getRemoteKeys(heroId = hero.id)
            }
    }

//    private fun parseMillis(millis: Long): String {
//        val date = Date(millis)
//        val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.ROOT)
//        return format.format(date)
//    }

    /*
                PAGİNG 3 LİBRARY
    -Şimdi ilk sayfalama kaynağıyla(paging source) başlayalım, böylece bir sayfalama kaynağı verileri tek bir sayfadan alır.
    ağ, yerel veritabanı dosyası vb. gibi veri kaynağı.
    Bu class sayfalanmış verinin kaynağını ve bu kaynaktan nasıl veri alınacağıyla sorumlu bir abstract generic class.
    İki parametre alan çağrı kaynağı, değerde anahtardır, bu nedenle değer parametresi,
    yüklenecek ve kullanıcılara gösterilecek veriler.
    -Room u destekler sizin manuel olarak bir şey yapmanıza gerek kalmaz.
    yerel veritabanındaki verileri sayfalandırmak için kendi sayfalama kaynak sınıfınızı uygulamanız gerekmez.

    -Paging Data, paging source un bulunduğu yerdir.Yanıt olarak bir çağrı kaynağıdır lazy coulum da kullanılırken.
    daha çok veri görmek istersek ekranda ilk baş paging data sonra paging source a bildirilir böylece yeni bir veri sayfası alınır

    -Paging Config : bir sayfalama yapılandırması, bir yükleme davranışını yapılandırmak için kullanılan bir sınıftır.
    page size, initial load size, max size özellikleri vardır
    page size:
    initial load size : burda 3 verisini veririz çünkü 3 sayfamız var. Paging Size  * 3 olarak.
    max size: ögelerin sayısını sınırlamak için kullanılır

    -Pager (pager class): veri akışı elde etmemizi sağlar. paging source un nasıl tanımlanacağının yapılandırıldığı yerdir.

    -RemoteMediator : Eğer ki hem remote hem de local bazlı(Buradaki kasıt remotetaki veriyi locale yazıp tek bir kaynak üzerinden gitme işlemi) bir yapıda çalışacaksak RemoteMediator kullanmamız gerecek.
     Offline caching yapabiliyor
      uygulama yalnızca veritabanında önbelleğe alınmış verileri görüntüler.
      remote mediator sinyal görevi görür.
      remote keys: uzak anahtarlar, uzak arabulucu uygulamasının arka uç hizmetine anlatmak için kullandığı anahtarlardır.
     */

    /*
    -           REMOTE MEDIATOR
    Üç tane parametresi var Network service, database, Query
    Bu nedenle bir sorgu, arka uç sunucusundan hangi verilerin alınacağını tanımlayan basit bir dizedir.
    biz sadece newtwork serice i kullanıcaz

    -Load iki parametre alır  Paging State ve LoadType
    Paging State alır o ana kadar yüklenen sayfalar hakkındaki bilgileri içeren bir sayfalama durumudur.
    LoadType: load türünü belirtir yükleme türünü yani.  refresh, append , prepend
    MediatorResults: Dolayısıyla bir aracı sonucu, aracılık veya sonuç hatası veya sonuç başarısı olabilir
     */


    /*
    -yaptığımız şey, kahraman uzaktan kumanda anahtarımızı kullandığımız bir tablodur.
    önceki ve sonraki sayfaların uzaktan kumanda tuşlarını alın, böylece önceki ve sonraki sayfaları gerçekten talep edebiliriz.
    sunucumuz.
     */

}