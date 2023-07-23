package br.com.makecoin.view.CadastroUsuarios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import br.com.makecoin.R
import br.com.makecoin.view.LoginUsuarios.CadastroDosUsuarios
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalGraficos.TelaPrincipalGraficos
import com.google.firebase.auth.FirebaseAuth

class CadastroUsuarios : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuarios)


        // Configurar o listener do botão
        val btn_cadastrar: Button = findViewById(R.id.btn_cadastrar)
        btn_cadastrar.setOnClickListener {
            abrirTelaCadastro()
        }

        emailEditText = findViewById(R.id.edit_email)
        senhaEditText = findViewById(R.id.edit_senha)

        val btn_entrar : Button = findViewById(R.id.btn_entrar)
        btn_entrar.setOnClickListener{
            val email = emailEditText.text.toString().trim()
            val senha = senhaEditText.text.toString().trim()

            if(email.isEmpty() || senha.isEmpty()){
                Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else{
                auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener{autenticacao ->
                    if(autenticacao.isSuccessful){
                        abrirTelaPrincipal()
                        emailEditText.setText("")
                        senhaEditText.setText("")
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Erro ao fazer login do usuário!", Toast.LENGTH_SHORT).show()
                    emailEditText.setText("")
                    senhaEditText.setText("")
                }
            }
        }
    }
    // Função para abrir a tela de cadastro
    private fun abrirTelaCadastro() {
        val intent = Intent(this, CadastroDosUsuarios::class.java)
        startActivity(intent)
    }
    private fun abrirTelaPrincipal(){
        val intent = Intent(this, TelaPrincipal::class.java)
        startActivity(intent)
    }

}