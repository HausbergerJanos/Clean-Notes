package com.codingwithmitch.cleannotes.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: String,
    val createdAt: String
): Parcelable