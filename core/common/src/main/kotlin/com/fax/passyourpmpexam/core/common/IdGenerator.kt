package com.fax.passyourpmpexam.core.common

import java.util.UUID

/** Abstracts id creation so persisted rows get stable ids in production and deterministic ones in tests. */
fun interface IdGenerator {
    fun newId(): String
}

class UuidGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
