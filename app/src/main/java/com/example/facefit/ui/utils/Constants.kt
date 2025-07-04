package com.example.facefit.ui.utils

object Constants {
    const val BASE_URL = "http://localhost:5007/facefit/customers/"
    const val SIGNUP_ENDPOINT = "facefit/customers/signup"
    const val LOGIN_ENDPOINT = "facefit/customers/login"
    const val BESTSELLERS_ENDPOINT = "facefit/glasses/bestsellers"
    const val NEWARRIVALS_ENDPOINT = "facefit/glasses/newarrivals"
    const val FILTER_GLASSES_ENDPOINT = "facefit/glasses/search"
    const val ALL_GLASSES_ENDPOINT = "facefit/glasses/all"
    const val SINGLE_GLASSES_ENDPOINT = "facefit/glasses/search"
    const val FAVORITES_ENDPOINT = "facefit/customers/favorites/{glassesid}"
    const val GET_FAVORITES_ENDPOINT = "facefit/customers/favorites"
    const val GET_REVIEWS_ENDPOINT = "facefit/reviews/{glassesId}"
    const val GET_CUSTOMER_PROFILE_ENDPOINT = "facefit/customers/profile"
    const val SUBMIT_REVIEW_ENDPOINT = "/facefit/reviews/add"
    const val EMULATOR_URL = "https://facefit.onrender.com"
    const val GET_IMAGE_ENDPOINT = "https://facefit.onrender.com/uploads/usersPictures/"
    const val UPDATE_USER_PROFILE_ENDPOINT = "facefit/customers/update"
    const val UPLOAD_PROFILE_PICTURE_ENDPOINT = "facefit/customers/profile-picture"
    const val CREATE_PRESCRIPTION_ENDPOINT = "facefit/prescriptions"
    const val ADD_TO_CART_ENDPOINT = "facefit/cart/add"
    const val GET_CART_ENDPOINT = "facefit/cart"
    const val UPDATE_CART_ITEM_ENDPOINT = "facefit/cart/edit/{cartItemId}"
    const val REMOVE_CART_ITEM_ENDPOINT = "facefit/cart/delete/{cartItemId}"
    const val CLEAR_CART_ENDPOINT = "facefit/cart/clear"
    const val CREATE_ORDER_ENDPOINT = "facefit/orders/checkout"
    const val GET_ORDERS_ENDPOINT = "facefit/orders"
    const val GET_PRIMAGE_ENDPOINT = "https://facefit.onrender.com/"
}