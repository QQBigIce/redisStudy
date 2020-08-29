package com.hp.pojo

import java.io.Serializable

data class User(
        var name: String,
        var age: Int
) : Serializable {
}