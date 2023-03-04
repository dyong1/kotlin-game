package game

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


object snakeGame {
    internal val game = Game()
    fun run() {
        this.game.objectContainer.add("player", Player())
        this.game.run()
    }
}

class Player() : GameObject {
    private val pos = Position()
    private val sprite = Sprite(imageSrc = "hello.jpg")

    override suspend fun onFrame(frames: Double) {
        this.move(0.1 * frames)
    }

    override suspend fun onRender() {
        val p = this
        return coroutineScope {
            launch { snakeGame.game.engine.renderText("snake", p.pos) }
            launch { snakeGame.game.engine.renderSprite(p.sprite, p.pos) }
        }
    }

    private fun move(deltaX: Double = 0.0, deltaY: Double = 0.0) {
        this.pos.x += deltaX
        this.pos.y += deltaY
    }
}