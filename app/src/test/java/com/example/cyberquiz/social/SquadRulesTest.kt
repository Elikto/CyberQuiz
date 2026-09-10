package com.example.cyberquiz.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SquadRulesTest {
    @Test
    fun `squad accepts up to three distinct friends`() {
        var selected = emptySet<String>()
        selected = toggleSquadFriend(selected, "alice")
        selected = toggleSquadFriend(selected, "bob")
        selected = toggleSquadFriend(selected, "carol")
        selected = toggleSquadFriend(selected, "dave")

        assertEquals(setOf("alice", "bob", "carol"), selected)
        assertEquals(MAX_SQUAD_FRIENDS, selected.size)
    }

    @Test
    fun `tapping selected friend removes it and frees a slot`() {
        val selected = setOf("alice", "bob", "carol")
        val afterRemove = toggleSquadFriend(selected, "bob")
        val afterAdd = toggleSquadFriend(afterRemove, "dave")

        assertEquals(setOf("alice", "carol", "dave"), afterAdd)
    }

    @Test
    fun `blank friend ids are ignored`() {
        assertEquals(setOf("alice"), toggleSquadFriend(setOf("alice"), "   "))
    }

    @Test
    fun `only supported shared quiz lengths are accepted`() {
        SQUAD_QUESTION_COUNTS.forEach { assertTrue(isSupportedSquadQuestionCount(it)) }
        assertFalse(isSupportedSquadQuestionCount(0))
        assertFalse(isSupportedSquadQuestionCount(15))
        assertFalse(isSupportedSquadQuestionCount(200))
    }
}
