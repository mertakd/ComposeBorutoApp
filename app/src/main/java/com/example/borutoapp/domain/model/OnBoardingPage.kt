package com.example.borutoapp.domain.model

import androidx.annotation.DrawableRes
import com.example.borutoapp.R

sealed class OnBoardingPage(
    @DrawableRes
    val image: Int,
    val title: String,
    val description: String
) {
    object First : OnBoardingPage(
        image = R.drawable.greetings,
        title = "Merhaba",
        description = "Boruto hayranı mısın? Eğer öyleysen o zaman sana harika bir haberimiz var.!"
    )

    object Second : OnBoardingPage(
        image = R.drawable.explore,
        title = "Keşfet",
        description = "En sevdiğiniz kahramanları bulun ve bilmediğiniz bazı şeyleri öğrenin."
    )

    object Third : OnBoardingPage(
        image = R.drawable.power,
        title = "Güç",
        description = "Kahramanınızın gücünü kontrol edin ve diğerlerine kıyasla ne kadar güçlü olduklarını görün."
    )
}
