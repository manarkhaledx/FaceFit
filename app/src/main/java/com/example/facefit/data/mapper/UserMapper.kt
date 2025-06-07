package com.example.facefit.data.mapper

import com.example.facefit.data.models.User as DataUser
import com.example.facefit.domain.models.User as DomainUser

fun DataUser.toDomainUser(): DomainUser {
    return DomainUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone,
        address = address,
        profilePicture = profilePicture
    )
}