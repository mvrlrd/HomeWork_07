package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class MyDiagrammView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val values = ArrayList<Expense>()

    private val paddingParameter = 10f
    private val widthOfCycleGraph = 100f

    private var callback: ((Expense) -> Unit)? = null

    private lateinit var paintBackground : Paint
    private lateinit var paintMyRed: Paint
    private lateinit var paintMyOrange: Paint
    private lateinit var paintMySand: Paint
    private lateinit var paintMyPeach: Paint
    private lateinit var paintMyLemon: Paint
    private lateinit var paintMyLime: Paint
    private lateinit var paintMyWave: Paint
    private lateinit var paintMyOcean: Paint
    private lateinit var paintMyNight: Paint
    private lateinit var paintMyDeep: Paint

    private lateinit var paintTextMain: Paint
    private lateinit var paintTextNameOfCategory: Paint
    private lateinit var paintTextAmount: Paint

    private lateinit var paintStr: Paint

    private val listOfPaints = mutableListOf<Paint>()

    private var onePercent: Float = 0.0f

    private var clickedPointX: Float = 0f
    private var clickedPointY: Float = 0f

    private var myPaddingWith: Float = 0f
    private var chosenPiece: Expense? = null

    private var sum = 0f





    private val gestureDetector = GestureDetector(context, object :SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            e?.let {
                clickedPointX = e.x
                clickedPointY = e.y
                val hCenter = height/2
                val wCenter = width/2

                val clickedXRelatevelyTheCenter = clickedPointX - wCenter
                val clickedYRelatevelyTheCenter = clickedPointY - hCenter
                val distanceToCenter =
                    sqrt(clickedXRelatevelyTheCenter.pow(2) + clickedYRelatevelyTheCenter.pow(2))
                val graphRadius = wCenter - myPaddingWith

                var angleToCenterRad = atan2(clickedYRelatevelyTheCenter, clickedXRelatevelyTheCenter)

                if (angleToCenterRad < 0) {
                    val angleToCenterG = (180 + angleToCenterRad * 180 / PI).toFloat()
                    angleToCenterRad = (((180 + angleToCenterG) / 180) * PI).toFloat()
                }

                var angleStart = 0f
                for (i in 0 .. values.lastIndex){
                    val angleRad: Float = (values[i].amount/onePercent)*3.6f*PI.toFloat()/180
                    val angleEnd = angleStart+angleRad

                    if ((angleToCenterRad>=angleStart)&&(angleToCenterRad<angleEnd)){
                        val innerCycleRadius = (width - 2*myPaddingWith - 2*widthOfCycleGraph) / 2



                        if (checkIfTouchedInPieceOfGraph(
                                distanceToCenter,
                                graphRadius,
                                innerCycleRadius,
                            )
                        ){

                            Log.i(TAG, "was chosen : ${values[i].name}")
                            callback?.invoke(values[i])
                            chosenPiece = values[i]
                            invalidate()
                            return true
                        }
                    }
                    angleStart=angleEnd
                }
                chosenPiece = null
            }
            invalidate()
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return super.onDoubleTap(e)
        }
    })


    init {
        if (isInEditMode) {
//            setValues(listOf(4f, 2f, 1f, 5f, 0f, 2f))
        }
        setup(
            context
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        println("$TAG wMode= ${MeasureSpec.toString(wMode)}  hMode=${MeasureSpec.toString(hMode)}  w=$wSize h=$hSize")
        when (hMode) {

            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }
            MeasureSpec.AT_MOST -> {
                val newH = wSize.coerceAtMost(hSize)
                val newW =  wSize.coerceAtMost(hSize)
                setMeasuredDimension(newW, newH)
            }
            MeasureSpec.UNSPECIFIED -> {
                val barWidth = 1f
                setMeasuredDimension((values.size * barWidth).toInt(), hSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRGB(255, 255, 255)

        if (values.isEmpty()) return

        val worldWidth = width.toFloat()
        val worldHeight = height.toFloat()
        val hCenter = worldHeight / 2
        val wCenter = worldWidth / 2

        val paddingWidth: Float
        val paddingHeight: Float


        if (hCenter < wCenter) {
            paddingHeight = worldHeight / paddingParameter
            paddingWidth = wCenter - hCenter + paddingHeight
        } else {
            paddingWidth = worldWidth / paddingParameter
            paddingHeight = hCenter - wCenter + paddingWidth
        }
        myPaddingWith = paddingWidth

        var startAngleG = 0f
        var paintIndex = 0
        var paint = listOfPaints[paintIndex]


        var chosenStartAngle = 0f
        var chosenEAngle = 0f
        var chosenPaint : Paint? = null

        for (i in 0..values.lastIndex) {
            val endAngleG = (values[i].amount / onePercent) * 3.6f
            chosenPiece?.let {
                if (values[i] == it) {
                    Log.i(TAG,"it is ____${chosenPiece!!.name}")
                    chosenPaint = paint
                    chosenStartAngle = startAngleG - 10f
                    chosenEAngle = endAngleG + 20f
                }
            }
            drawPieceOfGraph(
                canvas,
                paddingWidth,
                paddingHeight,
                worldWidth,
                worldHeight,
                startAngleG,
                endAngleG,
                paint
            )

            startAngleG += endAngleG
            if (paintIndex == listOfPaints.lastIndex) {
                paintIndex = 0
            } else {
                paintIndex++
            }
            paint = listOfPaints[paintIndex]
        }

            drawInnerBackgroundCircles(
                canvas,
                paddingWidth,
                paddingHeight,
                worldWidth,
                worldHeight
            )

        chosenPiece?.let {
            drawChosenPiece(
                chosenPaint,
                canvas,
                chosenStartAngle,
                chosenEAngle,
                paddingWidth,
                paddingHeight
            )
        }

        //HEADER SUM ______
        val diameter = worldWidth - paddingWidth - widthOfCycleGraph -paddingWidth - widthOfCycleGraph  //
        val r = diameter/2
        val hOfHeader =  height/2 - r/3

        val spaceForHeader = sqrt(3.0f)*r
        //   __________________
        //   |                 |
        //   |____headerText___|
        //   |                 |
        //   |_________________|

        val headerSum = "$sum ₽"
        var textWidth = paintTextMain.measureText(headerSum)
        Log.e(TAG,"sapcing    ${ paintTextMain.letterSpacing }")
        while (textWidth>0.6f*spaceForHeader){
            paintTextMain.textSize = paintTextMain.textSize - 1f
            textWidth = paintTextMain.measureText(headerSum)
        }
        val startOfHeader = width/2f-textWidth/2

        canvas.drawText(headerSum,startOfHeader, hOfHeader,paintTextMain)
//______________________________________

        }

    private fun checkIfTouchedInPieceOfGraph(
        distanceToCenter: Float,
        graphRadius: Float,
        innerCycleRadius: Float,
    ): Boolean {
        return (distanceToCenter <= graphRadius) && (distanceToCenter >= innerCycleRadius)
    }

    private fun drawPieceOfGraph(
        canvas: Canvas,
        paddingWidth: Float,
        paddingHeight: Float,
        worldWidth: Float,
        worldHeight: Float,
        startAngleG: Float,
        endAngleG: Float,
        paint: Paint
    ) {
        canvas.drawArc(
            paddingWidth,
            paddingHeight,
            worldWidth - paddingWidth,
            worldHeight - paddingHeight,
            startAngleG,
            endAngleG,
            true,
            paint
        )
        canvas.drawArc(
            paddingWidth,
            paddingHeight,
            worldWidth - paddingWidth,
            worldHeight - paddingHeight,
            startAngleG,
            endAngleG,
            true,
            paintStr
        )
    }

    private fun drawInnerBackgroundCircles(
        canvas: Canvas,
        paddingWidth: Float,
        paddingHeight: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        canvas.drawOval(
            paddingWidth + widthOfCycleGraph,
            paddingHeight + widthOfCycleGraph,
            worldWidth - paddingWidth - widthOfCycleGraph,
            worldHeight - paddingHeight - widthOfCycleGraph,
            paintBackground
        )
        canvas.drawOval(
            paddingWidth + widthOfCycleGraph,
            paddingHeight + widthOfCycleGraph,
            worldWidth - paddingWidth - widthOfCycleGraph,
            worldHeight - paddingHeight - widthOfCycleGraph,
            paintStr
        )





    }


    private fun drawChosenPiece(
        chosenPaint: Paint?,
        canvas: Canvas,
        chosenStartAngle: Float,
        chosenEAngle: Float,
        paddingWidth: Float,
        paddingHeight: Float
    ) {
       val chosenLeft = paddingWidth - widthOfCycleGraph/5
       val chosenTop = paddingHeight - widthOfCycleGraph/5
       val chosenRight = width - paddingWidth + widthOfCycleGraph/5
       val chosenBottom = height - paddingHeight + widthOfCycleGraph/5

        chosenPaint?.let {
            canvas.drawArc(
                chosenLeft,
                chosenTop,
                chosenRight,
                chosenBottom,
                chosenStartAngle,
                chosenEAngle,
                true,
                chosenPaint
            )
            canvas.drawArc(
                chosenLeft,
                chosenTop,
                chosenRight,
                chosenBottom,
                chosenStartAngle,
                chosenEAngle,
                true,
                paintStr
            )

            val koef =  widthOfCycleGraph/5+widthOfCycleGraph+widthOfCycleGraph/5
            canvas.drawArc(
                chosenLeft + koef,
                chosenTop + koef,
                chosenRight - koef,
                chosenBottom - koef,
                chosenStartAngle,
                chosenEAngle,
                true,
                paintStr
            )

            canvas.drawOval(
                chosenLeft + koef,
                chosenTop + koef,
                chosenRight - koef,
                chosenBottom - koef,
                paintBackground
            )
        }
        chosenPiece?.let {
            val koef =  widthOfCycleGraph/5+widthOfCycleGraph+widthOfCycleGraph/5
            //нужно правильно управлять размером текста
            val diameter = chosenRight - koef - chosenLeft - koef //
            val r = diameter/2
            val hOfName =  height/2f + r/12

            val spaceForHeader = sqrt(3.0f)*r
            //   __________________
            //   |                 |
            //   |____headerText___|
            //   |      name       |
            //   |     amount      |
            //   |_________________|




            val textName = it.name
            paintTextNameOfCategory.textSize = 30f
            var textNameWidth = paintTextNameOfCategory.measureText(textName)



            while (textNameWidth>0.9f*diameter) {
//                Log.i(TAG, "di = $diameter, tW= $textNameWidth")
                paintTextNameOfCategory.textSize = paintTextNameOfCategory.textSize - 1f
                    textNameWidth = paintTextNameOfCategory.measureText(textName)
            }
//            while (textNameWidth<0.5f*diameter) {
////                Log.i(TAG, "di = $diameter, tW= $textNameWidth")
//                paintTextNameOfCategory.textSize = paintTextNameOfCategory.textSize + 1f
//                textNameWidth = paintTextNameOfCategory.measureText(textName)
//            }
            Log.i(TAG, "di = $diameter, tW= $textNameWidth    textSize=${paintTextNameOfCategory.textSize}")

            val startOfName = width/2f-textNameWidth/2f
            canvas.drawText(textName,startOfName, hOfName,paintTextNameOfCategory)


            val textAmount = "${it.amount} ₽"
            var textAmountWidth = paintTextAmount.measureText(textAmount)
            while (textAmountWidth>0.5f*diameter){
                paintTextAmount.textSize = paintTextAmount.textSize - 1f
                textAmountWidth = paintTextAmount.measureText(textAmount)
            }

            val hOfAmount = hOfName + (2* paintTextAmount.textSize)

            val startOfAmount = width/2f-textAmountWidth/2f
            canvas.drawText(textAmount,startOfAmount, hOfAmount,paintTextAmount)
        }

    }


    fun setValues(values : List<Expense>, _callback: (Expense)->Unit) {
        this.values.clear()
        this.values.addAll(values)

        this.callback = _callback

        var hundredPercent = 0f
        values.forEach { hundredPercent+=it.amount
            sum+=it.amount }
        onePercent = hundredPercent/100
        requestLayout()
        invalidate()
    }

    private fun setup(
        context: Context
    ) {
        paintBackground = Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
        }
        paintMyRed=Paint().apply {
            color = context.getColor(R.color.my_red)
            style = Paint.Style.FILL
        }
        paintMyOrange=Paint().apply {
            color = context.getColor(R.color.my_orange)
            style = Paint.Style.FILL
        }
        paintMySand=Paint().apply {
            color = context.getColor(R.color.my_sand)
            style = Paint.Style.FILL
        }
        paintMyPeach=Paint().apply {
            color = context.getColor(R.color.my_peach)
            style = Paint.Style.FILL
        }
        paintMyLemon=Paint().apply {
            color = context.getColor(R.color.my_lemon)
            style = Paint.Style.FILL
        }
        paintMyLime=Paint().apply {
            color = context.getColor(R.color.my_lime)
            style = Paint.Style.FILL
        }
        paintMyWave=Paint().apply {
            color = context.getColor(R.color.my_wave)
            style = Paint.Style.FILL
        }
        paintMyOcean=Paint().apply {
            color = context.getColor(R.color.my_ocean)
            style = Paint.Style.FILL
        }
        paintMyNight=Paint().apply {
            color = context.getColor(R.color.my_night)
            style = Paint.Style.FILL
        }
        paintMyDeep=Paint().apply {
            color = context.getColor(R.color.my_deep)
            style = Paint.Style.FILL
        }
        paintStr=Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        paintTextMain = Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.STROKE
            textSize = 88f
        }

        paintTextAmount = Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.FILL
            textSize = 30f
        }
        paintTextNameOfCategory = Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.FILL
            textSize = 30f
        }

        listOfPaints.run {
            add(paintMyLime)
            add(paintMyRed)
            add(paintMySand)
            add(paintMyWave)
            add(paintMyOrange)
            add(paintMyOcean)
            add(paintMyPeach)
            add(paintMyLemon)
            add(paintMyNight)
            add(paintMyDeep)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    /**
     * Восстановление данных из [AnalyticalPieChartState]
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        val analyticalPieChartState = state as? AnalyticalPieChartState
        super.onRestoreInstanceState(analyticalPieChartState?.superState ?: state)

        chosenPiece = analyticalPieChartState?.dataList
    }

    /**
     * Сохранение [dataList] в собственный [AnalyticalPieChartState]
     */
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return AnalyticalPieChartState(superState, chosenPiece)
    }
}

/**
 * Собственный state для сохранения и восстановления данных
 */
class AnalyticalPieChartState(
    private val superSavedState: Parcelable?,
    val dataList: Expense?
) : View.BaseSavedState(superSavedState), Parcelable {
}