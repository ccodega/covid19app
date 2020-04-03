package com.sazilla.covid19.fragment

import com.sazilla.covid19.JSONUrl
import com.sazilla.covid19.R

class ProvincialChartFragment: BasicChartFragment() {

    override val defaultCategory: String
        get() = "Lecco"

    override val categoryField: String
        get() = "denominazione_provincia"

    override val defaultField: Field
        get() = Field.TotaleCasi

    override val jsonUrl: JSONUrl
        get() = JSONUrl.Provincial

    override val menuResId: Int
        get() = R.menu.menu_province
}
