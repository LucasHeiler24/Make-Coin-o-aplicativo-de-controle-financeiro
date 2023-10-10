package br.com.makecoin.view.TelaPrincipalGraficos

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Random

class TelaPrincipalGraficos : AppCompatActivity() {
    private var valorDespesas: Double = 0.0
    private var valorReceitas: Double = 0.0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mesSelecionadoTextView: TextView // Adicionar esta linha
    private val mesesAbreviados = arrayOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )
    private var mesSelecionado = 0
    private var anoSelecionado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal_graficos)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNameGrafico = "PrefsGrafico_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNameGrafico, MODE_PRIVATE)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Defina os ícones para os itens do menu programaticamente
        bottomNavigationView.menu.findItem(R.id.navigation_item1).setIcon(R.drawable.navegarcasa)
        bottomNavigationView.menu.findItem(R.id.navigation_item2).setIcon(R.drawable.mais)
        bottomNavigationView.menu.findItem(R.id.navigation_item3).setIcon(R.drawable.estatistico)

        bottomNavigationView.setSelectedItemId(R.id.navigation_item3)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.navigation_item1 -> {
                    // Abrir a Tela 1
                    val intent1 = Intent(this, TelaPrincipal::class.java)
                    startActivity(intent1)
                    true
                }

                R.id.navigation_item2 -> {
                    // Abrir a Tela 2
                    val intent2 = Intent(this, TelaPrincipalCategorias::class.java)
                    startActivity(intent2)
                    true
                }

                R.id.navigation_item3 -> {
                    // Abrir a Tela 3
                    val intent3 = Intent(this, TelaPrincipalGraficos::class.java)
                    startActivity(intent3)
                    true
                }

                else -> false
            }
        }
        mesSelecionadoTextView = findViewById(R.id.mesSelecionadoTextView)
        // Obter o mês e o ano atuais
        val calendar = Calendar.getInstance()
        val anoAtual = calendar.get(Calendar.YEAR)
        val mesAtual = calendar.get(Calendar.MONTH)
        // Verificar se o valor do mês foi salvo nas preferências compartilhadas
        if (sharedPreferences.contains("mesSelecionado")) {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se estiver salvo, carregar o valor e atualizar a TextView
            mesSelecionado = sharedPreferences.getInt("mesSelecionado", 0)
        } else {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se não estiver salvo, definir o mês atual e salvar nas preferências
            mesSelecionado = mesAtual

            // Salvar o valor do mês nas preferências compartilhadas
            val editor = sharedPreferences.edit()
            editor.putInt("mesSelecionado", mesSelecionado)
            editor.apply()
        }

        // Verificar se o valor do ano foi salvo nas preferências compartilhadas
        if (sharedPreferences.contains("anoSelecionado")) {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se estiver salvo, carregar o valor
            anoSelecionado = sharedPreferences.getInt("anoSelecionado", anoAtual)
        } else {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNamePrincipal = "PrefsPrincipal_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNamePrincipal, MODE_PRIVATE)

            // Se não estiver salvo, definir o ano atual e salvar nas preferências
            anoSelecionado = anoAtual

            // Salvar o valor do ano nas preferências compartilhadas
            val editor = sharedPreferences.edit()
            editor.putInt("anoSelecionado", anoSelecionado)
            editor.apply()
        }

        // Atualizar a TextView do mês selecionado na tela principal
        atualizarMesSelecionado(anoSelecionado, mesSelecionado)

        val setaBaixoImageView = findViewById<ImageView>(R.id.setaBaixoImageView)
        setaBaixoImageView.setOnClickListener {
            mostrarDialogMiniCalendario()
        }
        obterDadosReceitas()
        obterDadosDespesasEPlotarGrafico()
    }

    private fun obterDadosReceitas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            // Referência à coleção "receitas" do usuário atual
            val receitasRef = db.collection("receitas")
                .whereEqualTo("userId", uid)
                .whereEqualTo("recebido_ou_nao", "Recebido")

            receitasRef.get()
                .addOnSuccessListener { documents ->
                    var totalReceitas = 0.0

                    for (document in documents) {
                        val dataEfetuacaoTimestamp = document.get("data_efetuacao_receitas")
                        if (totalReceitas is Number && dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                            val calendar = Calendar.getInstance()
                            calendar.time = dataEfetuacao

                            if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(
                                    Calendar.YEAR
                                ) == anoSelecionado
                            ) {

                                val valorReceita = document.getDouble("valor_da_receita") ?: 0.0
                                totalReceitas += valorReceita
                            }
                        }
                    }
                    obterDadosDespesas(totalReceitas)  // Chame a função para obter despesas após receber receitas
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Falha ao obter as receitas: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun obterDadosDespesas(receitas: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            // Referência à coleção "despesas" do usuário atual
            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)
                .whereEqualTo("valor_ja_pago", "Pago")

            despesasRef.get()
                .addOnSuccessListener { documents ->
                    var totalDespesas = 0.0

                    for (document in documents) {
                        val dataEfetuacaoTimestamp = document.get("data_efetuacao")
                        val formaPagamento = document.getString("forma_pagamento")
                        if (totalDespesas is Number && dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                            val calendar = Calendar.getInstance()
                            calendar.time = dataEfetuacao

                            if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(
                                    Calendar.YEAR
                                ) == anoSelecionado
                            ) {
                                if(formaPagamento == "À vista" || formaPagamento == "Recorrente") {
                                    val valorDespesa = document.getDouble("valor_despesa") ?: 0.0
                                    totalDespesas += valorDespesa
                                }
                                else{
                                    val valorMensal = document.getDouble("valor_das_parcelas_calculado") ?: 0.0
                                    totalDespesas += valorMensal
                                }
                            }
                        }
                    }
                    plotarGraficoBarra(
                        receitas,
                        totalDespesas
                    )  // Chame a função para plotar o gráfico com os dados
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Falha ao obter as despesas: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun prepararDadosGrafico(receitas: Double, despesas: Double): BarData {
        val total = receitas + despesas
        val receitasPercent = (receitas / total) * 100
        val despesasPercent = (despesas / total) * 100

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, receitasPercent.toFloat()))
        entries.add(BarEntry(1f, despesasPercent.toFloat()))

        val dataSet = BarDataSet(entries, "Porcentagem")
        dataSet.colors = listOf(Color.GREEN, Color.RED)

        val legendas = arrayListOf("Receitas", "Despesas")
        val data = BarData(dataSet)
        data.barWidth = 0.4f

        return data
    }

    private fun plotarGraficoBarra(receitas: Double, despesas: Double) {
        val chart = findViewById<BarChart>(R.id.chart)
        // Verificar se há dados para plotar o gráfico
        if (receitas == 0.0 && despesas == 0.0) {
            // Limpar o gráfico e exibir mensagem
            chart.clear()
            chart.setNoDataText("Você não possui registros para este mês.")
            chart.setNoDataTextColor(Color.GRAY)
            chart.invalidate()

            // Limpar os TextViews de receitas, despesas e saldo
            findViewById<TextView>(R.id.textViewReceitas).text = "Receitas: R$ 0,00"
            findViewById<TextView>(R.id.textViewDespesas).text = "Despesas: R$ 0,00"
            findViewById<TextView>(R.id.textViewSaldo).text = "Saldo: R$ 0,00"

            return  // Não há dados para plotar, então saímos da função
        }
        chart.setNoDataText("")  // Limpar a mensagem se havia sido exibida anteriormente
        val barData = prepararDadosGrafico(receitas, despesas)

        chart.data = barData

        val legendas = arrayListOf("Receitas", "Despesas")
        val legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 12f
        legend.textColor = Color.BLACK
        legend.formSize = 12f
        legend.xEntrySpace = 12f

        val xAxis = chart.xAxis
        xAxis.labelCount = 2
        xAxis.valueFormatter = IndexAxisValueFormatter(legendas)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 12f

        val yAxisLeft = chart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 100f
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.textSize = 12f

        chart.axisRight.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.setPinchZoom(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)

        val yAxis = chart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 100f
        yAxis.setDrawLabels(false)  // Não mostrar os números no eixo y
        yAxis.setDrawAxisLine(false)  // Não mostrar a linha do eixo y
        yAxis.setDrawGridLines(false)  // Não mostrar as linhas de grade
        chart.setPinchZoom(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)

        // Calcular o saldo
        val saldo = receitas - despesas

        // Atualizar os TextViews com os valores obtidos
        val textViewReceitas = findViewById<TextView>(R.id.textViewReceitas)
        textViewReceitas.text = "Receitas: ${formatCurrencyValue(receitas)}"

        val textViewDespesas = findViewById<TextView>(R.id.textViewDespesas)
        textViewDespesas.text = "Despesas: ${formatCurrencyValue(despesas)}"

        val textViewSaldo = findViewById<TextView>(R.id.textViewSaldo)
        textViewSaldo.text = "Saldo: ${formatCurrencyValue(saldo)}"

        chart.animateY(800)
        chart.invalidate()
    }

    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }

    // Função para formatar um valor monetário como uma string no formato R$ 0,00
    private fun formatCurrencyValue(value: Double): String {
        return String.format("%.2f", value)
    }

    private fun calcularSaldoAtual(totalReceitas: Double, totalDespesas: Double): Double {
        return totalReceitas - totalDespesas
    }

    private fun atualizarMesSelecionado(ano: Int, mes: Int) {
        val mesAtual = mesesAbreviados[mes]
        val texto = "$mesAtual $ano"
        mesSelecionadoTextView.text = texto
    }

    private fun atualizarMesSelecionadoNoDialog(mes: Int, ano: Int) {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNameGrafico = "PrefsGrafico_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNameGrafico, MODE_PRIVATE)

        // Atualiza o mês selecionado com o valor escolhido no diálogo
        mesSelecionado = mes
        anoSelecionado = ano

        // Obter o ano atual
        val calendar = Calendar.getInstance()
        val anoAtual = calendar.get(Calendar.YEAR)

        // Atualizar a TextView do mês selecionado na tela principal
        atualizarMesSelecionado(anoSelecionado, mesSelecionado)

        // Salvar o valor do mês nas preferências compartilhadas
        val editor = sharedPreferences.edit()
        editor.putInt("mesSelecionado", mesSelecionado)
        editor.putInt("anoSelecionado", anoSelecionado)
        editor.apply()
        obterDadosReceitas()
        obterDadosDespesasEPlotarGrafico()
    }

    private fun mostrarDialogMiniCalendario() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNameGrafico = "PrefsGrafico_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNameGrafico, MODE_PRIVATE)

        val dialogView = layoutInflater.inflate(R.layout.dialog_mini_calendario, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Selecione o ano e mês")

        val anoPicker = dialogView.findViewById<NumberPicker>(R.id.anoPicker)
        val mesPicker = dialogView.findViewById<NumberPicker>(R.id.mesPicker)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Configurar o NumberPicker para o ano
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anosExibidos = 71 // Quantidade total de anos exibidos no NumberPicker (de -5 a +5)
        anoPicker.minValue =
            anoAtual - (anosExibidos / 2) // Exibir a metade dos anos antes do ano atual
        anoPicker.maxValue =
            anoAtual + (anosExibidos / 2) // Exibir a metade dos anos após o ano atual
        anoPicker.value = anoSelecionado // Selecionar o ano que estava previamente selecionado

        // Configurar o NumberPicker para os meses
        mesPicker.minValue = 0 // Janeiro (índice 0)
        mesPicker.maxValue = mesesAbreviados.size - 1 // Dezembro (índice 11)
        mesPicker.displayedValues = mesesAbreviados // Mostrar os meses abreviados
        mesPicker.value = mesSelecionado // Selecionar o mês atual

        val dialog = dialogBuilder.create()

        btnConfirmar.setOnClickListener {
            val anoSelecionado = anoPicker.value
            val mesSelecionado = mesPicker.value
            atualizarMesSelecionado(anoSelecionado, mesSelecionado)

            atualizarMesSelecionadoNoDialog(mesSelecionado, anoSelecionado)
            obterDadosReceitas()
            obterDadosDespesasEPlotarGrafico()

            dialog.dismiss() // Fechar o dialog após confirmar
        }

        dialog.show()
    }

    data class Transacao(
        val categoria: String?, // Nome da categoria
        val iconeResId: Int,   // ID do ícone
        val corCirculo: Int,    // Cor do círculo
        val valor: Double       // Valor da transação
    )

    private fun obterDadosDespesasEPlotarGrafico() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Suponha que você tenha uma coleção "despesas" no Firestore
        val despesasRef = FirebaseFirestore.getInstance().collection("despesas")
            .whereEqualTo("userId", userId)
            .whereEqualTo("valor_ja_pago", "Pago")

        despesasRef.get()
            .addOnSuccessListener { documents ->
                val despesasList = mutableListOf<Transacao>()

                for (document in documents) {
                    val categoria = document.getString("categoria_escolhida_pelo_usuario")
                    val iconeResId = (document.get("iconeResId") as? Long)?.toInt() ?: 0
                    val corCirculo = (document.get("corCirculo") as? Long)?.toInt() ?: 0
                    val valorDespesa = document.getDouble("valor_despesa") ?: 0.0
                    val valorMensal = document.getDouble("valor_das_parcelas_calculado") ?: 0.0
                    val dataEfetuacaoTimestamp = document.get("data_efetuacao")
                    val formaPagamento = document.getString("forma_pagamento")

                    if (formaPagamento == "À vista" || formaPagamento == "Recorrente") {
                        if (dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                            val calendar = Calendar.getInstance()
                            calendar.time = dataEfetuacao

                            if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(
                                    Calendar.YEAR
                                ) == anoSelecionado
                            ) {
                                // Suponha que você tenha uma classe Transacao para armazenar as informações
                                val despesa =
                                    Transacao(categoria, iconeResId, corCirculo, valorDespesa)

                                despesasList.add(despesa)
                            }
                        }
                    }
                    else{
                        if (dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                            val calendar = Calendar.getInstance()
                            calendar.time = dataEfetuacao

                            if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(
                                    Calendar.YEAR
                                ) == anoSelecionado
                            ) {
                                // Suponha que você tenha uma classe Transacao para armazenar as informações
                                val despesa =
                                    Transacao(categoria, iconeResId, corCirculo, valorMensal)

                                despesasList.add(despesa)
                            }
                        }
                    }
                }
                // Agora você tem a lista de despesas com as informações de categoria, ícone e cor
                // Use esses dados para plotar o gráfico de pizza de despesas
                val totaisPorCategoria = obterTotaisPorCategoria(despesasList)

                plotarGraficoPizzaDespesas(despesasList, totaisPorCategoria)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Falha ao obter despesas: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun plotarGraficoPizzaDespesas(despesasList: List<Transacao>,  totalPorCategoria: Map<String, Double>) {
        // Crie uma lista de entradas para o gráfico de pizza
        val entries = mutableListOf<PieEntry>()
        val cores = mutableListOf<Int>()
        val categoriasProcessadas = mutableSetOf<String>()

        // Calcule o valor total das despesas
        var valorTotalDespesas = 0.0
        
        // Iterar sobre os totais por categoria para criar as entradas do gráfico
        for ((categoria, total) in totalPorCategoria) {
            valorTotalDespesas += total

            val despesa = despesasList.find { it.categoria == categoria }
            val entry = PieEntry(total.toFloat())

            if (despesa != null) {
                cores.add(despesa.corCirculo)
            }

            entries.add(entry)
        }

        // Crie o conjunto de dados
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = cores

        // Crie o gráfico de pizza
        val pieChart = findViewById<PieChart>(R.id.pieChartDespesas)
        val data = PieData(dataSet)

        if (valorTotalDespesas == 0.0) {
            // Limpar o gráfico e exibir mensagem
            pieChart.clear()
            pieChart.setNoDataText("Você não possui registros para este mês.")
            pieChart.setNoDataTextColor(Color.GRAY)
            pieChart.invalidate()

            return  // Não há dados para plotar, então saímos da função
        }
        pieChart.setNoDataText("")  // Limpar a mensagem se havia sido exibida anteriormente


        val formatter = PercentFormatter(pieChart)
        data.setValueFormatter(formatter)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setUsePercentValues(true)
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(12f) // Tamanho da fonte (ajuste conforme necessário)
        //pieChart.setDrawHoleEnabled(false)
        pieChart.invalidate()

        // Adicione os círculos coloridos e nomes das categorias ao layout
        val legendasLayout = findViewById<LinearLayout>(R.id.legendasPizzaDespesaLayout)
        legendasLayout.removeAllViews()
        legendasLayout.orientation = LinearLayout.VERTICAL
        legendasLayout.gravity = Gravity.START

        // Dentro do loop para criar as legendas
        for (i in 0 until despesasList.size) {
            val categoria = despesasList[i].categoria
            val corCategoria = despesasList[i].corCirculo

            if (!categoriasProcessadas.contains(categoria)) {
                // Crie um layout horizontal para a entrada (círculo + texto)
                val entryLayout = LinearLayout(this)
                entryLayout.orientation = LinearLayout.HORIZONTAL
                val entryLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                entryLayoutParams.bottomMargin =
                    resources.getDimensionPixelSize(R.dimen.circle_margin)
                entryLayout.layoutParams = entryLayoutParams

                // Crie o círculo
                val circleView = View(this)
                circleView.setBackgroundResource(R.drawable.circle_background)
                val circleParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.circle_diameter),
                    resources.getDimensionPixelSize(R.dimen.circle_diameter)
                )
                circleParams.gravity = Gravity.CENTER_VERTICAL
                circleView.layoutParams = circleParams
                circleView.setBackgroundColor(corCategoria)

                // Chame createCategoryTextView com o conjunto de categorias já processadas
                val categoryTextView =
                    createCategoryTextView(categoria, totalPorCategoria, categoriasProcessadas)
                val textParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textParams.gravity = Gravity.CENTER_VERTICAL
                textParams.marginStart = resources.getDimensionPixelSize(R.dimen.circle_margin)
                categoryTextView.layoutParams = textParams

                // Adicione o círculo e o nome da categoria ao layout horizontal
                entryLayout.addView(circleView)
                entryLayout.addView(categoryTextView)

                // Adicione o layout horizontal ao layout vertical
                legendasLayout.addView(entryLayout)
            }
        }
    }
    private fun createCategoryTextView(categoria: String?, totalPorCategoria: Map<String, Double>, categoriasProcessadas: MutableSet<String>): TextView {
        val textView = TextView(this)

        if (!categoria.isNullOrBlank() && !categoriasProcessadas.contains(categoria)) {
            val totalCategoria = totalPorCategoria[categoria] ?: 0.0
            val texto = "$categoria - R$ ${formatCurrencyValue(totalCategoria)}"

            textView.text = texto
            textView.textSize = resources.getDimension(R.dimen.category_text_size)

            // Adicione a categoria ao conjunto de categorias já processadas
            categoriasProcessadas.add(categoria)
        } else {
            textView.visibility = View.GONE // Oculta a TextView se a categoria estiver em branco ou já foi processada
        }

        return textView
    }

    private fun obterTotaisPorCategoria(despesasList: List<Transacao>): Map<String, Double> {
        val totaisPorCategoria = mutableMapOf<String, Double>()

        for (despesa in despesasList) {
            val categoria = despesa.categoria ?: "Sem Categoria"
            val valorDespesa = despesa.valor

            val totalAtual = totaisPorCategoria.getOrDefault(categoria, 0.0)
            totaisPorCategoria[categoria] = totalAtual + valorDespesa
        }

        return totaisPorCategoria
    }
}