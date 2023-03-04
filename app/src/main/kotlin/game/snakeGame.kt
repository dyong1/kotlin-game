package game

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


object SnakeGame {
    val game = Game()
    fun run() {
        this.game.objectContainer.add("player", Player())
        this.game.run()
    }
}

fun game(): Game {
    return SnakeGame.game
}

class Player : GameObject {
    private val pos = Position.Long(0, 0)
    private val sprite = Sprite(imageSrc = "hello.jpg")

    override suspend fun onFrame(frames: Double) {
        val key = game().keyboard.lastKeyPress()
        when (key?.keyCode) {
            'h' -> this.move(deltaX = -1)
            'j' -> this.move(deltaY = 1)
            'k' -> this.move(deltaY = -1)
            'l' -> this.move(deltaX = 1)
        }
    }

    override suspend fun onRender() {
        val p = this
        return coroutineScope {
            launch { game().engine.renderText("snake", p.pos.double()) }
            launch { game().engine.renderSprite(p.sprite, p.pos.double()) }
        }
    }

    private fun move(deltaX: Long = 0L, deltaY: Long = 0L) {
        this.pos.x += deltaX
        this.pos.y += deltaY
    }
}