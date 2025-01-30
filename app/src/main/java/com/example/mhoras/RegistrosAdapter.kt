package com.example.mhoras

import Registro
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RegistrosAdapter(private val registros: List<Registro>) :
    RecyclerView.Adapter<RegistrosAdapter.RegistroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registro, parent, false)
        return RegistroViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        val registro = registros[position]
        holder.bind(registro)
    }

    override fun getItemCount(): Int = registros.size

    class RegistroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        private val tvLocal: TextView = itemView.findViewById(R.id.tvLocal)
        private val tvData: TextView = itemView.findViewById(R.id.tvData)
        private val tvHoraInicio: TextView = itemView.findViewById(R.id.tvHoraInicio)
        private val tvHoraFim: TextView = itemView.findViewById(R.id.tvHoraFim)
        private val tvDuracao: TextView = itemView.findViewById(R.id.tvDuracao)

        fun bind(registro: Registro) {
            tvUsuario.text = "Usuário: ${registro.nomeUsuario ?: "Desconhecido"}"
            tvLocal.text = "Local: ${registro.local ?: "Não informado"}"
            tvData.text = "Data: ${registro.data ?: "Sem data"}"
            tvHoraInicio.text = "Início: ${registro.horaInicio ?: "Sem horário"}"
            tvHoraFim.text = "Fim: ${registro.horaFim ?: "Sem horário"}"
            tvDuracao.text = "Duração: ${registro.duracao ?: "Sem duração"}"
        }
    }
}



