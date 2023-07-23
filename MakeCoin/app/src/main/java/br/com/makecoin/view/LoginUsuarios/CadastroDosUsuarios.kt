package br.com.makecoin.view.LoginUsuarios

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import br.com.makecoin.R
import br.com.makecoin.view.CadastroUsuarios.CadastroUsuarios
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroDosUsuarios : AppCompatActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var confirmarSenhaEditText: EditText

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_dos_usuarios)

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        nomeEditText = findViewById(R.id.edit_nome_cadastro)
        emailEditText = findViewById(R.id.edit_email_cadastro)
        senhaEditText = findViewById(R.id.edit_senha_cadastro)
        confirmarSenhaEditText = findViewById(R.id.edit_confirmar_senha_cadastro)

        val btn_cadastrar_cadastro: Button = findViewById(R.id.btn_cadastrar_cadastro)
        btn_cadastrar_cadastro.setOnClickListener {
            val nomeCompleto = nomeEditText.text.toString().trim()
            val primeiroNome = extrairPrimeiroNome(nomeCompleto)
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()
            val confirmar = confirmarSenhaEditText.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty() || nomeCompleto.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else if (senha != confirmar) {
                Toast.makeText(this, "Senha diferente, confirme novamente!", Toast.LENGTH_SHORT).show()
            } else {

                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { cadastro ->
                        if (cadastro.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid ?: ""

                            // Salvar o nome do usuário no Firestore
                            val userData = hashMapOf(
                                "nome" to nomeCompleto
                            )
                            db.collection("usuario")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                    nomeEditText.setText("")
                                    emailEditText.setText("")
                                    senhaEditText.setText("")
                                    confirmarSenhaEditText.setText("")

                                    // Salvar o nome do usuário nos SharedPreferences
                                    val editor = sharedPreferences.edit()
                                    editor.putString("nomeUsuario", primeiroNome)
                                    editor.putString("nomeCompletoUsuario", nomeCompleto)
                                    editor.apply()

                                    abrirLogin()
                        }.addOnFailureListener { exception ->
                                    Toast.makeText(this, "Erro ao cadastrar usuário!", Toast.LENGTH_SHORT).show()
                                    // Remover o usuário criado no Firebase Authentication
                                    user?.delete()
                                }
                        } else {
                            Toast.makeText(this, "Erro ao cadastrar usuário!", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exeption ->
                        val mensagemErro = when (exeption) {
                            is FirebaseAuthWeakPasswordException -> "Digite uma senha que possua no mínimo 6 digitos!"
                            is FirebaseAuthInvalidCredentialsException -> "Digite um e-mail valido!"
                            is FirebaseAuthUserCollisionException -> "Ja possui uma conta cadastrada!"
                            is FirebaseNetworkException -> "Sem conexão com a internet"
                            else -> "Erro ao cadastrar usuário!"
                        }
                        Toast.makeText(this, mensagemErro, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun extrairPrimeiroNome(nomeCompleto: String): String {
        val nomeParts = nomeCompleto.split(" ")
        return if (nomeParts.isNotEmpty()) nomeParts[0] else ""
    }

    private fun abrirLogin() {
        val intent = Intent(this, CadastroUsuarios::class.java)
        startActivity(intent)
    }
}
