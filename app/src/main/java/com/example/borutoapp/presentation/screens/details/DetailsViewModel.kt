package com.example.borutoapp.presentation.screens.details

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.borutoapp.domain.model.Hero
import com.example.borutoapp.domain.use_cases.UseCases
import com.example.borutoapp.util.Constants.DETAILS_ARGUMENT_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val useCases: UseCases,
    savedStateHandle: SavedStateHandle //SavedStateHandle HeroId yi çekecek home yada search screen de kahramanlara tıkladığımızda
) : ViewModel() {

    private val _selectedHero: MutableStateFlow<Hero?> = MutableStateFlow(null)
    val selectedHero: StateFlow<Hero?> = _selectedHero //veri çekiyor detail sayfasında verileri gözlemyiyor (observe)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val heroId = savedStateHandle.get<Int>(DETAILS_ARGUMENT_KEY) //heroId
            _selectedHero.value = heroId?.let { useCases.getSelectedHeroUseCase(heroId = heroId) }
            //verileri yerel veritabanımızdan çekiyoruz
        }
    }

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _colorPalette: MutableState<Map<String, String>> = mutableStateOf(mapOf())
    val colorPalette: State<Map<String, String>> = _colorPalette

    fun generateColorPalette() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.GenerateColorPalette)
        }
    }

    fun setColorPalette(colors: Map<String, String>) {
        _colorPalette.value = colors
    }

}

sealed class UiEvent {
    object GenerateColorPalette : UiEvent()
}

/*
*tüm nesneleri geçirmiyoruz bu kötü bir pratik.primative tiple bir ıd belirterek verileri getiriyoruz
*yerel veri tabanından  verileri çekiyoruz
*hero itemlarından birine basılınca(home yada search screen de) seçilen heroId DetailsViewModel kullanılarak detay ekranına aktarılacak,
* daha sonra init bloğu içerisinde yerel database den verileri çekiyoruz
*
 */