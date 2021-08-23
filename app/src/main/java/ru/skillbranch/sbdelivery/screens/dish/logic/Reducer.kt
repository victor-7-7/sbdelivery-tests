package ru.skillbranch.sbdelivery.screens.dish.logic

import ru.skillbranch.sbdelivery.screens.dish.data.DishUiState
import ru.skillbranch.sbdelivery.screens.dish.data.ReviewUiState
import ru.skillbranch.sbdelivery.screens.root.logic.Eff
import ru.skillbranch.sbdelivery.screens.root.logic.RootState
import ru.skillbranch.sbdelivery.screens.root.logic.ScreenState

fun DishFeature.State.selfReduce(msg: DishFeature.Msg) : Pair<DishFeature.State, Set<Eff>> {
    val pair = when (msg) {
        // Здесь msg.count - количество штук блюда, добавляемых в корзину за раз. При этом мы
        // в стейте сбрасываем свойство count (цифру в степпере) к 1
        is DishFeature.Msg.AddToCart -> copy(count = 1) to setOf(DishFeature.Eff.AddToCart(msg.id, msg.count)).toEffs()
        // На странице товара невозможно сбросить степпер до 0 (поскольку кнопка "минус"
        // исчезает, когда степпер = 1). Но для прохождения теста increment_decrement_count()
        // реализую эту логику
        is DishFeature.Msg.DecrementCount -> {
            var temp = count - 1
            if (temp == 0) temp = 1
            copy(count = temp) to emptySet<Eff>()
        }
        is DishFeature.Msg.HideReviewDialog -> copy(isReviewDialog = false) to emptySet<Eff>()
        is DishFeature.Msg.IncrementCount -> copy(count = count + 1) to emptySet<Eff>()
        is DishFeature.Msg.SendReview -> {
            val reviewList = if (reviews is ReviewUiState.Empty) emptyList()
            else (reviews as ReviewUiState.Value).list
            copy(isReviewDialog = false, reviews = ReviewUiState.ValueWithLoading(reviewList)) to
                    setOf(DishFeature.Eff.SendReview(msg.dishId, msg.rating, msg.review, reviewList)).toEffs()
        }
        is DishFeature.Msg.ShowDish -> copy(content = DishUiState.Value(msg.dish)) to emptySet<Eff>()
        is DishFeature.Msg.ShowReviewDialog -> copy(isReviewDialog = true) to emptySet<Eff>()
        is DishFeature.Msg.ShowReviews -> copy(reviews = ReviewUiState.Value(msg.reviews)) to emptySet<Eff>()
        // По хорошему тут еще надо добавить эффект ToggleLike, а в хендлере эффектов
        // записать в БД девайса, что данное блюдо лайкнуто владельцем девайса
        is DishFeature.Msg.ToggleLike -> copy(isLiked = !isLiked) to emptySet<Eff>()
    }

    return pair
}

fun DishFeature.State.reduce(root: RootState, msg: DishFeature.Msg): Pair<RootState, Set<Eff>> {
    val (dishState, effs) = selfReduce(msg)
    // Блок copy(dishState = dishState) будет выполнен на экземпляре ScreenState.Dish,
    // который имеет свойство dishState типа DishFeature.State
    return root.updateCurrentScreenState<ScreenState.Dish> { copy(dishState = dishState) } to effs
}

private fun Set<DishFeature.Eff>.toEffs(): Set<Eff> = mapTo(HashSet(), Eff::Dish)
