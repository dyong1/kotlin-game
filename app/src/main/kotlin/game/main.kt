package game

import kotlinx.coroutines.*
import java.time.Instant

const val FPS = 60L
fun main() {
    var prev = Instant.now()
    val container = ObjectContainer()
    val engine = Engine(
        objectContainer = container
    )

    container.add(Player::class.java, Player(name= "player1"))
    container.add(Player::class.java, Player(name = "player2"))

    while (true) {
        val now = Instant.now()
        val elapsed = now.toEpochMilli() - prev.toEpochMilli()

        loop(engine, elapsed * FPS / 1000.0)

        prev = now
    }
}

fun loop(engine: Engine, frames: Double) = runBlocking {
    engine.objectContainer.objects.filterIsInstance<FrameHandler>().map { launch { it.onFrame(engine, frames) } }.joinAll()

    engine.clearRender()
    engine.objectContainer.objects.filterIsInstance<RenderHandler>().map { launch { it.onRender(engine) } }.joinAll()
}

class ObjectContainer {
    var objects = arrayListOf<Any>()
        private set

    fun <T> add(clazz: Class<T>, v: Any) {
        objects.add(v)
    }
}

class Player(val name: String) : RenderHandler, FrameHandler {
    val pos = Position()
    private val sprite = Sprite(imageSrc = "hello.jpg")

    fun move(deltaX: Double = 0.0, deltaY: Double = 0.0) {
        this.pos.x += deltaX
        this.pos.y += deltaY
    }

    override suspend fun onFrame(engine: Engine, frames: Double) {
        this.move(0.1 * frames)
    }

    override suspend fun onRender(engine: Engine):Job {
        val p = this
        return coroutineScope {
            launch {engine.renderText(p.name, p.pos)}
            launch {engine.renderSprite(p.sprite, p.pos)}
        }
    }
}

data class Position(
    var x: Double = 0.0, var y: Double = 0.0
)

interface RenderHandler {
    suspend fun onRender(engine: Engine): Job
}
interface FrameHandler {
    suspend fun onFrame(engine: Engine, frames: Double)
}

data class Sprite(val imageSrc: String)

class Engine(val objectContainer: ObjectContainer) {
    suspend fun renderSprite(sprite: Sprite, pos: Position) {
        delay((1000.0 * Math.random()).toLong())
        println("render sprite ${sprite} ${pos}")
    }

    fun clearRender() {
        println("clear render")
    }

    suspend fun renderText(name: String, pos: Position) {
        delay((1000L * Math.random()).toLong())
        println("render name ${name} ${pos}")
    }
}

