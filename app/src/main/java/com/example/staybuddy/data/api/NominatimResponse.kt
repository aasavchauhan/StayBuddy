package com.example.staybuddy.data.api

data class NominatimResponse(
    val place_id: Long,
    val lat: String,
    val lon: String,
    val display_name: String,
    val address: Address? = null
)

data class Address(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state_district: String? = null,
    val state: String? = null,
    val country: String? = null,
    val suburb: String? = null,
    val neighbourhood: String? = null,
    val residential: String? = null,
    val road: String? = null
)
