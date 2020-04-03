package com.sazilla.covid19.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.sazilla.covid19.JSONUrl
import com.sazilla.covid19.R
import com.sazilla.covid19.RemoteJsonProvider
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_main.*
import org.json.JSONException

abstract class BasicChartFragment : Fragment() {

    private var currentCategory: String
    private var currentField: Field
    private val categories = mutableListOf<String>()

    init {
        currentCategory = defaultCategory
        currentField = defaultField
    }

    protected abstract val defaultCategory: String
    protected abstract val categoryField: String
    protected abstract val defaultField: Field
    protected abstract val jsonUrl: JSONUrl
    protected abstract val menuResId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSpinner()
        initChart()
    }

    private fun initChart() {
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.axisMinimum = 0f
        chart.axisRight.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.description.isEnabled = false
        loadChartData(defaultField, defaultCategory)
    }

    @SuppressLint("CheckResult")
    private fun initSpinner() {
        if (defaultCategory.isNotEmpty() && categoryField.isNotEmpty()) {
            loadCategoriesData().subscribe({
                spinner.setSelection(categories.indexOf(defaultCategory))
                spinner.visibility = View.VISIBLE
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        loadChartData(currentField, categories[position])
                    }
                }
            }, { handleError() })
        }
    }

    @SuppressLint("CheckResult")
    private fun loadChartData(field: Field, category: String) {
        RemoteJsonProvider(requireContext()).retrieveJSON(jsonUrl)
            .doOnSubscribe {
                progressBar.visibility = View.VISIBLE
                chart.visibility = View.GONE
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val set = mutableListOf<BarEntry>()
                val datesMap = HashMap<Float, String>()
                try {
                    for (i in 0 until it.length()) {
                        val data = it.getJSONObject(i)
                        if (categoryField.isNotEmpty() && data.getString(categoryField) != category) {
                            continue
                        }
                        val count = data.getInt(field.fieldName)
                        set.add(BarEntry(set.size.toFloat(), count.toFloat()))
                        datesMap[set.size.toFloat()] = data.getString("data").getShortDate()
                    }
                } catch (e: JSONException) { }
                val dataSets = listOf(
                    BarDataSet(set, getString(field.displayNameResId))
                )
                setChartData(dataSets, datesMap)
                currentField = field
                currentCategory = category
            }, { handleError() })
    }

    @SuppressLint("CheckResult")
    private fun loadCategoriesData(): Maybe<List<String>> =
        RemoteJsonProvider(requireContext()).retrieveJSON(jsonUrl)
            .doOnSubscribe { progressBar.visibility = View.VISIBLE }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                try {
                    for (i in 0 until it.length()) {
                        val data = it.getJSONObject(i)
                        val category = data.optString(categoryField)
                        if (!categories.contains(category)) {
                            categories.add(category)
                        }
                    }
                } catch (e: JSONException) { }
                categories.sort()
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.adapter = adapter
                categories
            }

    @MainThread
    private fun setChartData(dataSets: List<BarDataSet>, datesMap: Map<Float, String>) {
        chart.xAxis.valueFormatter = DatesFormatter(datesMap)
        chart.data = BarData(dataSets)
        chart.notifyDataSetChanged()
        progressBar.visibility = View.GONE
        chart.visibility = View.VISIBLE
    }

    private fun handleError() {
        progressBar.visibility = View.GONE
        chart.visibility = View.GONE
        text_view_error.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        menuIdFieldMap[item.itemId]?.let { loadChartData(it, currentCategory); true }?: false

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(menuResId, menu)
    }

    private val menuIdFieldMap = mapOf(
        R.id.menuid_ricoverati_con_sintomi to Field.RicoveratiConSintomi,
        R.id.menuid_terapia_intensiva to Field.TerapiaIntensiva,
        R.id.menuid_isolamento_domiciliare to Field.IsolamentoDomiciliare,
        R.id.menuid_attuali_positivi to Field.AttualiPositivi,
        R.id.menuid_nuovi_positivi to Field.NuoviPositivi,
        R.id.menuid_delta_positivi to Field.DeltaPositivi,
        R.id.menuid_dimessi_guariti to Field.DimessiGuariti,
        R.id.menuid_tamponi to Field.Tamponi,
        R.id.menuid_decessi to Field.Decessi,
        R.id.menuid_totale_casi to Field.TotaleCasi
    )
}

enum class Field(val fieldName: String, val displayNameResId: Int) {
    RicoveratiConSintomi("ricoverati_con_sintomi", R.string.menu_ricoverati_con_sintomi),
    TerapiaIntensiva("terapia_intensiva", R.string.menu_terapia_intensiva),
    IsolamentoDomiciliare("isolamento_domiciliare", R.string.menu_isolamento_domiciliare),
    AttualiPositivi("totale_positivi", R.string.menu_attuali_positivi),
    NuoviPositivi("nuovi_positivi", R.string.menu_nuovi_positivi),
    DeltaPositivi("variazione_totale_positivi", R.string.menu_delta_positivi),
    DimessiGuariti("dimessi_guariti", R.string.menu_dimessi_guariti),
    Tamponi("tamponi", R.string.menu_tamponi),
    Decessi("deceduti", R.string.menu_decessi),
    TotaleCasi("totale_casi", R.string.menu_totale_casi)
}

class DatesFormatter(private val datesMap: Map<Float, String>): ValueFormatter() {
    override fun getFormattedValue(value: Float) = datesMap[value] ?: ""
}

internal fun String.getShortDate() = substring(5, 10)
