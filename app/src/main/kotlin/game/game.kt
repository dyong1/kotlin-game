package game

import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant

const val FPS = 60L

class Game {
    var objectContainer = ObjectContainer()
    var engine = Engine(this.objectContainer)


    fun run() {
        var prev = Instant.now()

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
)

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

class Engine(val objectContainer: ObjectContainer) {
    suspend fun renderSprite(sprite: Sprite, pos: Position) {
        delay((1000.0 * Math.random()).toLong())
        println(message = "render sprite $sprite $pos")
    }

    fun clearRender() {
        println("clear render")
    }

    suspend fun renderText(name: String, pos: Position) {
        delay((1000L * Math.random()).toLong())
        println("render name $name $pos")
    }
}

