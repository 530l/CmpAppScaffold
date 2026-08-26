package com.lyf.cmpdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform