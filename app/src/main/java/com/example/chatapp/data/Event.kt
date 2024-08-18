package com.example.chatapp.data

open class Event<out T>(val content: T) {
    var hasBeenHamdled = false
    fun getContentOrNull(): T?{
        return if (hasBeenHamdled) null
        else {
            hasBeenHamdled = true
            content
        }
    }
}