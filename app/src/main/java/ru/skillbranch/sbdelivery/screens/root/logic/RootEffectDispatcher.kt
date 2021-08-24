package ru.skillbranch.sbdelivery.screens.root.logic

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import ru.skillbranch.sbdelivery.repository.RootRepository
import ru.skillbranch.sbdelivery.screens.cart.logic.CartEffectHandler
import ru.skillbranch.sbdelivery.screens.dish.logic.DishEffectHandler
import ru.skillbranch.sbdelivery.screens.dishes.logic.DishesEffectHandler
import javax.inject.Inject


class EffDispatcher @Inject constructor(
    private val dishesHandler: DishesEffectHandler,
    private val dishHandler: DishEffectHandler,
    private val cartHandler: CartEffectHandler,

    private val rootRepository : RootRepository,

    //Channels for ui (ui effects) and android command
    private val _notifyChannel: Channel<Eff.Notification>,
    private val _commandChannel: Channel<Command>

) : IEffectHandler<Eff, Msg> {
    // fan-out => развёртываться как веер
    // receiveAsFlow -> Represents the given receive channel as a hot flow and
    // receives from the channel in fan-out fashion every time this flow is
    // collected. One element will be emitted to one collector only

    // К этому флоу, связанному с чаннелом эффектов-нотификаций,
    // приколлекчен компоузбл RootScreen
    val notifications = _notifyChannel.receiveAsFlow()
    // К этому флоу, связанному с чаннелом андроид-команд, приколлекчена RootActivity
    val androidCommands = _commandChannel.receiveAsFlow()

    override suspend fun handle(effect: Eff, commit: (Msg) -> Unit) {
        Log.e("EffDispatcher", "EFF $effect")

        when (effect) {
            is Eff.Dishes -> dishesHandler.handle(effect.eff, commit)
            is Eff.Dish -> dishHandler.handle(effect.eff, commit)
            is Eff.Cart -> cartHandler.handle(effect.eff, commit)

            //root effects handle
            is Eff.SyncCounter -> {
                val count = rootRepository.cartCount()
                commit(Msg.UpdateCartCount(count))
            }

            is Eff.Notification -> _notifyChannel.send(effect)
            is Eff.Navigate -> {
                // t.c. 01:33:00 можно реализовать навигацю одним из двух способов:
                // 1. to downstream and handle in activity layer
                // 2. or handle in state layer
                commit(Msg.Navigate(effect.cmd))
            }
            is Eff.Cmd -> _commandChannel.send(effect.cmd)
        }
    }
}


