package ru.skillbranch.sbdelivery.screens.dish.logic

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import ru.skillbranch.sbdelivery.repository.DishRepository
import ru.skillbranch.sbdelivery.screens.root.logic.Eff
import ru.skillbranch.sbdelivery.screens.root.logic.IEffectHandler
import ru.skillbranch.sbdelivery.screens.root.logic.Msg
import javax.inject.Inject


class DishEffectHandler @Inject constructor(
    private val repository: DishRepository,
    private val notifyChannel: Channel<Eff.Notification>,
    private val dispatcher: CoroutineDispatcher  = Dispatchers.Default
) :
    IEffectHandler<DishFeature.Eff, Msg> {

    private var localJob: Job? = null

    override suspend fun handle(effect: DishFeature.Eff, commit: (Msg) -> Unit) {

        if (localJob == null) localJob = Job()

        withContext(localJob!! + dispatcher) {
            when (effect) {
                is DishFeature.Eff.AddToCart -> {
                    repository.addToCart(effect.id, effect.count)
                    val count = repository.cartCount()
                    // Обновляем значок в тулбаре с количеством блюд в корзине
                    commit(Msg.UpdateCartCount(count))
                    // Сообщаем юзеру, что блюдо в количестве count добавлено в корзину
                    notifyChannel.send(
                        Eff.Notification.Text(
                            message = "В корзину добавлено ${effect.count} товаров"
                        )
                    )
                }

                is DishFeature.Eff.LoadDish -> {
                    val dish = repository.findDish(effect.dishId)
                    commit(DishFeature.Msg.ShowDish(dish).toMsg())
                }

                is DishFeature.Eff.LoadReviews -> {
                    try {
                        val reviews = repository.loadReviews(effect.dishId)
                        commit(DishFeature.Msg.ShowReviews(reviews).toMsg())
                    } catch (t: Throwable) {
                        notifyChannel.send(Eff.Notification.Error(t.message ?: "something error"))
                    }
                }

                is DishFeature.Eff.SendReview -> {
                    try {
                        val reviewRes = repository.sendReview(effect.id, effect.rating, effect.review)
                        val reviews = effect.currReviews.toMutableList()
                        reviews.add(reviewRes)
                        commit(DishFeature.Msg.ShowReviews(reviews).toMsg())
                        notifyChannel.send(
                            Eff.Notification.Text(
                                message = "Отзыв успешно отправлен"
                            )
                        )
                    } catch (t: Throwable) {
                        notifyChannel.send(Eff.Notification.Error(t.message ?: "something error"))
                    }
                }

                is DishFeature.Eff.Terminate -> {
                    localJob?.cancel("Terminate coroutine scope")
                    // t.c. 01:59:40 если джоб отменен, то к нему уже
                    // нельзя присоединять новые джобы, нельзя запустить
                    // внутри него новые корутины
                    localJob = null
                }
            }
        }
    }

    private fun DishFeature.Msg.toMsg(): Msg = Msg.Dish(this)
}



