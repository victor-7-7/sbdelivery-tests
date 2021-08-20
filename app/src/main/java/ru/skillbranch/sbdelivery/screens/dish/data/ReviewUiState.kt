package ru.skillbranch.sbdelivery.screens.dish.data

import ru.skillbranch.sbdelivery.data.network.res.ReviewRes
import java.io.Serializable

sealed class ReviewUiState : Serializable {
    object Loading : ReviewUiState()
    // Отзывы покупателей
    data class Value(val list: List<ReviewRes>) : ReviewUiState()
    // Этот класс нужен чтобы на момент отправки нового сообщения текущие сообщения
    // не скрывались, а просто поверх них отображался лоадер
    data class ValueWithLoading(val list: List<ReviewRes>) : ReviewUiState()
    object Empty : ReviewUiState()
}