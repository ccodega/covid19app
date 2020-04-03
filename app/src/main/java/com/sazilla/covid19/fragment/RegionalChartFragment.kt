package com.sazilla.covid19.fragment

import com.sazilla.covid19.JSONUrl
import com.sazilla.covid19.R

class RegionalChartFragment: BasicChartFragment() {

    override val defaultCategory: String
        get() = "Lombardia"

    override val categoryField: String
        get() = "denominazione_regione"

    override val defaultField: Field
        get() = Field.TotaleCasi

    override val jsonUrl: JSONUrl
        get() = JSONUrl.Regional

    override val menuResId: Int
        get() = R.menu.menu_regioni
}
