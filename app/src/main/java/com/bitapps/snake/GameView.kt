package com.bitapps.snake

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import java.util.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val CELL_SIZE = 70
    private val CELL_COUNT = 10
    private val FRAME_TIME = 250L
    private val EYE_RADIUS = 8
    private val EYE_SHIFT = 14

    private val gridPaint = Paint().apply {
        this.color = Color.BLUE
    }
    private val snakePaint = Paint().apply {
        this.color = Color.BLUE
    }
    private val foodPaint = Paint().apply {
        this.color = Color.YELLOW
    }
    private val eyePaint = Paint().apply {
        this.color = Color.GREEN
    }

    private var startTime = 0L

    private val handler = Handler(Looper.getMainLooper())

    private val snake = arrayListOf<Point>()
    var direction = KeyEvent.KEYCODE_DPAD_RIGHT
        set(value) {
            if (collidesOriginalDirection(value).not()) {
                when (direction) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (value != KeyEvent.KEYCODE_DPAD_RIGHT) {
                            field = value
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        if (value != KeyEvent.KEYCODE_DPAD_DOWN) {
                            field = value
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (value != KeyEvent.KEYCODE_DPAD_LEFT) {
                            field = value
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if (value != KeyEvent.KEYCODE_DPAD_UP) {
                            field = value
                        }
                    }
                }
            }
        }
    private var originalDirection = KeyEvent.KEYCODE_DPAD_RIGHT

    var listener: GameListener? = null
    private var isFail = false

    private val food = Point(-1, -1)
    private val freeCells = arrayListOf<Int>()
    private val random = Random()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawSnake(canvas)
        drawFood(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        for (i in 0..CELL_COUNT) {
            canvas.drawLine(
                0f, i.toFloat() * CELL_SIZE,
                CELL_COUNT * CELL_SIZE.toFloat(), i.toFloat() * CELL_SIZE,
                gridPaint
            )
            canvas.drawLine(
                i.toFloat() * CELL_SIZE, 0f,
                i.toFloat() * CELL_SIZE, CELL_COUNT * CELL_SIZE.toFloat(),
                gridPaint
            )
        }
    }

    private fun drawSnake(canvas: Canvas) {
        for (part in snake) {
            canvas.drawRect(
                part.x.toFloat() * CELL_SIZE,
                part.y * CELL_SIZE.toFloat(),
                (part.x.toFloat() * CELL_SIZE) + CELL_SIZE,
                (part.y.toFloat() * CELL_SIZE) + CELL_SIZE,
                snakePaint
            )
        }
        if (snake.isNotEmpty()) {
            val cx = snake[0].x.toFloat() * CELL_SIZE + (CELL_SIZE / 2)
            val cy = snake[0].y.toFloat() * CELL_SIZE + (CELL_SIZE / 2)
            val isVertical = originalDirection == KeyEvent.KEYCODE_DPAD_UP ||
                    originalDirection == KeyEvent.KEYCODE_DPAD_DOWN
            if (isVertical) {
                canvas.drawCircle(cx - EYE_SHIFT, cy, EYE_RADIUS.toFloat(), eyePaint)
                canvas.drawCircle(cx + EYE_SHIFT, cy, EYE_RADIUS.toFloat(), eyePaint)
            } else {
                canvas.drawCircle(cx, cy - EYE_SHIFT, EYE_RADIUS.toFloat(), eyePaint)
                canvas.drawCircle(cx, cy + EYE_SHIFT, EYE_RADIUS.toFloat(), eyePaint)
            }
        }
    }

    private fun drawFood(canvas: Canvas) {
        canvas.drawRect(
            food.x.toFloat() * CELL_SIZE + 1,
            food.y * CELL_SIZE.toFloat() + 1,
            (food.x.toFloat() * CELL_SIZE) + CELL_SIZE,
            (food.y.toFloat() * CELL_SIZE) + CELL_SIZE,
            foodPaint
        )
    }

    fun startGame() {
        startTime = System.currentTimeMillis()
        runFrames(isFirstFrame = true)
    }

    private fun runFrames(isFirstFrame: Boolean = false) {
        handler.post {
            if (isFail) {
                isFail = false
                return@post
            }
            if (isFirstFrame) {
                direction = KeyEvent.KEYCODE_DPAD_RIGHT
                originalDirection = KeyEvent.KEYCODE_DPAD_RIGHT
                snake.clear()
                snake.add(Point(4, 0))
                snake.add(Point(3, 0))
                snake.add(Point(2, 0))
                snake.add(Point(1, 0))
                snake.add(Point(0, 0))
                repeat(CELL_COUNT * CELL_COUNT, freeCells::add)
                freeCells.remove(0)
                freeCells.remove(1)
                freeCells.remove(2)
                freeCells.remove(3)
                freeCells.remove(4)
                spawnFood()
            } else {
                moveSnake()
            }
            if (!isFail) invalidate()
            handler.postDelayed({ runFrames() }, FRAME_TIME)
        }
    }

    private fun moveSnake() {
        originalDirection = direction

        val oldSnake = arrayListOf<Point>()
        snake.forEach { point ->
            oldSnake.add(Point(point.x, point.y))
        }

        when (direction) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                snake[0].x -= 1
                if (snake[0].x == -1) {
                    snake[0].x = CELL_COUNT - 1
                }
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                snake[0].y -= 1
                if (snake[0].y == -1) {
                    snake[0].y = CELL_COUNT - 1
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                snake[0].x += 1
                if (snake[0].x == CELL_COUNT) {
                    snake[0].x = 0
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                snake[0].y += 1
                if (snake[0].y == CELL_COUNT) {
                    snake[0].y = 0
                }
            }
        }

        if (snake.count() > 1) {
            for (i in 1..< snake.count()) {
                snake[i].x = oldSnake[i - 1].x
                snake[i].y = oldSnake[i - 1].y
            }
            for (i in 1..< snake.count()) {
                if (snake[0].x == snake[i].x && snake[0].y == snake[i].y) {
                    failGame(oldSnake)
                    return
                }
            }
        }

        if (snake[0].x == food.x && snake[0].y == food.y) {
            eatFood(oldSnake)
        }
    }

    private fun failGame(oldSnake: List<Point>) {
        snake.clear()
        snake.addAll(oldSnake)
        direction = KeyEvent.KEYCODE_DPAD_RIGHT
        originalDirection = KeyEvent.KEYCODE_DPAD_RIGHT
        food.x = -1
        food.y = -1
        isFail = true
        listener?.onFail()
    }

    private fun eatFood(oldSnake: List<Point>) {
        snake.add(Point(oldSnake.last().x, oldSnake.last().y))
        freeCells.clear()
        repeat(CELL_COUNT * CELL_COUNT, freeCells::add)
        snake.forEach { part ->
            freeCells.remove((part.y * CELL_COUNT) + part.x)
        }
        spawnFood()
    }

    private fun collidesOriginalDirection(direction: Int): Boolean {
        return (direction == KeyEvent.KEYCODE_DPAD_LEFT &&
                        originalDirection == KeyEvent.KEYCODE_DPAD_RIGHT) ||
                (direction == KeyEvent.KEYCODE_DPAD_UP &&
                        originalDirection == KeyEvent.KEYCODE_DPAD_DOWN) ||
                (direction == KeyEvent.KEYCODE_DPAD_RIGHT &&
                        originalDirection == KeyEvent.KEYCODE_DPAD_LEFT) ||
                (direction == KeyEvent.KEYCODE_DPAD_DOWN &&
                        originalDirection == KeyEvent.KEYCODE_DPAD_UP)
    }

    private fun spawnFood() {
        val freeCellIndex = random.nextInt(freeCells.count())
        val foodPosition = freeCells[freeCellIndex]
        food.x = foodPosition % CELL_COUNT
        food.y = foodPosition / CELL_COUNT
    }

    fun finish() {
        listener = null
    }
}