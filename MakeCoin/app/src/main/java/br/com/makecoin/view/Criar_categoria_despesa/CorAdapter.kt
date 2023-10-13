package br.com.makecoin.view.Criar_categoria_despesa

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import br.com.makecoin.R

class CorAdapter(private val context: Context, private val cores: List<Int>) : BaseAdapter() {

    override fun getCount(): Int = cores.size

    override fun getItem(position: Int): Int = cores[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val corView: View
        if (convertView == null) {
            corView = LayoutInflater.from(context).inflate(R.layout.cor_item, parent, false)
        } else {
            corView = convertView
        }

        val cor = cores[position]
        corView.findViewById<View>(R.id.cor_view).setBackgroundColor(cor)

        return corView
    }
}
