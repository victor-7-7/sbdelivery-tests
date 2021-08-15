package ru.skillbranch.sbdelivery.repository

import ru.skillbranch.sbdelivery.aop.LogClassMethods
import ru.skillbranch.sbdelivery.data.db.dao.CartDao
import ru.skillbranch.sbdelivery.data.db.dao.DishesDao
import ru.skillbranch.sbdelivery.data.db.entity.CartItemPersist
import ru.skillbranch.sbdelivery.data.network.RestService
import ru.skillbranch.sbdelivery.data.network.res.ReviewReq
import ru.skillbranch.sbdelivery.data.network.res.ReviewRes
import ru.skillbranch.sbdelivery.data.toDishContent
import ru.skillbranch.sbdelivery.screens.dish.data.DishContent
import java.util.*
import javax.inject.Inject

interface IDishRepository {
    suspend fun findDish(id: String): DishContent
    suspend fun addToCart(id: String, count: Int)
    suspend fun cartCount(): Int
    suspend fun loadReviews(dishId: String): List<ReviewRes>
    suspend fun sendReview(id: String, rating: Int, review: String): ReviewRes
}

@LogClassMethods
class DishRepository @Inject constructor(
    private val api: RestService,
    private val dishesDao: DishesDao,
    private val cartDao: CartDao,
) : IDishRepository {
    override suspend fun findDish(id: String): DishContent = dishesDao.findDish(id).toDishContent()

    // Здесь count - количество штук блюда, добавляемых в корзину за раз
    override suspend fun addToCart(id: String, count: Int) {
        val dishCount = cartDao.dishCount(id) ?: 0
        // Если в корзине уже есть такое блюдо, то просто увеличиваем
        // на count количество штук этого блюда в корзине
        if (dishCount > 0) cartDao.updateItemCount(id, dishCount + count)
        else {
            // Если в корзине такого блюда еще не было, то добавляем его
            // с указанием количества добавки
            cartDao.addItem(CartItemPersist(dishId = id, count = count))
        }
    }

    override suspend fun cartCount(): Int = cartDao.cartCount() ?: 0

    override suspend fun loadReviews(dishId: String): List<ReviewRes> {
        val reviews = mutableListOf<ReviewRes>()
        try {
            val resp = api.getReviews(dishId, 0, 10)
            if (resp.isSuccessful) {
                reviews.addAll(resp.body()!!)
            } else {
                reviews.addAll(reviewsStub())
            }
        } catch (e: Exception) {
            reviews.addAll(reviewsStub())
        }
        return reviews
    }

    override suspend fun sendReview(id: String, rating: Int, review: String): ReviewRes {
        /* TODO: получить имя отправителя [а как в тестах?] ? */
        val reviewResStub = ReviewRes("xxx", Date().time, rating, review)
        return try {
            /* TODO: откуда взять токен ? */
            api.sendReview(id, ReviewReq(Date().time, rating, review), "token")
        } catch (e: Exception) {
            reviewResStub
        }
    }

    private fun reviewsStub(): List<ReviewRes> {
        val cal = Calendar.getInstance()
        cal.set(2021, 8, 10)
        return listOf(
            ReviewRes("Глеб", cal.timeInMillis, 4, "Понравилось"),
            ReviewRes("Алина", cal.timeInMillis - 5 * 60 * 60 * 1000, 1, "Не вкусно"),
            ReviewRes("Иван", cal.timeInMillis + 2 * 60 * 60 * 1000, 3, "Что-то среднее")
        )
    }
}