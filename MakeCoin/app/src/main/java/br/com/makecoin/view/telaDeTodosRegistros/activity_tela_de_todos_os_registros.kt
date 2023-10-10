package br.com.makecoin.view.telaDeTodosRegistros

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.telaEditarRegistros.activity_tela_visualizar_despesas
import br.com.makecoin.view.telaEditarRegistros.activity_tela_visualizar_registro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class activity_tela_de_todos_os_registros : AppCompatActivity() {
    // These variables should be defined at the beginning of your class
    private val mesesAbreviados = arrayOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    private var mesSelecionado = 0
    private var anoSelecionado = 0
    private var documentosReceitas: List<DocumentSnapshot> = mutableListOf() // Declaração da variável no nível da classe
    private var documentosDespesas: List<DocumentSnapshot> = mutableListOf() // Declaração da variável no nível da classe
    private lateinit var mesSelecionadoTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var valorParcelaAtual: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_de_todos_os_registros)
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileNameRegistros = "PrefsRegistros_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

        val setaBaixoImageView = findViewById<ImageView>(R.id.setaBaixoImageView)

        setaBaixoImageView.setOnClickListener {
            mostrarDialogMiniCalendario()
        }
        val voltarTelaPrincipal = findViewById<ImageView>(R.id.Voltar_tela_inicial)
        voltarTelaPrincipal.setOnClickListener {
            abrirTelaPrincipal()
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
            val prefsFileNameRegistros = "PrefsRegistros_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

            // Se estiver salvo, carregar o valor e atualizar a TextView
            mesSelecionado = sharedPreferences.getInt("mesSelecionado", 0)
        } else {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNameRegistros = "PrefsRegistros_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

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
            val prefsFileNameRegistros = "PrefsRegistros_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

            // Se estiver salvo, carregar o valor
            anoSelecionado = sharedPreferences.getInt("anoSelecionado", anoAtual)
        } else {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileNameRegistros = "PrefsRegistros_$userId"
            sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

            // Se não estiver salvo, definir o ano atual e salvar nas preferências
            anoSelecionado = anoAtual

            // Salvar o valor do ano nas preferências compartilhadas
            val editor = sharedPreferences.edit()
            editor.putInt("anoSelecionado", anoSelecionado)
            editor.apply()
        }

        // Atualizar a TextView do mês selecionado na tela principal
        atualizarMesSelecionado(anoSelecionado, mesSelecionado)

        // Chamando a função para carregar e exibir registros
        carregarEExibirRegistros()

        exibirUltimosReceitas(documentosReceitas, mesSelecionado, anoSelecionado)
        exibirUltimosRegistros(documentosDespesas, mesSelecionado, anoSelecionado)

        atualizarValoresReceitasDespesas()
    }
    private fun atualizarMesSelecionado(ano: Int, mes: Int) {
        val mesAtual = mesesAbreviados[mes]
        val texto = "$mesAtual $ano"
        mesSelecionadoTextView.text = texto
    }
    private fun atualizarMesSelecionadoNoDialog(mes: Int, ano: Int) {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val prefsFileNameRegistros = "PrefsRegistros_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

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
        atualizarValoresReceitasDespesas()


    }
    private fun mostrarDialogMiniCalendario() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val prefsFileNameRegistros = "PrefsRegistros_$userId"
        sharedPreferences = getSharedPreferences(prefsFileNameRegistros, MODE_PRIVATE)

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
        anoPicker.minValue = anoAtual - (anosExibidos / 2) // Exibir a metade dos anos antes do ano atual
        anoPicker.maxValue = anoAtual + (anosExibidos / 2) // Exibir a metade dos anos após o ano atual
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

            // Faça o que desejar com o anoSelecionado e mesSelecionado
            // Por exemplo, atualize o mês selecionado na tela principal
            atualizarMesSelecionado(anoSelecionado, mesSelecionado)

            atualizarMesSelecionadoNoDialog(mesSelecionado, anoSelecionado)

            carregarEExibirRegistros()
            atualizarValoresReceitasDespesas()

            // Atualize a exibição dos registros
            exibirUltimosReceitas(documentosReceitas, mesSelecionado, anoSelecionado)
            exibirUltimosRegistros(documentosDespesas, mesSelecionado, anoSelecionado)

            dialog.dismiss() // Fechar o dialog após confirmar
        }

        dialog.show()
    }
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    private fun carregarEExibirRegistros() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            val receitasRef = db.collection("receitas")
                .whereEqualTo("userId", uid)
                .orderBy("data_efetuacao_receitas", Query.Direction.DESCENDING)

            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp_registro", Query.Direction.DESCENDING)

            receitasRef.get()
                .addOnSuccessListener { documents ->
                    val documentosReceitas = documents.documents
                    exibirUltimosReceitas(documentosReceitas, mesSelecionado, anoSelecionado)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Falha ao obter as receitas: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            despesasRef.get()
                .addOnSuccessListener { documents ->
                    val documentosDespesas = documents.documents// Obter a lista de documentos despesas

                    exibirUltimosRegistros(documentosDespesas, mesSelecionado, anoSelecionado)
                }
                .addOnFailureListener { exception ->
                    // Lidar com falha na obtenção das despesas
                    Toast.makeText(this, "Falha ao obter as despesas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun exibirUltimosReceitas(
        documentosUltimosReceitas: List<DocumentSnapshot>,
        mesSelecionado: Int,
        anoSelecionado: Int,
    ) {
        val containerLayout = findViewById<LinearLayout>(R.id.layoutRegistrosReceitas)
        val inflater = LayoutInflater.from(this)
        containerLayout.removeAllViews() // Remove todas as visualizações antigas

        for (receita in documentosUltimosReceitas) {

            val ganhojaRecebido = receita.getString("recebido_ou_nao")
            val idReceita = receita.getString("receita_id")

            val dataEfetuacaoTimestamp2 = receita.get("data_efetuacao_receitas") as com.google.firebase.Timestamp
            val dataEfetuacao2 = dataEfetuacaoTimestamp2.toDate()

            val calendar2 = Calendar.getInstance()
            calendar2.time = dataEfetuacao2

            if (ganhojaRecebido == "Recebido") {
                if (calendar2.get(Calendar.MONTH) == mesSelecionado && calendar2.get(Calendar.YEAR) == anoSelecionado) {
                    val itemReceita =
                        inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                    // Configurar os elementos do itemDespesa
                    val nomeDespesaTextView =
                        itemReceita.findViewById<TextView>(R.id.itemNomeDespesa)
                    val nomeCategoriaTextView =
                        itemReceita.findViewById<TextView>(R.id.itemNomeCategoria)
                    val descricaoTextView = itemReceita.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView = itemReceita.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemReceita.findViewById<TextView>(R.id.valorRegistro)
                    val proximaParcelaTextView =
                        itemReceita.findViewById<TextView>(R.id.dataRegistro)

                    nomeDespesaTextView.text = receita.getString("nome_da_receita")
                    nomeCategoriaTextView.text =
                        receita.getString("categoria_escolhida_pelo_usuario_receitas")
                    descricaoTextView.text = receita.getString("descricao_da_receita")
                    favorecidoTextView.text = receita.getString("nome_favorecido_receita")
                    valorParcelaTextView.text =
                        formatCurrencyValue(receita.getDouble("valor_da_receita") ?: 0.0)
                    valorParcelaTextView.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.green
                        )
                    ) // Certifique-se de ter uma cor chamada "verde" definida em seus recursos

                    // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = formatoData.format(dataEfetuacao2)
                    proximaParcelaTextView.text = dataFormatada

                    // Recuperar os IDs inteiros dos ícones e cores
                    val iconeResId = receita.getLong("iconeReceita")?.toInt() ?: 0
                    val corImagemRedonda = receita.getLong("corImagemReceita")?.toInt() ?: 0
                    val corCirculo = receita.getLong("corCirculoReceita")?.toInt() ?: 0

                    // Configurar o ícone diretamente usando o ID
                    val categoriaImageView =
                        itemReceita.findViewById<ImageView>(R.id.categoria_de_salario)
                    categoriaImageView.setImageResource(iconeResId)
                    categoriaImageView.setColorFilter(corImagemRedonda)

                    // Configurar a cor do círculo diretamente
                    val circuloImageView = itemReceita.findViewById<ImageView>(R.id.Circulo_cor)
                    circuloImageView.setColorFilter(corCirculo)

                    itemReceita.setOnClickListener {
                        // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                        // Por exemplo, para iniciar uma nova atividade:
                        val intent = Intent(this, activity_tela_visualizar_registro::class.java)
                        // Passar os campos da receita como extras
                        intent.putExtra("nome_da_receita", receita.getString("nome_da_receita"))
                        intent.putExtra("receita_id", idReceita)
                        intent.putExtra(
                            "categoria_da_receita",
                            receita.getString("categoria_escolhida_pelo_usuario_receitas")
                        )
                        intent.putExtra(
                            "descricao_da_receita",
                            receita.getString("descricao_da_receita")
                        )
                        intent.putExtra(
                            "nome_favorecido_receita",
                            receita.getString("nome_favorecido_receita")
                        )
                        intent.putExtra("recebido_ou_nao", receita.getString("recebido_ou_nao"))
                        intent.putExtra(
                            "valor_da_receita",
                            receita.getDouble("valor_da_receita") ?: 0.0
                        )
                        intent.putExtra(
                            "data_efetuacao_receita",
                            dataEfetuacao2.time
                        ) // Enviar a data como um timestamp
                        intent.putExtra(
                            "iconeReceita",
                            receita.getLong("iconeReceita")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corImagemReceita",
                            receita.getLong("corImagemReceita")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corCirculoReceita",
                            receita.getLong("corCirculoReceita")?.toInt() ?: 0
                        )
                        startActivity(intent)
                    }
                    containerLayout.addView(itemReceita)
                }
            }else {
                if (calendar2.get(Calendar.MONTH) == mesSelecionado && calendar2.get(Calendar.YEAR) == anoSelecionado) {
                    val itemReceita =
                        inflater.inflate(R.layout.ultimos_registros, containerLayout, false)
                    // Configurar os elementos do itemDespesa
                    val nomeDespesaTextView =
                        itemReceita.findViewById<TextView>(R.id.itemNomeDespesa)
                    val nomeCategoriaTextView =
                        itemReceita.findViewById<TextView>(R.id.itemNomeCategoria)
                    val descricaoTextView = itemReceita.findViewById<TextView>(R.id.itemDescricao)
                    val favorecidoTextView = itemReceita.findViewById<TextView>(R.id.itemFavorecido)
                    val valorParcelaTextView =
                        itemReceita.findViewById<TextView>(R.id.valorRegistro)
                    val proximaParcelaTextView =
                        itemReceita.findViewById<TextView>(R.id.dataRegistro)

                    nomeDespesaTextView.text = receita.getString("nome_da_receita")
                    nomeCategoriaTextView.text =
                        receita.getString("categoria_escolhida_pelo_usuario_receitas")
                    descricaoTextView.text = receita.getString("descricao_da_receita")
                    favorecidoTextView.text = receita.getString("nome_favorecido_receita")
                    valorParcelaTextView.text = "Valor pendente"
                    valorParcelaTextView.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.green
                        )
                    ) // Certifique-se de ter uma cor chamada "verde" definida em seus recursos

                    // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                    val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dataFormatada = formatoData.format(dataEfetuacao2)
                    proximaParcelaTextView.text = dataFormatada

                    // Recuperar os IDs inteiros dos ícones e cores
                    val iconeResId = receita.getLong("iconeReceita")?.toInt() ?: 0
                    val corImagemRedonda = receita.getLong("corImagemReceita")?.toInt() ?: 0
                    val corCirculo = receita.getLong("corCirculoReceita")?.toInt() ?: 0

                    // Configurar o ícone diretamente usando o ID
                    val categoriaImageView =
                        itemReceita.findViewById<ImageView>(R.id.categoria_de_salario)
                    categoriaImageView.setImageResource(iconeResId)
                    categoriaImageView.setColorFilter(corImagemRedonda)

                    // Configurar a cor do círculo diretamente
                    val circuloImageView = itemReceita.findViewById<ImageView>(R.id.Circulo_cor)
                    circuloImageView.setColorFilter(corCirculo)

                    itemReceita.setOnClickListener {
                        // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                        // Por exemplo, para iniciar uma nova atividade:
                        val intent = Intent(this, activity_tela_visualizar_registro::class.java)
                        // Passar os campos da receita como extras
                        intent.putExtra("nome_da_receita", receita.getString("nome_da_receita"))
                        intent.putExtra("receita_id", idReceita)
                        intent.putExtra(
                            "categoria_da_receita",
                            receita.getString("categoria_escolhida_pelo_usuario_receitas")
                        )
                        intent.putExtra(
                            "descricao_da_receita",
                            receita.getString("descricao_da_receita")
                        )
                        intent.putExtra(
                            "nome_favorecido_receita",
                            receita.getString("nome_favorecido_receita")
                        )
                        intent.putExtra("recebido_ou_nao", receita.getString("recebido_ou_nao"))
                        intent.putExtra(
                            "valor_da_receita",
                            receita.getDouble("valor_da_receita") ?: 0.0
                        )
                        intent.putExtra(
                            "data_efetuacao_receita",
                            dataEfetuacao2.time
                        ) // Enviar a data como um timestamp
                        intent.putExtra(
                            "iconeReceita",
                            receita.getLong("iconeReceita")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corImagemReceita",
                            receita.getLong("corImagemReceita")?.toInt() ?: 0
                        )
                        intent.putExtra(
                            "corCirculoReceita",
                            receita.getLong("corCirculoReceita")?.toInt() ?: 0
                        )
                        startActivity(intent)
                    }
                    containerLayout.addView(itemReceita)
                }
            }
        }
    }
    private fun exibirUltimosRegistros(
        documentosDespesas: List<DocumentSnapshot>,
        mesSelecionado: Int,
        anoSelecionado: Int,
    ) {
        val containerLayout = findViewById<LinearLayout>(R.id.layoutRegistros)
        val inflater = LayoutInflater.from(this)
        containerLayout.removeAllViews() // Remove todas as visualizações antigas

        for (despesa in documentosDespesas) {
            val valorJaPago = despesa.getString("valor_ja_pago")
            val numeroParcelas = despesa.getLong("numero_de_parcelas") ?: 1
            val idDespesa = despesa.getString("despesa_id")
            val opcaoRecorrente = despesa.getString("opcao_recorrente")
            val formaPagamento = despesa.getString("forma_pagamento")
            val dataEfetuacaoTimestamp = despesa.get("data_efetuacao") as com.google.firebase.Timestamp
            val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
            val parcelasPagas = despesa.getLong("parcela_paga") ?: 1

            val calendar = Calendar.getInstance()
            calendar.time = dataEfetuacao

            if (valorJaPago == "Pago") {
                if (formaPagamento == "Mensal") {
                    if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                        val itemDespesa =
                            inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                        // Configurar os elementos do itemDespesa
                        val nomeDespesaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                        val nomeCategoriaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                        val descricaoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                        val favorecidoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                        val valorParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                        val proximaParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                        nomeDespesaTextView.text = despesa.getString("nome_despesa")
                        nomeCategoriaTextView.text =
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        descricaoTextView.text = despesa.getString("descricao")
                        favorecidoTextView.text = despesa.getString("nome_favorecido")
                        valorParcelaTextView.text = formatCurrencyValue(
                            despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                        )

                        // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        proximaParcelaTextView.text = dataFormatada

                        // Recuperar os IDs inteiros dos ícones e cores
                        val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                        val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                        // Configurar o ícone diretamente usando o ID
                        val categoriaImageView =
                            itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                        categoriaImageView.setImageResource(iconeResId)
                        categoriaImageView.setColorFilter(corImagemRedonda)

                        // Configurar a cor do círculo diretamente
                        val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                        circuloImageView.setColorFilter(corCirculo)
                        itemDespesa.setOnClickListener {
                            // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                            // Por exemplo, para iniciar uma nova atividade:
                            val intent =
                                Intent(this, activity_tela_visualizar_despesas::class.java)
                            // Passar os campos da receita como extras
                            intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                            intent.putExtra(
                                "categoria_escolhida_pelo_usuario",
                                despesa.getString("categoria_escolhida_pelo_usuario")
                            )
                            intent.putExtra("descricao", despesa.getString("descricao"))
                            intent.putExtra(
                                "nome_favorecido",
                                despesa.getString("nome_favorecido")
                            )
                            intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                            intent.putExtra(
                                "valor_das_parcelas_calculado",
                                despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                            )
                            intent.putExtra(
                                "data_efetuacao",
                                dataEfetuacao.time
                            ) // Enviar a data como um timestamp
                            intent.putExtra(
                                "iconeResId",
                                despesa.getLong("iconeResId")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corImagemRedonda",
                                despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corCirculo",
                                despesa.getLong("corCirculo")?.toInt() ?: 0
                            )
                            intent.putExtra("despesa_id", idDespesa)
                            intent.putExtra("numero_de_parcelas", numeroParcelas)
                            intent.putExtra("parcela_paga", parcelasPagas)
                            startActivity(intent)
                        }

                        containerLayout.addView(itemDespesa)
                    }
                } else if (formaPagamento == "À vista" || formaPagamento == "Recorrente") {
                    if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                        val itemDespesa =
                            inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                        // Configurar os elementos do itemDespesa
                        val nomeDespesaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                        val nomeCategoriaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                        val descricaoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                        val favorecidoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                        val valorParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                        val proximaParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                        nomeDespesaTextView.text = despesa.getString("nome_despesa")
                        nomeCategoriaTextView.text =
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        descricaoTextView.text = despesa.getString("descricao")
                        favorecidoTextView.text = despesa.getString("nome_favorecido")
                        valorParcelaTextView.text = formatCurrencyValue(
                            despesa.getDouble("valor_despesa") ?: 0.0
                        )

                        // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        proximaParcelaTextView.text = dataFormatada

                        // Recuperar os IDs inteiros dos ícones e cores
                        val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                        val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                        // Configurar o ícone diretamente usando o ID
                        val categoriaImageView =
                            itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                        categoriaImageView.setImageResource(iconeResId)
                        categoriaImageView.setColorFilter(corImagemRedonda)

                        // Configurar a cor do círculo diretamente
                        val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                        circuloImageView.setColorFilter(corCirculo)
                        itemDespesa.setOnClickListener {
                            // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                            // Por exemplo, para iniciar uma nova atividade:
                            val intent =
                                Intent(this, activity_tela_visualizar_despesas::class.java)
                            // Passar os campos da receita como extras
                            intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                            intent.putExtra(
                                "categoria_escolhida_pelo_usuario",
                                despesa.getString("categoria_escolhida_pelo_usuario")
                            )
                            intent.putExtra("descricao", despesa.getString("descricao"))
                            intent.putExtra(
                                "nome_favorecido",
                                despesa.getString("nome_favorecido")
                            )
                            intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                            intent.putExtra(
                                "valor_das_parcelas_calculado",
                                despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                            )
                            intent.putExtra(
                                "data_efetuacao",
                                dataEfetuacao.time
                            ) // Enviar a data como um timestamp
                            intent.putExtra(
                                "iconeResId",
                                despesa.getLong("iconeResId")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corImagemRedonda",
                                despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corCirculo",
                                despesa.getLong("corCirculo")?.toInt() ?: 0
                            )
                            intent.putExtra("despesa_id", idDespesa)
                            intent.putExtra("numero_de_parcelas", numeroParcelas)
                            intent.putExtra("parcela_paga", parcelasPagas)
                            intent.putExtra("opcao_recorrente", opcaoRecorrente)
                            startActivity(intent)
                        }

                        containerLayout.addView(itemDespesa)
                    }
                }
            }
            else{
                if (formaPagamento == "Mensal") {
                    if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                        val itemDespesa =
                            inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                        // Configurar os elementos do itemDespesa
                        val nomeDespesaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                        val nomeCategoriaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                        val descricaoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                        val favorecidoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                        val valorParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                        val proximaParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                        nomeDespesaTextView.text = despesa.getString("nome_despesa")
                        nomeCategoriaTextView.text =
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        descricaoTextView.text = despesa.getString("descricao")
                        favorecidoTextView.text = despesa.getString("nome_favorecido")
                        valorParcelaTextView.text = "Valor pendente"

                        // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        proximaParcelaTextView.text = dataFormatada

                        // Recuperar os IDs inteiros dos ícones e cores
                        val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                        val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                        // Configurar o ícone diretamente usando o ID
                        val categoriaImageView =
                            itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                        categoriaImageView.setImageResource(iconeResId)
                        categoriaImageView.setColorFilter(corImagemRedonda)

                        // Configurar a cor do círculo diretamente
                        val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                        circuloImageView.setColorFilter(corCirculo)
                        itemDespesa.setOnClickListener {
                            // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                            // Por exemplo, para iniciar uma nova atividade:
                            val intent =
                                Intent(this, activity_tela_visualizar_despesas::class.java)
                            // Passar os campos da receita como extras
                            intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                            intent.putExtra(
                                "categoria_escolhida_pelo_usuario",
                                despesa.getString("categoria_escolhida_pelo_usuario")
                            )
                            intent.putExtra("descricao", despesa.getString("descricao"))
                            intent.putExtra(
                                "nome_favorecido",
                                despesa.getString("nome_favorecido")
                            )
                            intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                            intent.putExtra(
                                "valor_das_parcelas_calculado",
                                despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                            )
                            intent.putExtra(
                                "data_efetuacao",
                                dataEfetuacao.time
                            ) // Enviar a data como um timestamp
                            intent.putExtra(
                                "iconeResId",
                                despesa.getLong("iconeResId")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corImagemRedonda",
                                despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corCirculo",
                                despesa.getLong("corCirculo")?.toInt() ?: 0
                            )
                            intent.putExtra("despesa_id", idDespesa)
                            intent.putExtra("numero_de_parcelas", numeroParcelas)
                            intent.putExtra("parcela_paga", parcelasPagas)
                            startActivity(intent)
                        }

                        containerLayout.addView(itemDespesa)
                    }
                }
                else if(formaPagamento == "À vista" || formaPagamento == "Recorrente") {
                    if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                        val itemDespesa =
                            inflater.inflate(R.layout.ultimos_registros, containerLayout, false)

                        // Configurar os elementos do itemDespesa
                        val nomeDespesaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeDespesa)
                        val nomeCategoriaTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemNomeCategoria)
                        val descricaoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemDescricao)
                        val favorecidoTextView =
                            itemDespesa.findViewById<TextView>(R.id.itemFavorecido)
                        val valorParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.valorRegistro)
                        val proximaParcelaTextView =
                            itemDespesa.findViewById<TextView>(R.id.dataRegistro)

                        nomeDespesaTextView.text = despesa.getString("nome_despesa")
                        nomeCategoriaTextView.text =
                            despesa.getString("categoria_escolhida_pelo_usuario")
                        descricaoTextView.text = despesa.getString("descricao")
                        favorecidoTextView.text = despesa.getString("nome_favorecido")
                        valorParcelaTextView.text = "Valor pendente"

                        // Recuperar a data de efetuação do Firestore e formatá-la para exibição
                        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dataFormatada = formatoData.format(dataEfetuacao)
                        proximaParcelaTextView.text = dataFormatada

                        // Recuperar os IDs inteiros dos ícones e cores
                        val iconeResId = despesa.getLong("iconeResId")?.toInt() ?: 0
                        val corImagemRedonda = despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                        val corCirculo = despesa.getLong("corCirculo")?.toInt() ?: 0

                        // Configurar o ícone diretamente usando o ID
                        val categoriaImageView =
                            itemDespesa.findViewById<ImageView>(R.id.categoria_de_salario)
                        categoriaImageView.setImageResource(iconeResId)
                        categoriaImageView.setColorFilter(corImagemRedonda)

                        // Configurar a cor do círculo diretamente
                        val circuloImageView = itemDespesa.findViewById<ImageView>(R.id.Circulo_cor)
                        circuloImageView.setColorFilter(corCirculo)
                        itemDespesa.setOnClickListener {
                            // Aqui você pode iniciar a atividade desejada quando o usuário clicar no itemReceita
                            // Por exemplo, para iniciar uma nova atividade:
                            val intent =
                                Intent(this, activity_tela_visualizar_despesas::class.java)
                            // Passar os campos da receita como extras
                            intent.putExtra("nome_despesa", despesa.getString("nome_despesa"))
                            intent.putExtra(
                                "categoria_escolhida_pelo_usuario",
                                despesa.getString("categoria_escolhida_pelo_usuario")
                            )
                            intent.putExtra("descricao", despesa.getString("descricao"))
                            intent.putExtra(
                                "nome_favorecido",
                                despesa.getString("nome_favorecido")
                            )
                            intent.putExtra("valor_ja_pago", despesa.getString("valor_ja_pago"))
                            intent.putExtra(
                                "valor_das_parcelas_calculado",
                                despesa.getDouble("valor_das_parcelas_calculado") ?: 0.0
                            )
                            intent.putExtra(
                                "data_efetuacao",
                                dataEfetuacao.time
                            ) // Enviar a data como um timestamp
                            intent.putExtra(
                                "iconeResId",
                                despesa.getLong("iconeResId")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corImagemRedonda",
                                despesa.getLong("corImagemRedonda")?.toInt() ?: 0
                            )
                            intent.putExtra(
                                "corCirculo",
                                despesa.getLong("corCirculo")?.toInt() ?: 0
                            )
                            intent.putExtra("despesa_id", idDespesa)
                            intent.putExtra("numero_de_parcelas", numeroParcelas)
                            intent.putExtra("parcela_paga", parcelasPagas)
                            intent.putExtra("opcao_recorrente", opcaoRecorrente)
                            startActivity(intent)
                        }

                        containerLayout.addView(itemDespesa)
                    }
                }
            }
        }
    }
    // Função para formatar um valor monetário como uma string no formato R$ 0,00
    private fun formatCurrencyValue(value: Double): String {
        return String.format("R$ %.2f", value)
    }

    private fun atualizarValoresReceitasDespesas() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val db = FirebaseFirestore.getInstance()

            // Referência à coleção "receitas" do usuário atual
            val receitasRef = db.collection("receitas")
                .whereEqualTo("userId", uid)

            // Referência à coleção "despesas" do usuário atual
            val despesasRef = db.collection("despesas")
                .whereEqualTo("userId", uid)

            // Calcular o total das receitas
            receitasRef.get()
                .addOnSuccessListener { documents ->
                    var totalReceitas = 0.0

                    for (document in documents) {
                        val dataEfetuacaoTimestamp = document.get("data_efetuacao_receitas")
                        val valorReceita = document.get("valor_da_receita")
                        val ganhoJaRecebido = document.getString("recebido_ou_nao")

                        if (ganhoJaRecebido == "Recebido") {
                            if (valorReceita is Number && dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                                val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                                val calendar = Calendar.getInstance()
                                calendar.time = dataEfetuacao

                                // Verificar se a receita está no mês e ano selecionados
                                if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                                    totalReceitas += valorReceita.toDouble()
                                }
                            }
                        }
                    }
                    // Exibir o total de receitas no TextView
                    val valorReceitasTextView = findViewById<TextView>(R.id.valor_receitas)
                    valorReceitasTextView.text = formatCurrencyValue(totalReceitas)

                    // Calcular o total das despesas
                    despesasRef.get()
                        .addOnSuccessListener { documents ->
                            var totalDespesas = 0.0

                            for (document in documents) {
                                val valorDespesa = document.getDouble("valor_despesa") ?: 0.0
                                val dataEfetuacaoTimestamp = document.get("data_efetuacao")

                                val formaPagamento = document.getString("forma_pagamento")
                                val opcaoRecorrente = document.getString("opcao_recorrente")
                                val valorMensalDespesa = document.getDouble("valor_das_parcelas_calculado") ?: 0.0
                                val numeroParcelas = document.getLong("numero_de_parcelas") ?: 1 // Supondo que 1 é o valor padrão caso não esteja presente
                                val tempoRecorrente = document.getLong("tempo_recorrente") ?: 1
                                val valorJaPago = document.getString("valor_ja_pago")

                                if (valorJaPago == "Pago") {
                                    if (dataEfetuacaoTimestamp is com.google.firebase.Timestamp) {
                                        val dataEfetuacao = dataEfetuacaoTimestamp.toDate()
                                        val calendar = Calendar.getInstance()
                                        calendar.time = dataEfetuacao

                                        // Verificar se a despesa está no mês e ano selecionados
                                        if (calendar.get(Calendar.MONTH) == mesSelecionado && calendar.get(Calendar.YEAR) == anoSelecionado) {
                                            if (formaPagamento == "À vista" || formaPagamento == "Recorrente") {
                                                totalDespesas += valorDespesa
                                            } else if (formaPagamento == "Mensal") {
                                                // Se for pagamento mensal, calcular apenas para o mês atual
                                                totalDespesas += valorMensalDespesa
                                            }
                                        }
                                    }
                                }
                            }
                            // Exibir o total de despesas no TextView
                            val valorDespesasTextView = findViewById<TextView>(R.id.valor_despesas)
                            valorDespesasTextView.text = formatCurrencyValue(totalDespesas)
                        }
                        .addOnFailureListener { exception ->
                            // Lidar com falha na obtenção das despesas
                            Toast.makeText(this, "Falha ao obter as despesas: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    // Lidar com falha na obtenção das receitas
                    Toast.makeText(this, "Falha ao obter as receitas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}