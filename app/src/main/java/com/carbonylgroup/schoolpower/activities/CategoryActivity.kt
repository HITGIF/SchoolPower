package com.carbonylgroup.schoolpower.activities

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.data.CategoryWeightData
import com.carbonylgroup.schoolpower.data.Subject
import com.carbonylgroup.schoolpower.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.content_category.*
import java.util.*

class CategoryActivity : BaseActivity() {

    private lateinit var categoriesWeights: CategoryWeightData
    private lateinit var utils : Utils

    override fun initActivity() {
        super.initActivity()
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)

        utils = Utils(this)
        categoriesWeights = CategoryWeightData(utils)

        val subject = intent.getSerializableExtra("subject") as Subject
        initChart(subject)
    }

    private fun initChart(subject: Subject) {

        val name = subject.getLatestTermName(utils)
        val grade = subject.grades[name]!!

        if(name!=null) {
            supportActionBar!!.title = "$name %.2f%%".format(
                    grade.calculatedGrade.getEstimatedPercentageGrade()*100)
        }

        var text = ""
        for ((cname, cate) in grade.calculatedGrade.categories){
            text += ("\n$cname ${cate.score}/${cate.maxScore} (%.2f%%) weight ${cate.weight} " +
                    "Contributing a loss of %.2f%%")
                    .format(cate.getPercentage()*100, cate.weight*(1-cate.getPercentage()))
        }
        cates.text = text

        val categories = grade.calculatedGrade.categories
        val weightLabel = arrayOfNulls<TextView>(categories.size)
        val weightEdit = arrayOfNulls<EditText>(categories.size)

        for ((cname, cate) in grade.calculatedGrade.categories){
            val label = TextView(this)
            val edit = EditText(this)

            label.text = cname
            edit.setText(cate.weight.toString())
            edit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    categoriesWeights.setWeight(cname, subject, s.toString().toDoubleOrNull()?:return)
                    categoriesWeights.flush()
                    subject.recalculateGrades(categoriesWeights)
                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {}
            })

            categoryContainer.addView(label)
            categoryContainer.addView(edit)

        }

        val entries = ArrayList<PieEntry>()
        val percentages = ArrayList<android.util.Pair<Float, Float>>()
        val colors = ArrayList<Int>()
        var count = 0
        for ((cname, cate) in grade.calculatedGrade.categories){
            entries.add(PieEntry(cate.weight.toFloat(), cname))
            percentages.add(android.util.Pair(cate.score.toFloat(), cate.maxScore.toFloat()))
            colors.add(Color.parseColor(Utils.chartColorList[count++]))
        }
        setRadarPieChartData(entries, percentages, colors)

        val entries2 = ArrayList<PieEntry>()
        for ((cname, cate) in grade.calculatedGrade.categories){
            entries2.add(PieEntry((cate.weight * (1.0f - cate.getPercentage())).toFloat(), cname))
        }
        setPieChartData(entries2, colors)
    }

    private fun setRadarPieChartData(entries: ArrayList<PieEntry>,
                        percentages: ArrayList<android.util.Pair<Float, Float>>,
                        colors: ArrayList<Int>) {

        val primaryColor = utils.getPrimaryTextColor()
        pieRadarChart.rotationAngle = 0f
        pieRadarChart.isRotationEnabled = true
        pieRadarChart.isDrawHoleEnabled = false
        pieRadarChart.description.isEnabled = false
        pieRadarChart.transparentCircleRadius = 61f
        pieRadarChart.isHighlightPerTapEnabled = false
        pieRadarChart.dragDecelerationFrictionCoef = 0.95f
        pieRadarChart.setUsePercentValues(true)
        pieRadarChart.setEntryLabelTextSize(11f)
        pieRadarChart.setTransparentCircleAlpha(110)
        pieRadarChart.setEntryLabelColor(primaryColor)
        pieRadarChart.setEntryInnerLabelColor(Color.WHITE)
        pieRadarChart.setTransparentCircleColor(Color.WHITE)
        pieRadarChart.setExtraOffsets(15f, 0f, 15f, 0f)

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueLinePart1Length = .4f
        dataSet.valueLinePart2Length = .7f
        dataSet.valueLineColor = primaryColor
        dataSet.valueTextColor = primaryColor
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        pieRadarChart.data = data
        pieRadarChart.legend.isEnabled = false
        pieRadarChart.invalidate()
        pieRadarChart.highlightValues(null)
        pieRadarChart.setDrawRadiiPairs(percentages)
        pieRadarChart.animateY(1200, Easing.EasingOption.EaseInOutCubic)
    }


    private fun setPieChartData(entries: ArrayList<PieEntry>,
                                     colors: ArrayList<Int>) {

        val primaryColor = utils.getPrimaryTextColor()
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isDrawHoleEnabled = false
        pieChart.description.isEnabled = false
        pieChart.transparentCircleRadius = 61f
        pieChart.isHighlightPerTapEnabled = false
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(11f)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.setEntryLabelColor(primaryColor)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setExtraOffsets(5f, 5f, 5f, 5f)

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueLinePart1Length = .4f
        dataSet.valueLinePart2Length = .7f
        dataSet.valueLineColor = primaryColor
        dataSet.valueTextColor = primaryColor
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        pieChart.data = data
        pieChart.legend.isEnabled = false
        pieChart.invalidate()
        pieChart.highlightValues(null)
        pieChart.animateY(1200, Easing.EasingOption.EaseInOutCubic)
    }
}
