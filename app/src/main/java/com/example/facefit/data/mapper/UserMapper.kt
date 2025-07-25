package com.example.facefit.data.mapper

import com.example.facefit.data.models.requests.UpdateUserRequest
import com.example.facefit.data.models.User as DataUser
import com.example.facefit.domain.models.User as DomainUser
import com.example.facefit.domain.models.User
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

fun User.toUpdateRequest(): UpdateUserRequest {
    return UpdateUserRequest(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phoneNumber = this.phone,
        address = this.address
    )

}