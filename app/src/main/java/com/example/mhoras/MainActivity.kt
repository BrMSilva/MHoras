package com.example.mhoras

import Registro
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvDateTime: TextView
    private lateinit var imageView: ImageView
    private lateinit var tvNome: TextView
    private lateinit var etLocal: EditText
    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnEnd: Button
    private lateinit var rvLastRecords: RecyclerView
    private lateinit var btnRegistros: Button
    private lateinit var chronometer: Chronometer

    private var isRunning = false
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private val registros = mutableListOf<Registro>()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val MAX_REGISTROS = 7

    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        tvDateTime = findViewById(R.id.tvDateTime)
        imageView = findViewById(R.id.imageView)
        tvNome = findViewById(R.id.tvNome)
        etLocal = findViewById(R.id.etLocal)
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnEnd = findViewById(R.id.btnEnd)
        rvLastRecords = findViewById(R.id.rvLastRecords)
        btnRegistros = findViewById(R.id.btnRegistros)
        chronometer = findViewById(R.id.chronometer)

        setupRecyclerView()
        setupDatePickers()
        setupTimePicker(etStartTime)
        setupTimePicker(etEndTime)
        updateCurrentDateTime()
        fetchUserName()
        fetchLastRecords()

        btnStart.setOnClickListener { startRegistro() }
        btnPause.setOnClickListener { pauseRegistro() }
        btnEnd.setOnClickListener { endRegistro() }
    }

    private fun setupRecyclerView() {
        rvLastRecords.layoutManager = LinearLayoutManager(this)
        rvLastRecords.setHasFixedSize(true)
        rvLastRecords.isNestedScrollingEnabled = false
        rvLastRecords.adapter = RegistrosAdapter(registros)
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        etDate.setOnClickListener {
            DatePickerDialog(
                this, { _, year, month, dayOfMonth ->
                    etDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        editText.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                editText.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun updateCurrentDateTime() {
        tvDateTime.text = "Data e Hora: ${dateTimeFormatter.format(LocalDateTime.now())}"
        imageView.setImageResource(R.drawable.ic_launcher_foreground)
    }

    private fun fetchUserName() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvNome.text = "Erro: Usuário não autenticado"
            mostrarToast("Usuário não autenticado. Faça login novamente.")
            return
        }
        val userId = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("usuario").child(userId)
        userRef.child("nome").get()
            .addOnSuccessListener { snapshot ->
                val nome = snapshot.getValue(String::class.java)
                if (!nome.isNullOrEmpty()) {
                    tvNome.text = "Bem-vindo, $nome"
                } else {
                    tvNome.text = "Erro: Nome não encontrado"
                    mostrarToast("Erro: Nome não encontrado")
                }
            }
            .addOnFailureListener { error ->
                tvNome.text = "Erro ao carregar nome"
                mostrarToast("Erro ao buscar nome: ${error.message}")
            }
    }

    private fun fetchLastRecords() {
        val registrosRef = FirebaseDatabase.getInstance().getReference("registros")
        registrosRef.orderByKey().limitToLast(7).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                registros.clear()
                for (registroSnapshot in snapshot.children) {
                    val registro = registroSnapshot.getValue(Registro::class.java)
                    if (registro != null) registros.add(0, registro)
                }
                rvLastRecords.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarToast("Erro ao carregar registros.")
            }
        })
    }

    private fun startRegistro() {
        if (etLocal.text.isEmpty()) {
            mostrarToast("Digite o local de trabalho!")
            return
        }

        if (!isRunning) {
            val dateInput = etDate.text.toString()
            val timeInput = etStartTime.text.toString()
            val timeInput2 = etEndTime.text.toString()

            if (dateInput.isNotEmpty() && timeInput.isNotEmpty() && timeInput2.isNotEmpty()) {
                val parsedStartTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("$dateInput $timeInput")
                val parsedEndTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse("$dateInput $timeInput2")

                if (parsedStartTime == null || parsedEndTime == null || parsedStartTime.after(parsedEndTime)) {
                    mostrarToast("Data e hora inválidas ou horário inicial posterior ao final!")
                    return
                }

                startTime = parsedStartTime.time
                val currentTime = System.currentTimeMillis()
                val elapsedMillis = currentTime - startTime

                if (elapsedMillis >= 0) {
                    chronometer.base = SystemClock.elapsedRealtime() - elapsedMillis
                } else {
                    chronometer.base = SystemClock.elapsedRealtime() + (-elapsedMillis)
                }

                chronometer.start()
                isRunning = true
                mostrarToast("Cronômetro iniciado!")
            } else {
                mostrarToast("Informe a data e hora para iniciar o cronômetro!")
                return
            }
        }
    }

    private fun pauseRegistro() {
        if (isRunning) {
            pausedTime = SystemClock.elapsedRealtime() - chronometer.base
            chronometer.stop()
            isRunning = false
            mostrarToast("Cronômetro pausado.")
        } else {
            mostrarToast("Nenhum cronômetro em execução.")
        }
    }

    @RequiresApi(35)
    private fun endRegistro() {
        if (!isRunning && startTime == 0L) {
            mostrarToast("Nenhum registro para finalizar.")
            return
        }

        if (isRunning) {
            chronometer.stop()
            isRunning = false
        }

        val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        val totalMinutes = (elapsedMillis / 60000).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val formattedDuration = String.format("%02d:%02d", hours, minutes)

        // Obtém as informações fornecidas pelo usuário
        val nomeUsuario = tvNome.text.toString()
        val local = etLocal.text.toString()
        val data = etDate.text.toString()
        val horaInicio = etStartTime.text.toString()
        val horaFim = etEndTime.text.toString()
        val total = chronometer.stop()

        val registro = Registro(
            nomeUsuario = nomeUsuario,
            local = local,
            data = data,
            horaInicio = horaInicio,
            horaFim = horaFim,

        )

        if (registros.size >= MAX_REGISTROS) {
            registros.removeLast()
            rvLastRecords.adapter?.notifyItemRemoved(registros.size)
        }

        registros.add(0, registro)
        rvLastRecords.adapter?.notifyItemInserted(0)

        salvarRegistro(registro)

        etLocal.text.clear()
        etDate.text.clear()
        etStartTime.text.clear()
        etEndTime.text.clear()
        startTime = 0

        mostrarToast("Registro finalizado e salvo!")
    }

    private fun salvarRegistro(registro: Registro) {
        val registrosRef = FirebaseDatabase.getInstance().getReference("registros")
        registrosRef.push().setValue(registro).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mostrarToast("Registro salvo no Firebase!")
            } else {
                mostrarToast("Falha ao salvar registro.")
            }
        }
    }

    private fun mostrarToast(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }
}




