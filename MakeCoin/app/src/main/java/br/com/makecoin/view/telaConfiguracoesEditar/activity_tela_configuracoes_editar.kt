package br.com.makecoin.view.telaConfiguracoesEditar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.makecoin.R
import br.com.makecoin.view.CadastroUsuarios.CadastroUsuarios
import br.com.makecoin.view.TelaPrincipal.TelaPrincipal
import br.com.makecoin.view.TelaPrincipalCategorias.TelaPrincipalCategorias
import br.com.makecoin.view.TelaPrincipalGraficos.TelaPrincipalGraficos
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class activity_tela_configuracoes_editar : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val PERMISSION_CODE = 1000
    private val IMAGE_PICK_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private lateinit var profileImageView: ImageView // Variável profileImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_configuracoes_editar)
        storageRef = FirebaseStorage.getInstance().reference

        val editTextEmail = findViewById<TextView>(R.id.editTextEmail)

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            editTextEmail.text = email
        }

        val nomeCompletoTextView1 = findViewById<TextView>(R.id.nomeCompletoTextView1)
        val nomeCompletoTextView2 = findViewById<TextView>(R.id.nomeCompletoTextView2)

        buscarNomeUsuario(nomeCompletoTextView1, nomeCompletoTextView2)

        val editEmailButton = findViewById<ImageView>(R.id.dialog_editar_email)
        editEmailButton.setOnClickListener {
            exibirDialogEditarEmail()
        }

        val editNomeButton = findViewById<ImageView>(R.id.dialog_editar_nome)
        editNomeButton.setOnClickListener {
            exibirDialogEditarNome()
        }

        val editSenhaButton = findViewById<Button>(R.id.senha_editar)
        editSenhaButton.setOnClickListener {
            exibirDialogSenhaAtual()
        }

        profileImageView = findViewById(R.id.profileImageView) // Inicializar a variável profileImageView

        val btnAlterarImagem = findViewById<Button>(R.id.btn_alterar_imagem)
        btnAlterarImagem.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_CODE
                )
            } else {
                openGallery()
            }
        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { id ->
            val imageRef = storageRef.child("perfil/${id}.jpg")
            imageRef.downloadUrl
                .addOnSuccessListener { downloadUri ->
                    // Exibir a imagem no ImageView
                    val profileImageView = findViewById<ImageView>(R.id.profileImageView)
                    Glide.with(this)
                        .load(downloadUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImageView)
                }
                .addOnFailureListener { exception ->
                    // Ocorreu um erro ao obter a URL da imagem do perfil
                   // Toast.makeText(this, "Erro ao obter a imagem do perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

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
    }

    private fun buscarNomeUsuario(nomeCompletoTextView1: TextView, nomeCompletoTextView2: TextView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { id ->
            db.collection("usuario").document(id).get()
                .addOnSuccessListener { documentSnapshot ->
                    val nome = documentSnapshot.getString("nome")
                    nome?.let { nomeUsuario ->
                        nomeCompletoTextView1.text = nomeUsuario
                        nomeCompletoTextView2.text = nomeUsuario
                    }
                }
        }
    }

    private fun exibirDialogEditarEmail() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_email, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Editar Email")
            .setPositiveButton("Salvar") { _, _ ->
                val novoEmailEditText = dialogView.findViewById<EditText>(R.id.novoEmailEditText)
                val novoEmail = novoEmailEditText.text.toString()

                if (novoEmail.isBlank()) {
                    Toast.makeText(this, "O email é obrigatório.", Toast.LENGTH_SHORT).show()
                } else {
                    val user = FirebaseAuth.getInstance().currentUser

                    if (novoEmail == user?.email) {
                        Toast.makeText(this, "O email é igual ao atual.", Toast.LENGTH_SHORT).show()
                    } else {
                        verificarEmailExistente(novoEmail) { emailExistente ->
                            if (emailExistente) {
                                Toast.makeText(this, "O email já está em uso.", Toast.LENGTH_SHORT).show()
                            } else {
                                user?.updateEmail(novoEmail)
                                    ?.addOnSuccessListener {
                                        Toast.makeText(this, "Email atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                        val editTextEmail = findViewById<TextView>(R.id.editTextEmail)
                                        editTextEmail.text = novoEmail
                                    }
                                    ?.addOnFailureListener { exception ->
                                        Toast.makeText(this, "Erro ao atualizar o email: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    private fun verificarEmailExistente(email: String, callback: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    val emailExistente = signInMethods != null && signInMethods.isNotEmpty()
                    callback(emailExistente)
                } else {
                    callback(false)
                }
            }
    }


    private fun exibirDialogEditarNome() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_nome, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Editar Nome")
            .setPositiveButton("Salvar") { _, _ ->
                val novoNomeEditText = dialogView.findViewById<EditText>(R.id.novoNomeEditText)
                val novoNome = novoNomeEditText.text.toString()

                if (validarNome(novoNome)) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    userId?.let { id ->
                        verificarNomeExistente(novoNome, id) { nomeExistente ->
                            if (nomeExistente) {
                                Toast.makeText(this, "O nome já está em uso.", Toast.LENGTH_SHORT).show()
                            } else {
                                val userRef = db.collection("usuario").document(id)
                                userRef.update("nome", novoNome)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Nome atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent()
                                        intent.putExtra("novoNome", novoNome)
                                        setResult(RESULT_OK, intent)
                                        finish()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Erro ao atualizar o nome: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                } else {
                    // O nome não possui um formato válido
                    Toast.makeText(this, "Nome inválido. Insira um nome válido.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun verificarNomeExistente(nome: String, userId: String, callback: (Boolean) -> Unit) {
        val usuariosRef = db.collection("usuario")
        usuariosRef.whereEqualTo("nome", nome)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val nomeExistente = querySnapshot.documents.any { document ->
                    document.id != userId
                }
                callback(nomeExistente)
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }

    private fun validarEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches() && email.endsWith("@gmail.com")
    }

    private fun validarNome(nome: String): Boolean {
        return nome.isNotEmpty()
    }

    private fun exibirDialogSenhaAtual() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_senha_atual, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Digite sua senha atual")
            .setPositiveButton("Confirmar") { _, _ ->
                val senhaAtualEditText = dialogView.findViewById<TextView>(R.id.senha_atual_edit_text)
                val senhaAtual = senhaAtualEditText.text.toString()

                if (senhaAtual.isBlank()) {
                    Toast.makeText(this, "Digite sua senha atual.", Toast.LENGTH_SHORT).show()
                } else {
                    val user = FirebaseAuth.getInstance().currentUser

                    // Comparar a senha atual digitada pelo usuário com a senha armazenada no banco de dados
                    val credential = EmailAuthProvider.getCredential(user?.email ?: "", senhaAtual)
                    user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // A senha atual é válida, prosseguir com a alteração da senha
                            exibirDialogNovaSenha()
                        } else {
                            // A senha atual é inválida
                            Toast.makeText(this, "Senha atual incorreta.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    private fun exibirDialogNovaSenha() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nova_senha, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Digite sua nova senha")
            .setPositiveButton("Confirmar") { _, _ ->
                val novaSenhaEditText = dialogView.findViewById<EditText>(R.id.nova_senha_edit_text)
                val confirmarSenhaEditText = dialogView.findViewById<EditText>(R.id.confirmar_senha_edit_text)

                val novaSenha = novaSenhaEditText.text.toString()
                val confirmarSenha = confirmarSenhaEditText.text.toString()

                if (novaSenha.isNotBlank() && confirmarSenha.isNotBlank()) {
                    if (novaSenha.length >= 6 && novaSenha == confirmarSenha) {
                        // Nova senha atende aos critérios mínimos, prosseguir com a alteração da senha
                        alterarSenha(novaSenha)
                    } else if (novaSenha.length < 6) {
                        // Nova senha não possui o número mínimo de caracteres
                        Toast.makeText(this, "A nova senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Nova senha e confirmação não coincidem
                        Toast.makeText(this, "Confirmação de senha não coincide.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Um ou ambos os campos estão vazios
                    Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }


    private fun alterarSenha(novaSenha: String) {
        val user = FirebaseAuth.getInstance().currentUser

        user?.updatePassword(novaSenha)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Senha alterada com sucesso
                    Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    // Ocorreu um erro ao alterar a senha
                    Toast.makeText(this, "Erro ao alterar a senha.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun salvarURLImagemUsuario(urlImagem: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { id ->
            db.collection("usuario").document(id)
                .update("urlImagem", urlImagem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagem de perfil atualizada com sucesso!", Toast.LENGTH_SHORT).show()

                    val intent = Intent()
                    intent.putExtra("novaUrlImagem", urlImagem) // Passa a nova URL da imagem
                    setResult(RESULT_OK, intent)

                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao atualizar a imagem de perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        super.onBackPressed()
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permissão de acesso ao armazenamento negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val imageUri = data?.data
            val profileImageView = findViewById<ImageView>(R.id.profileImageView) // Adicione essa linha

            // Redimensionar e exibir a imagem selecionada no profileImageView
            val bitmap = getBitmapFromUri(imageUri)
            val resizedBitmap =
                resizeBitmap(bitmap, profileImageView.width, profileImageView.height)
            val roundedBitmap = getRoundedBitmap(resizedBitmap)
            profileImageView.setImageDrawable(BitmapDrawable(resources, roundedBitmap))

            // Verifica se a imagem foi selecionada corretamente
            if (imageUri != null) {
                // Obtenha uma referência única para a imagem no Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("perfil/${FirebaseAuth.getInstance().currentUser?.uid}.jpg")

                // Faça o upload da imagem para o Firebase Storage
                val uploadTask = imageRef.putFile(imageUri)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    // Continua com a tarefa para obter a URL de download da imagem
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        // Salva a URL de download da imagem no banco de dados (Firestore)
                        salvarURLImagemUsuario(downloadUri?.toString())
                    } else {
                        // Ocorreu um erro ao fazer o upload da imagem
                        Toast.makeText(this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        return uri?.let {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap?, width: Int, height: Int): Bitmap? {
        return bitmap?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            scaledBitmap
        }
    }
    private fun getRoundedBitmap(bitmap: Bitmap?): Bitmap? {
        return bitmap?.let {
            val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(outputBitmap)
            val color = -0xbdbdbe
            val paint = android.graphics.Paint()
            val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = android.graphics.RectF(rect)
            val roundPx = bitmap.width.toFloat() * 0.5f
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
            paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            outputBitmap
        }
    }
    fun exibirDialogSairConta(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Deseja mesmo sair da conta?")
            .setPositiveButton("Sim") { dialog, id ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, CadastroUsuarios::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Não", null)
            .create()
            .show()
    }
    private fun salvarImagemSelecionada(imageUri: Uri?) {
        this.imageUri = imageUri

        // Realizar o upload da imagem para o Firebase Storage
        uploadImagemPerfil(imageUri)
    }

    private fun uploadImagemPerfil(imageUri: Uri?) {
        imageUri?.let { uri ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let { id ->
                val imageRef = storageRef.child("perfil/${id}.jpg")
                val uploadTask = imageRef.putFile(uri)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    // Continuar com a tarefa para obter a URL de download da imagem
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        // Salvar a URL de download da imagem no Firestore ou em outro local de sua preferência
                        salvarURLImagemUsuario(downloadUri?.toString())

                        // Exibir a imagem selecionada no ImageView
                        val bitmap = getBitmapFromUri(imageUri)
                        val resizedBitmap = resizeBitmap(bitmap, profileImageView.width, profileImageView.height)
                        val roundedBitmap = getRoundedBitmap(resizedBitmap)
                        profileImageView.setImageBitmap(roundedBitmap)
                    } else {
                        // Ocorreu um erro ao fazer o upload da imagem
                        Toast.makeText(this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
