package com.sazilla.covid19.fragment

import com.sazilla.covid19.JSONUrl
import com.sazilla.covid19.R

class NationalChartFragment: BasicChartFragment() {

    override val defaultCategory: String
        get() = ""

    override val categoryField: String
        get() = ""

    override val defaultField: Field
        get() = Field.TotaleCasi

    override val jsonUrl: JSONUrl
        get() = JSONUrl.National

    override val menuResId: Int
        get() = R.menu.menu_national
}
