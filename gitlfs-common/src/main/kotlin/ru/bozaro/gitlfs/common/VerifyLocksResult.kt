package ru.bozaro.gitlfs.common

import ru.bozaro.gitlfs.common.data.Lock

class VerifyLocksResult(val ourLocks: List<Lock>, val theirLocks: List<Lock>)
