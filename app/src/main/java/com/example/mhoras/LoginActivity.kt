package com.example.mhoras

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var imageView2: ImageView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialização dos componentes
        auth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        imageView2 = findViewById(R.id.imageView2)

        imageView2.setImageResource(R.drawable.ic_launcher_foreground)

        // Verifica se o usuário já está logado
        if (auth.currentUser != null) {
            redirecionarParaMain()
        }

        // Botão para redirecionar para a tela de cadastro
        registerButton.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // Botão para login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                mostrarToast("Preencha todos os campos")
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    /**
     * Realiza o login do usuário usando Firebase Authentication.
     */
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    buscarNomeUsuario(auth.currentUser?.uid.orEmpty())
                } else {
                    mostrarToast("Falha no login: ${task.exception?.localizedMessage}")
                }
            }
    }

    /**
     * Busca o nome do usuário no Firebase Realtime Database e redireciona para a MainActivity.
     */
    private fun buscarNomeUsuario(userId: String) {
        if (userId.isEmpty()) {
            mostrarToast("Erro: ID do usuário não encontrado.")
            return
        }

        val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios")
        usuariosRef.child(userId).child("nome").get().addOnSuccessListener { dataSnapshot ->
            val nome = dataSnapshot.getValue(String::class.java)

            if (!nome.isNullOrEmpty()) {
                mostrarToast("Bem-vindo, $nome!")
            } else {
                mostrarToast("Nome de usuário não encontrado.")
            }
            redirecionarParaMain()
        }
            .addOnFailureListener {
            mostrarToast("Erro ao buscar o nome do usuário: ${it.message}")
        }
    }

    /**
     * Redireciona para a MainActivity após o login.
     */
    private fun redirecionarParaMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Garante que o usuário não possa voltar para a tela de login
    }

    /**
     * Exibe mensagens curtas para o usuário.
     */
    private fun mostrarToast(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
}





