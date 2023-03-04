package game

import kotlinx.coroutines.*
import java.time.Instant
import java.util.*

const val FPS = 60L

class Game {
    val keyboard = Keyboard()
    val objectContainer = ObjectContainer()
    var engine = Engine(objectContainer = objectContainer, keyboard = keyboard)

    fun run() = runBlocking {
        var prev = Instant.now()

        launch(newSingleThreadContext("foo")) {
            while (true) {
                keyboard.accept()
            }
        }

        while (true) {
            val now = Instant.now()
            val elapsed = now.toEpochMilli() - prev.toEpochMilli()

            loop(engine, elapsed * FPS / 1000.0)

            prev = now
        }
    }
}

fun loop(engine: Engine, frames: Double) = runBlocking {
    engine.objectContainer.objects.map { launch { it.obj.onFrame(frames) } }
        .joinAll()

    engine.clearRender()
    engine.objectContainer.objects.map { launch { it.obj.onRender() } }.joinAll()
}

class ObjectContainer {
    var objects = arrayListOf<GameObjectWithID>()

    fun add(id: String, obj: GameObject) {
        this.objects.add(GameObjectWithID(id, obj))
        obj.onInit()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : GameObject> get(id: String): T? {
        val f = this.objects.find { o -> o.id == id }?.obj

        return f as? T
    }

    data class GameObjectWithID(
        val id: String,
        val obj: GameObject
    )
}

data class Position(
    var x: Double = 0.0, var y: Double = 0.0
) {
    data class Long(var x: kotlin.Long = 0L, var y: kotlin.Long = 0L) {
        fun double(): Position {
            return Position(x.toDouble(), y.toDouble())
        }
    }
}

interface RenderHandler {
    suspend fun onRender() {}
}

interface FrameHandler {
    suspend fun onFrame(frames: Double) {}
}

interface InitHandler {
    fun onInit() {}
}

interface GameObject : RenderHandler, FrameHandler, InitHandler

data class Sprite(val imageSrc: String)

class Engine(val objectContainer: ObjectContainer, val keyboard: Keyboard) {
    suspend fun renderSprite(sprite: Sprite, pos: Position) {
        delay((1000.0 * Math.random()).toLong())
//        println(message = "render sprite $sprite $pos")
    }

    fun clearRender() {
//        println("clear render")
    }

    suspend fun renderText(name: String, pos: Position) {
        delay((1000L * Math.random()).toLong())
        println("render name $name $pos")
    }
}


class Keyboard {
    // FIXME: better union type? or other pattern?
    private val events: LinkedList<Any> = LinkedList()

    fun lastKeyPress(): KeyPressEvent? {
        return this.events.findLast { it is KeyPressEvent } as? KeyPressEvent
    }

    fun lastKeyUp(): KeyUpEvent? {
        return this.events.findLast { it is KeyUpEvent } as? KeyUpEvent
    }

    suspend fun accept() {
        val byte = System.`in`.read()
        this.events.add(KeyPressEvent(keyCode = byte.toChar()))
    }
}

data class KeyPressEvent(val keyCode: Char) {
    val type = KeyEventType.KeyPress
}

data class KeyUpEvent(val keyCode: Char) {
    val type = KeyEventType.KeyUp
}

enum class KeyEventType {
    KeyPress,
    KeyUp
}