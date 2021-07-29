package ru.bozaro.gitlfs.common

import ru.bozaro.gitlfs.common.data.Lock

class LockConflictException(message: String?, val lock: Lock) : Exception(message) {

    constructor(lock: Lock) : this("Lock exists", lock)
}
