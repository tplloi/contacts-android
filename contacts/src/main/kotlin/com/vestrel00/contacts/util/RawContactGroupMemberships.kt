package com.vestrel00.contacts.util

import com.vestrel00.contacts.entities.Group
import com.vestrel00.contacts.entities.GroupMembership

fun Group.toGroupMembership(): GroupMembership = GroupMembership(
    id = null,
    rawContactId = null,
    contactId = null,
    groupId = id,
    isPrimary = false,
    isSuperPrimary = false
)

fun Collection<Group>.toGroupMemberships(): List<GroupMembership> = map { it.toGroupMembership() }

fun Collection<Group>.defaultGroup(): Group? = firstOrNull { it.isDefaultGroup }

fun Collection<Group>.favoritesGroup(): Group? = firstOrNull { it.isFavoritesGroup }

fun Sequence<Group>.toGroupMemberships(): Sequence<GroupMembership> = map { it.toGroupMembership() }

fun Sequence<Group>.defaultGroup(): Group? = firstOrNull { it.isDefaultGroup }

fun Sequence<Group>.favoritesGroup(): Group? = firstOrNull { it.isFavoritesGroup }