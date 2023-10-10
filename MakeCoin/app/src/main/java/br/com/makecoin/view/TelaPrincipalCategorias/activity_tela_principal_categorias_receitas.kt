package br.com.makecoin.view.TelaPrincipalCategorias

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import br.com.makecoin.R
import br.com.makecoin.view.TelaDasCategorias.activity_tela_das_categorias_receitas
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalGraficos.TelaPrincipalGraficos
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.HashMap
import java.util.Locale

class activity_tela_principal_categorias_receitas : AppCompatActivity() {
    private var selectedDateEfetuacao: Calendar? = null
    private lateinit var registrarDataHojeDataEfetuacaoButton: Button
    private val data = HashMap<String, Any>()
    private var categoriaSelecionada: Boolean = false
    // Declare uma variável para armazenar o SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    // Variável para armazenar a data selecionada no formato Long
    private var dataEfetuacaoInMillis: Long = 0
    // Declare as variáveis para armazenar as referências dos campos de entrada
    private lateinit var valorDaReceitaEditText: EditText
    private lateinit var descricaoDaReceitaEditText: EditText
    private val nomesReceitasList = mutableListOf<String>()
    private lateinit var autoCompleteTextViewReceita: AutoCompleteTextView
    private lateinit var autoCompleteTextViewFavorecidoReceita: AutoCompleteTextView
    private lateinit var toggleSwitch: Switch
    private var recebidoOuNaoRecebido: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_principal_categorias_receitas)
        FirebaseApp.initializeApp(this)
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName2 = "Prefs2_$userId"
        sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)

        // Obtenha as referências dos campos de entrada
        valorDaReceitaEditText = findViewById(R.id.valor_da_receita)
        val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_receita)
        autoCompleteTextViewReceita = textInputLayout.findViewById(R.id.auto_completa_texto_receita)
        val textInputLayout2: TextInputLayout = findViewById(R.id.Nome_do_favorecido_receita)
        autoCompleteTextViewFavorecidoReceita = textInputLayout2.findViewById(R.id.auto_completa_texto_favorecido_receita)
        descricaoDaReceitaEditText = findViewById(R.id.Descricao_da_receita)

        toggleSwitch = findViewById(R.id.toggleSwitch)
        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                recebidoOuNaoRecebido = "Recebido"
            } else {
                recebidoOuNaoRecebido = "Não recebido"
            }
            // Salvar o estado do toggleSwitch no SharedPreferences
            sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("recebido_ou_nao", isChecked)
            editor.apply()
        }

                val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNavigationView.itemBackgroundResource = R.drawable.bottom_navigation_item_background_receitas
                bottomNavigationView.setSelectedItemId(R.id.menu_receitas)

                bottomNavigationView.setOnNavigationItemSelectedListener { item ->

                    when (item.itemId) {
                        R.id.menu_despesas -> {
                            // Abrir a Tela 1
                            val intent1 = Intent(this, TelaPrincipalCategorias::class.java)
                            startActivity(intent1)
                            true
                        }

                        else -> false
                    }
                }
                val voltarTelaPrincipal = findViewById<ImageView>(R.id.Voltar_tela_inicial)
                voltarTelaPrincipal.setOnClickListener {
                    abrirTelaPrincipalCategorias()
                }

                // Obtenha a referência para o EditText
                val valorDaReceitaEditText = findViewById<EditText>(R.id.valor_da_receita)
                valorDaReceitaEditText.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                // Aplicando o estilo EditTextHintGreen ao EditText
                valorDaReceitaEditText.setTextAppearance(R.style.HintStyLE2)

                // Defina a cor verde também para o hint do EditText usando um SpannableString
                val hint = "R$ 0,00"
                val spannableString = SpannableString(hint)
                spannableString.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.white)),
                    0,
                    hint.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                valorDaReceitaEditText.hint = spannableString

                // Botão "Outros" para abrir o DatePickerDialog
                val abrirCalendarioDataEfetuacaoButton =
                    findViewById<Button>(R.id.abrir_calendario_data_efetuacao)
                abrirCalendarioDataEfetuacaoButton.setOnClickListener {
                    openDatePickerDialog() // Chama o método para exibir o DatePickerDialog
                }

                // Botão "Hoje" para definir a data como hoje
                registrarDataHojeDataEfetuacaoButton = findViewById<Button>(R.id.registrar_hoje_data_efetuacao)
                registrarDataHojeDataEfetuacaoButton.setOnClickListener {
                    selectedDateEfetuacao = Calendar.getInstance()
                    updateDataEfetuacaoTextView(selectedDateEfetuacao)

                    dataEfetuacaoInMillis = selectedDateEfetuacao?.timeInMillis ?: 0

                    toggleSwitch.isChecked = true

                    // Esconda os botões "Hoje" e "Outros" após definir a data manualmente
                    findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
                    findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility =
                        View.GONE

                    // Exiba o botão "Limpar Data"
                    findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

                    // Exiba o TextView com a data selecionada
                    findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
                    // Salve a data selecionada no SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
                    editor.apply()
                }

                // Botão "Limpar Data" para limpar a data selecionada
                val limparDataButton = findViewById<Button>(R.id.limpar_data_button)
                limparDataButton.setOnClickListener {
                    selectedDateEfetuacao = null

                    toggleSwitch.isChecked = false

                    // Exiba os botões "Hoje" e "Outros" novamente
                    findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility =
                        View.VISIBLE
                    findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility =
                        View.VISIBLE

                    // Esconda o botão "Limpar Data"
                    findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE

                    // Esconda o TextView com a data selecionada
                    findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE

                    // Limpe a data salva no SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.remove("data_efetuacao")
                    editor.apply()
                }
                // Defina a visibilidade do botão "Limpar Data" com base no estado da variável selectedDate
                if (selectedDateEfetuacao != null) {
                    limparDataButton.visibility = View.VISIBLE
                    updateDataEfetuacaoTextView(selectedDateEfetuacao)
                } else {
                    limparDataButton.visibility = View.GONE
                }
                val registrarReceitaButton = findViewById<Button>(R.id.Registrar_receita)
                registrarReceitaButton.setOnClickListener {
                    registrarReceita()
                }

        val irParaCategoriasDasReceitas = findViewById<ImageView>(R.id.Ir_para_tela_escolher_categorias_receitas)
        irParaCategoriasDasReceitas.setOnClickListener {
                    // Salvar os valores no SharedPreferences
                    salvarValoresAoSelecionarCategoria()
                    abrirTelaDasCategoriasDasReceitas()
                }
        // Obter os dados da categoria selecionada da Activity anterior
        val categoria = intent.getStringExtra("categoria_selecionada")


        val iconeResId = intent.getIntExtra("icone_res_id", 0)
        val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
        val corCirculo = intent.getIntExtra("circulo_cor", 0)

        // Agora você tem as informações da categoria selecionada
        // Faça o que precisar com essas informações, por exemplo:
        // - Defina o ícone e a cor da imagem redonda na tela de registro de receitas
        val categoriaImageView = findViewById<ImageView>(R.id.categoria_de_salario)
        categoriaImageView.setImageResource(iconeResId)

        // Defina a cor da imagem redonda
        categoriaImageView.setColorFilter(corImagemRedonda)

        // - Defina a cor do círculo
        val circuloImageView = findViewById<ImageView>(R.id.Circulo_cor)
        circuloImageView.setColorFilter(corCirculo)
       DadosCategoriasTemporarios.categoriaSelecionada = categoria

        // Verifique se há uma categoria selecionada pela Activity anterior
        if (!categoria.isNullOrEmpty()) {
            categoriaSelecionada = true
            val categoriaSelecionada = findViewById<TextView>(R.id.categoria_salario)
            categoriaSelecionada.text = categoria
        }

        valorDaReceitaEditText.requestFocus()

        // Crie um Handler para postar a exibição do teclado depois de um pequeno atraso
        Handler().postDelayed({
            // Abra o teclado
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(valorDaReceitaEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)  // Aguarde 200 milissegundos antes de exibir o teclado

        carregarNomesReceitasDoUsuario()
        carregarNomesFavorecidosReceitasDoUsuario()
        exibirDataSalvaNoSharedPreferences()
    }
    object DadosCategoriasTemporarios {
        var categoriaSelecionada: String? = ""
    }
    override fun onPause() {
        super.onPause()
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName2 = "Prefs2_$userId"

        // Salvar o estado atual do toggleSwitch no SharedPreferences
        val isChecked = toggleSwitch.isChecked
        sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("recebido_ou_nao", isChecked)
        editor.apply()
    }
    override fun onResume() {
        super.onResume()
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName2 = "Prefs2_$userId"
        sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)
        // Recuperar o estado do toggleSwitch do SharedPreferences ao retomar a tela
        val isRecebido = sharedPreferences.getBoolean("recebido_ou_nao", false)
        toggleSwitch.isChecked = isRecebido

        if (selectedDateEfetuacao != null) {
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
        }

        exibirDataSalvaNoSharedPreferences()
    }
    private fun salvarValoresAoSelecionarCategoria() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName2 = "Prefs2_$userId"
        sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)

        // Obter os valores inseridos pelo usuário
        val valorDaReceita = valorDaReceitaEditText.text.toString()
        val nomeDaReceita = autoCompleteTextViewReceita.text.toString()
        val descricaoDaReceita = descricaoDaReceitaEditText.text.toString()
        val nomeFavorecido = autoCompleteTextViewFavorecidoReceita.text.toString()

        // Salvar os valores no SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("valor_da_receita", valorDaReceita)
        editor.putString("nome_da_receita", nomeDaReceita)
        editor.putString("nome_do_favorecido", nomeFavorecido)
        editor.putString("descricao_da_receita", descricaoDaReceita) // Adicione a descrição ao SharedPreferences
        editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
        editor.apply()
    }
        private fun exibirDataSalvaNoSharedPreferences() {
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileName2 = "Prefs2_$userId"
            sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)

            // Recupere o valor da data_efetuacao do SharedPreferences
            dataEfetuacaoInMillis = sharedPreferences.getLong("data_efetuacao", 0)

            // Atualize a variável selectedDateEfetuacao com a data recuperada (se existir)
            if (dataEfetuacaoInMillis > 0) {
                selectedDateEfetuacao = Calendar.getInstance()
                selectedDateEfetuacao!!.timeInMillis = dataEfetuacaoInMillis

                // Exiba o botão "Limpar Data" e atualize o TextView com a data salva
                findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE
                findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
                updateDataEfetuacaoTextView(selectedDateEfetuacao)
            } else {
                // Caso não haja data salva, oculte o botão "Limpar Data" e o TextView
                findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE
                findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
            }

            // Exibir os valores salvos nos campos de entrada
            valorDaReceitaEditText.setText(sharedPreferences.getString("valor_da_receita", ""))
            autoCompleteTextViewReceita.setText(sharedPreferences.getString("nome_receita", ""))
            autoCompleteTextViewFavorecidoReceita.setText(sharedPreferences.getString("nome_do_favorecido", ""))
            descricaoDaReceitaEditText.setText(sharedPreferences.getString("descricao_da_receita", ""))
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
        }

    override fun onBackPressed() {
        // Impede que o usuário volte deslizando para a tela anterior
    }
    private fun abrirTelaPrincipalCategorias() {
        val intent = Intent(this, TelaPrincipalCategorias::class.java)
        startActivity(intent)
    }
    private fun abrirTelaPrincipal() {
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }
    private fun abrirTelaDasCategoriasDasReceitas() {
        val intent = Intent(this, activity_tela_das_categorias_receitas::class.java)
        startActivity(intent)
    }
    private fun formatarValorReceita(valor: String): String {
        return if (valor.contains(".")) {
            val partes = valor.split(".")
            if (partes.size == 2) {
                val centavos = partes[1]
                when (centavos.length) {
                    0 -> "$valor" + "00"
                    1 -> "$valor" + "0"
                    2 -> valor
                    else -> "$partes[0].${centavos.substring(0, 2)}"
                }
            } else {
                "$valor.00"
            }
        } else {
            "$valor.00"
        }
    }

    private fun obterDataHoraFormatada(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun updateDataEfetuacaoTextView(selectedDate: Calendar?) {
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)
            findViewById<TextView>(R.id.data_efetuacao_textview).text = formattedDate
        }
    }

    private fun openDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // Obtenha o ID do usuário logado
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
            val prefsFileName2 = "Prefs2_$userId"
            sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDateEfetuacao = selectedCalendar

            // Verifica se a data selecionada é anterior à data atual
            val currentDate = Calendar.getInstance()
            if (selectedCalendar.before(currentDate)) {
                toggleSwitch.isChecked = true // Ativa o toggle
            } else if(selectedCalendar == currentDate){
                toggleSwitch.isChecked = true // Ativa o toggle
            }
            else {
                toggleSwitch.isChecked = false // Deixa o toggle desativado
            }

            // Esconda os botões "Outros" e "Hoje" após selecionar a data manualmente
            findViewById<Button>(R.id.registrar_hoje_data_efetuacao).visibility = View.GONE
            findViewById<Button>(R.id.abrir_calendario_data_efetuacao).visibility = View.GONE

            // Exiba o botão "Limpar Data"
            findViewById<Button>(R.id.limpar_data_button).visibility = View.VISIBLE

            // Atualize o campo de texto ou realize qualquer ação adicional que você deseje
            // após a seleção da data.
            updateDataEfetuacaoTextView(selectedDateEfetuacao)
            dataEfetuacaoInMillis = selectedDateEfetuacao?.timeInMillis ?: 0

            // Salve a data selecionada no SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putLong("data_efetuacao", dataEfetuacaoInMillis)
            editor.apply()

            if (selectedDateEfetuacao != null) {
                val dataHoraFormatada = obterDataHoraFormatada(selectedDateEfetuacao!!)
                data["data_efetuacao"] = dataHoraFormatada
            }

            // Após selecionar a data, exiba o campo de texto com a data selecionada
            findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.VISIBLE
        }, year, month, dayOfMonth)

        // Exiba o diálogo seletor de data.
        datePickerDialog.show()
    }
    private fun registrarReceita() {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário
        val prefsFileName2 = "Prefs2_$userId"
        sharedPreferences = getSharedPreferences(prefsFileName2, MODE_PRIVATE)

        // Obter referências para os campos de entrada
        val valorDaReceitaEditText = findViewById<EditText>(R.id.valor_da_receita)
        val descricaoDaReceitaEditText = findViewById<EditText>(R.id.Descricao_da_receita)
        val nomeFavorecido = autoCompleteTextViewFavorecidoReceita.text.toString()

        // Obter os valores inseridos pelo usuário
        val valorDaReceita = valorDaReceitaEditText.text.toString()
        val nomeDaReceita = autoCompleteTextViewReceita.text.toString()
        val categoriaescolhida = DadosCategoriasTemporarios.categoriaSelecionada

        // Verificar se o valor da receita foi inserido
        if (TextUtils.isEmpty(valorDaReceita)) {
            // Mostrar um Toast para informar ao usuário que ele precisa inserir o valor da receita
            Toast.makeText(this, "Por favor, insira o valor da receita.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem o valor da receita
        }

        // Converter o valor da receita de String para Double
        val formattedValue: Double = try {
            valorDaReceita.replace("R", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Insira um valor válido para a receita", Toast.LENGTH_SHORT).show()
            return // Encerra o método se o valor não for válido
        }

        // Verificar se o valor é maior que zero
        if (formattedValue <= 0) {
            Toast.makeText(this, "Insira um valor válido para a receita", Toast.LENGTH_SHORT).show()
            return // Encerra o método se o valor não for válido
        }

        // Verificar se o nome da receita foi inserido
        if (TextUtils.isEmpty(nomeDaReceita)) {
            // Mostrar um Toast para informar ao usuário que ele precisa inserir o nome da receita
            Toast.makeText(this, "Por favor, insira o nome da receita.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem o nome da receita
        }

        // Verificar se a data de efetuação foi selecionada
        if (selectedDateEfetuacao == null) {
            // Mostrar um Toast para informar ao usuário que ele precisa selecionar a data de efetuação
            Toast.makeText(this, "Por favor, selecione a data de efetuação.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem a data de efetuação
        }

        // Verificar se a categoria foi selecionada
        if (!categoriaSelecionada) {
            // Mostrar um Toast para informar ao usuário que ele precisa selecionar a categoria
            Toast.makeText(this, "Por favor, selecione a categoria.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem a categoria
        }
        // Verificar se a categoria foi selecionada
        if (!categoriaSelecionada) {
            Toast.makeText(this, "Por favor, selecione a categoria.", Toast.LENGTH_SHORT).show()
            return // Encerra o método para evitar o registro sem a categoria
        }

        // Se todas as validações passarem, você pode prosseguir com o registro da receita
        // Aqui você pode adicionar o código para salvar os dados da receita no banco de dados ou realizar outras ações necessárias.
        // Exemplo de como salvar os dados em um HashMap:
        val iconeResId = intent.getIntExtra("icone_res_id", 0)
        val corImagemRedonda = intent.getIntExtra("cor_imagem_redonda", 0)
        val corCirculo = intent.getIntExtra("circulo_cor", 0)

        // Exemplo de como enviar os dados para o Firestore (banco de dados):
        val firestore = FirebaseFirestore.getInstance()
        val novaReceitaRef = firestore.collection("receitas").document() // Cria uma referência com ID automático
        val novaReceitaId = novaReceitaRef.id // ID exclusivo gerado automaticamente


        val timestamp = selectedDateEfetuacao?.let { Timestamp(it.time) }
        // Criar um mapa com os dados a serem salvos no Firestore
        val data = hashMapOf(
        "valor_da_receita" to formattedValue, // Salve o valor como Double no Firestore
        "nome_da_receita" to nomeDaReceita,
        "descricao_da_receita" to descricaoDaReceitaEditText.text.toString(),
        "categoria_escolhida_pelo_usuario_receitas" to categoriaescolhida, // Adicione a categoria ao HashMap
            "data_efetuacao_receitas" to timestamp,
            "nome_favorecido_receita" to nomeFavorecido,
            "iconeReceita" to iconeResId,
            "corImagemReceita" to corImagemRedonda,
            "corCirculoReceita" to corCirculo,
            "receita_id" to novaReceitaId
        )

        // Verificar se é uma despesa fixa e adicionar o valor da despesa fixa ao mapa de dados
        data["recebido_ou_nao"] = recebidoOuNaoRecebido

        // Adicione o ID do usuário ao HashMap
        if (!userId.isNullOrEmpty()) {
            data["userId"] = userId
        }

        novaReceitaRef.set(data)
            .addOnSuccessListener {
                // Registro da receita concluído com sucesso.
                Toast.makeText(this, "Receita registrada com sucesso!", Toast.LENGTH_SHORT).show()
                // Limpar os campos de entrada após o registro bem-sucedido, se desejado.
                valorDaReceitaEditText.text.clear()
                autoCompleteTextViewReceita.text.clear()
                descricaoDaReceitaEditText.text.clear()
                autoCompleteTextViewFavorecidoReceita.text.clear()
                // Reinicializar a data selecionada para evitar o registro duplicado.
                selectedDateEfetuacao = null
                // Atualizar a visibilidade do botão "Limpar Data" (caso o usuário tenha registrado a receita sem a data de efetuação).
                findViewById<Button>(R.id.limpar_data_button).visibility = View.GONE
                // Atualizar a visibilidade do TextView da data de efetuação.
                findViewById<TextView>(R.id.data_efetuacao_textview).visibility = View.GONE
                toggleSwitch.isChecked = false

                // Limpar os valores armazenados no SharedPreferences após o registro bem-sucedido
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                abrirTelaPrincipal()
            }
            .addOnFailureListener {
                // Ocorreu um erro durante o registro da receita.
                Toast.makeText(this, "Erro ao registrar a receita. Por favor, tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun carregarNomesReceitasDoUsuario() {
        Log.d("DespesasApp", "Iniciando carregarNomesDespesasDoUsuario")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Crie um nome de arquivo exclusivo para o SharedPreferences do usuário

        if (userId != null) {
            val receitasRef = FirebaseFirestore.getInstance()
                .collection("receitas")
                .whereEqualTo("userId", userId)

            receitasRef.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DespesasApp", "Sucesso ao obter dados do Firestore")

                    val nomesReceitasList = mutableListOf<String>()
                    val nomesReceitasSet = mutableSetOf<String>() // Usar um conjunto para armazenar nomes únicos

                    for (document in querySnapshot) {
                        val nomeReceita = document.getString("nome_da_receita")
                        nomeReceita?.let {
                            nomesReceitasSet.add(it) // Adicionar ao conjunto para garantir nomes únicos
                        }
                    }

                    nomesReceitasList.addAll(nomesReceitasSet.toList()) // Converter o conjunto de volta para uma lista

                    val textInputLayout: TextInputLayout = findViewById(R.id.Nome_da_receita)
                    val autoCompleteTextView = textInputLayout.editText as? AutoCompleteTextView

                    if (autoCompleteTextView != null) {
                        autoCompleteTextView?.let {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomesReceitasList)
                            it.setAdapter(adapter)

                            it.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    Log.d("DespesasApp", "Texto alterado: $s")

                                    val filtro = nomesReceitasList.filter { it.contains(s.toString(), ignoreCase = true) }

                                    val adapter = ArrayAdapter(this@activity_tela_principal_categorias_receitas, android.R.layout.simple_dropdown_item_1line, filtro)
                                    autoCompleteTextView.setAdapter(adapter)

                                    val editor = sharedPreferences.edit()
                                    editor.putString("nome_receita", s.toString())
                                    editor.apply()
                                }

                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    } else {
                        Log.e("DespesasApp", "autoCompleteTextView não foi encontrado na layout")
                        // Lide com a situação quando o autoCompleteTextView não for encontrado
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DespesasApp", "Falha ao obter dados do Firestore: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar os nomes das receitas do usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun carregarNomesFavorecidosReceitasDoUsuario() {
        Log.d("DespesasApp", "Iniciando carregarNomesFavorecidosDoUsuario")

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val favorecidosRef = FirebaseFirestore.getInstance()
                .collection("receitas")
                .whereEqualTo("userId", userId)

            favorecidosRef.get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DespesasApp", "Sucesso ao obter dados do Firestore para favorecidos")

                    val nomesFavorecidosList = mutableListOf<String>()
                    val nomesFavorecidosSet = mutableSetOf<String>() // Usar um conjunto para armazenar nomes únicos

                    for (document in querySnapshot) {
                        val nomeFavorecido = document.getString("nome_favorecido_receita")
                        nomeFavorecido?.let {
                            nomesFavorecidosSet.add(it) // Adicionar ao conjunto para garantir nomes únicos
                        }
                    }

                    nomesFavorecidosList.addAll(nomesFavorecidosSet.toList()) // Converter o conjunto de volta para uma lista

                    val textInputLayout3: TextInputLayout = findViewById(R.id.Nome_do_favorecido_receita)
                    val autoCompleteTextViewFavorecidoReceita = textInputLayout3.editText as? AutoCompleteTextView

                    if (autoCompleteTextViewFavorecidoReceita != null) {
                        autoCompleteTextViewFavorecidoReceita?.let {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomesFavorecidosList)
                            it.setAdapter(adapter)

                            it.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(d: CharSequence?, start: Int, count: Int, after: Int) {}

                                override fun onTextChanged(d: CharSequence?, start: Int, before: Int, count: Int) {
                                    Log.d("DespesasApp", "Texto alterado: $d")

                                    val filtro = nomesFavorecidosList.filter { it.contains(d.toString(), ignoreCase = true) }

                                    val adapter = ArrayAdapter(this@activity_tela_principal_categorias_receitas, android.R.layout.simple_dropdown_item_1line, filtro)
                                    autoCompleteTextViewFavorecidoReceita.setAdapter(adapter)

                                    val editor = sharedPreferences.edit()
                                    editor.putString("nome_do_favorecido", d.toString())
                                    editor.apply()
                                }

                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    } else {
                        Log.e("DespesasApp", "autoCompleteTextView não foi encontrado no layout")
                        // Lide com a situação quando o autoCompleteTextView não for encontrado
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DespesasApp", "Falha ao obter dados do Firestore para favorecidos: ${e.message}")
                    Toast.makeText(this, "Falha ao carregar os nomes dos favorecidos do usuário: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}