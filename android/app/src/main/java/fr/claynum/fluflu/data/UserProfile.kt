package fr.claynum.fluflu.data

enum class ProfileType { PERSONAL, DEMONSTRATION }

data class UserProfile(
    val id: String,
    val firstName: String,
    val type: ProfileType,
    val createdAt: Long
)
