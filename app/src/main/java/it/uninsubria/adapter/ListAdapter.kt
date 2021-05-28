package it.uninsubria.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import it.uninsubria.talks.R

class ListAdapter(private val context: Activity, private val nickname: MutableList<String>, private val content: MutableList<String>) : ArrayAdapter<String>(context, R.layout.row_talk, nickname) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.row_talk, null, true)

        val titleText = rowView.findViewById(R.id.TV_nickname) as TextView
        val subtitleText = rowView.findViewById(R.id.TV_content) as TextView

        titleText.text = nickname[position]
        subtitleText.text = content[position]

        return rowView
    }
}