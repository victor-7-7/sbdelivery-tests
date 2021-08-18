package ru.skillbranch.sbdelivery.screens.dish.logic

import ru.skillbranch.sbdelivery.screens.dish.data.DishUiState
import ru.skillbranch.sbdelivery.screens.dish.data.ReviewUiState
import ru.skillbranch.sbdelivery.screens.root.logic.Eff
import ru.skillbranch.sbdelivery.screens.root.logic.RootState
import ru.skillbranch.sbdelivery.screens.root.logic.ScreenState

fun DishFeature.State.selfReduce(msg: DishFeature.Msg) : Pair<DishFeature.State, Set<Eff>> {
    val pair = when (msg) {
        // Здесь msg.count - количество штук блюда, добавляемых в корзину за раз
        is DishFeature.Msg.AddToCart -> this to setOf(DishFeature.Eff.AddToCart(msg.id, msg.count)).toEffs()
        is DishFeature.Msg.DecrementCount -> copy(count = count - 1) to emptySet<Eff>()
        is DishFeature.Msg.HideReviewDialog -> copy(isReviewDialog = false) to emptySet<Eff>()
        is DishFeature.Msg.IncrementCount -> copy(count = count + 1) to emptySet<Eff>()
        is DishFeature.Msg.SendReview -> copy(isReviewDialog = false) to
                setOf(DishFeature.Eff.SendReview(msg.dishId, msg.rating, msg.review)).toEffs()
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
