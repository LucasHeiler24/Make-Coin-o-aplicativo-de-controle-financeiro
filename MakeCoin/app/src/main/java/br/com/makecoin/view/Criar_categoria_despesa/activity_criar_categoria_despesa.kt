package br.com.makecoin.view.Criar_categoria_despesa

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import br.com.makecoin.R
import br.com.makecoin.view.TelaDasCategorias.activity_tela_das_categorias_despesas
import br.com.makecoin.view.telaDeTodosRegistros.activity_tela_de_todos_os_registros
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.checkerframework.common.returnsreceiver.qual.This

class activity_criar_categoria_despesa : AppCompatActivity() {

    private var selectedColor: Int = Color.BLACK // Cor padrão
    private lateinit var viewCorSelecionada: View // Declare a variável aqui
    private lateinit var categoriaEditText: EditText
    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_categoria_despesa)

        categoriaEditText = findViewById(R.id.nomeCategoria)
        viewCorSelecionada = findViewById(R.id.view_cor_selecionada)

        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        btnConfirmar.setOnClickListener {
            val nomeCategoria = categoriaEditText.text.toString()

            // Verificar se o campo de nome não está vazio e a cor foi selecionada
            if (nomeCategoria.isNotBlank() && selectedColor != Color.BLACK) {
                verificarExistenciaCategoria(nomeCategoria)

            } else {
                // Exibir mensagem de erro se o campo estiver vazio ou a cor não foi selecionada
                Toast.makeText(this,"Por favor, escreva o nome da categoria e a cor desejada!", Toast.LENGTH_LONG).show()
            }
        }

        seekBarRed = findViewById(R.id.seek_bar_red)
        seekBarGreen = findViewById(R.id.seek_bar_green)
        seekBarBlue = findViewById(R.id.seek_bar_blue)

        seekBarRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateColor()
                updateSeekBarColor(seekBar)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateColor()
                updateSeekBarColor(seekBar)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateColor()
                updateSeekBarColor(seekBar)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }
    private fun updateSeekBarColor(seekBar: SeekBar) {
        val progress = seekBar.progress
        val color = Color.rgb(progress, 255 - progress, 0)
        seekBar.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
    private fun updateColor() {
        val red = seekBarRed.progress
        val green = seekBarGreen.progress
        val blue = seekBarBlue.progress

        selectedColor = Color.rgb(red, green, blue)

        // Aplica a cor selecionada como tintura na imagem
        val imagemRedonda = findViewById<ImageView>(R.id.view_cor_selecionada)
        val drawable = ContextCompat.getDrawable(this, R.drawable.circle_background)

        drawable?.let {
            it.mutate()  // Necessário para evitar que a tintura afete outras instâncias da mesma imagem
            DrawableCompat.setTint(it, selectedColor)
            imagemRedonda.setImageDrawable(it)
        }
    }

    private fun abrirTelaRegistros(){
        val intent = Intent(this, activity_tela_das_categorias_despesas::class.java)
        startActivity(intent)
    }
    private fun salvarCategoriaNoFirestore(nomeCategoria: String, corCategoria: Int) {
        // Obtenha o ID do usuário logado
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Crie um mapa (HashMap) para armazenar os dados da categoria
        val categoriaData = hashMapOf(
            "categoria_criada_pelo_usuario" to nomeCategoria,
            "cor_criada_pelo_usuario" to corCategoria,
            "usuario_da_categoria" to userId
        )

        // Acesse o Firestore e insira os dados na coleção desejada
        val db = FirebaseFirestore.getInstance()
        db.collection("categorias")  // Substitua pelo nome da sua coleção no Firestore
            .add(categoriaData)  // Adicione os dados da categoria
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Categoria criada com sucesso!")

                // Se desejar, você pode exibir uma mensagem de sucesso ou fazer outra ação aqui
                abrirTelaRegistros()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao adicionar categoria", e)
                // Se ocorrer um erro, você pode exibir uma mensagem de erro ou fazer outra ação aqui
            }
    }
    private fun verificarExistenciaCategoria(nomeCategoria: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "Verificando existência da categoria: $nomeCategoria")

        val nomeCategoriaSemEspacos = nomeCategoria.trim()

        // Função para verificar a existência da categoria em "categorias_despesas"
        db.collection("categorias").document("categorias_despesas")
            .get()
            .addOnSuccessListener { documentSnapshotDespesas ->
                if (documentSnapshotDespesas.exists()) {
                    val categoriasDespesas = documentSnapshotDespesas.data

                    // Verifica se a categoria fornecida está entre as categorias predefinidas
                    if (categoriasDespesas != null && categoriasDespesas.containsValue(nomeCategoriaSemEspacos)) {
                        Toast.makeText(this, "Categoria já existe, escolha outro nome.", Toast.LENGTH_LONG).show()
                    } else {
                        // Verifica se a categoria já existe para o usuário atual
                        db.collection("categorias")
                            .whereEqualTo("categoria_criada_pelo_usuario", nomeCategoriaSemEspacos)
                            .whereEqualTo("usuario_da_categoria", userId)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    Toast.makeText(this, "Categoria já existe, escolha outro nome.", Toast.LENGTH_LONG).show()
                                } else {
                                    // Categoria não existe, salvar no Firebase
                                    salvarCategoriaNoFirestore(nomeCategoriaSemEspacos, selectedColor)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Erro ao verificar categoria de usuário", e)
                            }
                    }
                } else {
                    Log.d(TAG, "Documento 'categorias_despesas' não existe")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao verificar categoria de despesas", e)
            }
    }
}