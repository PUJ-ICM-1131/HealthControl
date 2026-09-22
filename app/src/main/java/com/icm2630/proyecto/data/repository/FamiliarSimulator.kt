package com.icm2630.proyecto.data.repository

import com.icm2630.proyecto.data.model.Cita
import com.icm2630.proyecto.data.model.FormaMedicamento
import com.icm2630.proyecto.data.model.Genero
import com.icm2630.proyecto.data.model.Medicamento
import com.icm2630.proyecto.data.model.ModalidadCita
import com.icm2630.proyecto.data.model.PerfilUsuario
import com.icm2630.proyecto.data.model.PersonaVinculada
import com.icm2630.proyecto.data.model.TipoCita
import com.icm2630.proyecto.data.model.TipoSangre
import java.util.Calendar
import java.util.TimeZone

/**
 * No hay backend todavía: simula el HealthControl de cada familiar
 * (perfil, medicamentos y citas) para poder consultarlo en solo lectura.
 *
 * Cada código de familiar recibe siempre la misma plantilla, así cada
 * persona agregada muestra información distinta pero estable.
 */
object FamiliarSimulator {

    private const val UN_DIA_MILLIS = 24L * 60L * 60L * 1000L

    private val codigosConDatos = mutableSetOf<String>()

    private data class PlantillaFamiliar(
        val edadAnios: Int,
        val tipoSangre: TipoSangre,
        val alergias: String,
        val condiciones: List<String>,
        val medicamentos: (codigo: String, hoy: Long) -> List<Medicamento>,
        val citas: (codigo: String, hoy: Long) -> List<Cita>
    )

    private val PLANTILLAS = listOf(

        PlantillaFamiliar(
            edadAnios = 62,
            tipoSangre = TipoSangre.O_POSITIVO,
            alergias = "Penicilina",
            condiciones = listOf("Hipertensión", "Diabetes"),
            medicamentos = { codigo, hoy ->
                listOf(
                    medicamento(codigo, 1, "Metformina", "850", "mg", listOf("7:00 AM", "7:00 PM"), hoy - 400 * UN_DIA_MILLIS, null, "Tomar con las comidas"),
                    medicamento(codigo, 2, "Enalapril", "10", "mg", listOf("8:00 AM"), hoy - 200 * UN_DIA_MILLIS, null, "Controlar la presión antes de tomarlo")
                )
            },
            citas = { codigo, hoy ->
                listOf(
                    cita(codigo, 1, TipoCita.ESPECIALISTA, "Endocrinología", "Control de glucosa", hoy + UN_DIA_MILLIS, 10, 0, ModalidadCita.PRESENCIAL, "Hospital San Ignacio", "Dra. Patricia Rojas"),
                    cita(codigo, 2, TipoCita.EXAMENES, "", "Hemoglobina glicosilada", hoy + 5 * UN_DIA_MILLIS, 7, 0, ModalidadCita.PRESENCIAL, "Laboratorio Colcan", ""),
                    cita(codigo, 3, TipoCita.MEDICINA_GENERAL, "", "Control general", hoy - 20 * UN_DIA_MILLIS, 9, 0, ModalidadCita.PRESENCIAL, "Clínica Colsanitas", "Dr. Hernán Pardo")
                )
            }
        ),

        PlantillaFamiliar(
            edadAnios = 34,
            tipoSangre = TipoSangre.A_POSITIVO,
            alergias = "Ácaros del polvo",
            condiciones = listOf("Asma"),
            medicamentos = { codigo, hoy ->
                listOf(
                    medicamento(codigo, 1, "Montelukast", "10", "mg", listOf("9:00 PM"), hoy - 90 * UN_DIA_MILLIS, null, "Tomar antes de dormir"),
                    medicamento(codigo, 2, "Loratadina", "10", "mg", listOf("8:00 AM"), hoy - 40 * UN_DIA_MILLIS, hoy - 30 * UN_DIA_MILLIS, "Solo durante la crisis alérgica")
                )
            },
            citas = { codigo, hoy ->
                listOf(
                    cita(codigo, 1, TipoCita.ESPECIALISTA, "Neumología", "Revisión de espirometría", hoy + 3 * UN_DIA_MILLIS, 16, 0, ModalidadCita.VIRTUAL, "", "Dr. Felipe Mora"),
                    cita(codigo, 2, TipoCita.ODONTOLOGIA, "", "Limpieza dental", hoy - 45 * UN_DIA_MILLIS, 11, 30, ModalidadCita.PRESENCIAL, "Dentisalud", "Dra. Camila Ruiz")
                )
            }
        ),

        PlantillaFamiliar(
            edadAnios = 45,
            tipoSangre = TipoSangre.B_POSITIVO,
            alergias = "",
            condiciones = listOf("Hipotiroidismo"),
            medicamentos = { codigo, hoy ->
                listOf(
                    medicamento(codigo, 1, "Levotiroxina", "50", "mcg", listOf("6:00 AM"), hoy - 300 * UN_DIA_MILLIS, null, "En ayunas, 30 minutos antes del desayuno"),
                    medicamento(codigo, 2, "Omeprazol", "20", "mg", listOf("7:00 AM"), hoy - 7 * UN_DIA_MILLIS, hoy + 14 * UN_DIA_MILLIS, "Antes del desayuno")
                )
            },
            citas = { codigo, hoy ->
                listOf(
                    cita(codigo, 1, TipoCita.ESPECIALISTA, "Endocrinología", "Ajuste de dosis", hoy, 15, 30, ModalidadCita.VIRTUAL, "", "Dr. Julián Herrera"),
                    cita(codigo, 2, TipoCita.EXAMENES, "", "Perfil tiroideo (TSH)", hoy - 30 * UN_DIA_MILLIS, 7, 30, ModalidadCita.PRESENCIAL, "Laboratorio Synlab", "")
                )
            }
        ),

        PlantillaFamiliar(
            edadAnios = 70,
            tipoSangre = TipoSangre.AB_NEGATIVO,
            alergias = "Ibuprofeno",
            condiciones = listOf("Artritis", "Colesterol alto"),
            medicamentos = { codigo, hoy ->
                listOf(
                    medicamento(codigo, 1, "Atorvastatina", "20", "mg", listOf("9:00 PM"), hoy - 500 * UN_DIA_MILLIS, null, "Tomar en la noche"),
                    medicamento(codigo, 2, "Calcio + Vitamina D", "600", "mg", listOf("1:00 PM"), hoy - 60 * UN_DIA_MILLIS, null, "Después del almuerzo")
                )
            },
            citas = { codigo, hoy ->
                listOf(
                    cita(codigo, 1, TipoCita.ESPECIALISTA, "Reumatología", "Control de artritis", hoy + 2 * UN_DIA_MILLIS, 11, 0, ModalidadCita.PRESENCIAL, "Fundación Santa Fe", "Dr. Ricardo Salinas"),
                    cita(codigo, 2, TipoCita.MEDICINA_GENERAL, "", "Chequeo anual", hoy - 60 * UN_DIA_MILLIS, 8, 0, ModalidadCita.PRESENCIAL, "Clínica del Country", "Dra. Laura Gómez")
                )
            }
        )
    )

    /** Las dos personas de ejemplo del perfil quedan fijas a plantillas distintas. */
    private val PLANTILLA_POR_CODIGO = mapOf(
        "HC4F7K" to 0,
        "HC9M2P" to 1
    )

    private fun plantillaPara(codigo: String): PlantillaFamiliar {
        val indice = PLANTILLA_POR_CODIGO[codigo]
            ?: codigo.sumOf { it.code }.mod(PLANTILLAS.size)
        return PLANTILLAS[indice]
    }

    /** Registra (una sola vez) los medicamentos y citas simulados del familiar. */
    fun asegurarDatos(codigo: String) {
        if (!codigosConDatos.add(codigo)) return

        val plantilla = plantillaPara(codigo)
        val hoy = hoyUtcMillis()

        plantilla.medicamentos(codigo, hoy).forEach { MedicamentoRepository.registrar(it) }
        plantilla.citas(codigo, hoy).forEach { CitaRepository.registrar(it) }
    }

    /** Perfil simulado del familiar, construido a partir de su plantilla. */
    fun perfilDe(persona: PersonaVinculada, contactoEmergencia: String): PerfilUsuario {
        val plantilla = plantillaPara(persona.codigo)

        val nacimiento = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = hoyUtcMillis()
            add(Calendar.YEAR, -plantilla.edadAnios)
        }.timeInMillis

        return PerfilUsuario(
            nombreCompleto = persona.nombre,
            fechaNacimientoMillis = nacimiento,
            genero = generoPorRelacion(persona.relacion),
            contacto = VinculacionSimulator.telefonoParaCodigo(persona.codigo),
            contactoEmergencia = contactoEmergencia,
            tipoSangre = plantilla.tipoSangre,
            alergias = plantilla.alergias,
            condiciones = plantilla.condiciones,
            codigoVinculacion = persona.codigo,
            configurado = true
        )
    }

    private fun generoPorRelacion(relacion: String): Genero {
        val r = relacion.trim().lowercase()
        return when {
            r in listOf("madre", "mamá", "mama", "hermana", "hija", "abuela", "tía", "tia", "esposa", "pareja") -> Genero.FEMENINO
            r in listOf("padre", "papá", "papa", "hermano", "hijo", "abuelo", "tío", "tio", "esposo") -> Genero.MASCULINO
            else -> Genero.SIN_ESPECIFICAR
        }
    }

    private fun medicamento(
        codigo: String,
        n: Int,
        nombre: String,
        dosis: String,
        unidad: String,
        horarios: List<String>,
        inicio: Long,
        fin: Long?,
        indicaciones: String
    ) = Medicamento(
        id = "$codigo-medicamento-$n",
        personaId = null,
        cuentaCodigo = codigo,
        nombre = nombre,
        forma = FormaMedicamento.PASTILLA,
        dosis = dosis,
        unidad = unidad,
        cantidadPorToma = "1",
        horarios = horarios,
        fechaInicioMillis = inicio,
        fechaFinMillis = fin,
        tratamientoPermanente = fin == null,
        indicaciones = indicaciones
    )

    private fun cita(
        codigo: String,
        n: Int,
        tipo: TipoCita,
        especialidad: String,
        motivo: String,
        fecha: Long,
        hora: Int,
        minuto: Int,
        modalidad: ModalidadCita,
        institucion: String,
        medico: String
    ) = Cita(
        id = "$codigo-cita-$n",
        personaId = null,
        cuentaCodigo = codigo,
        tipo = tipo,
        especialidad = especialidad,
        motivo = motivo,
        fechaMillis = fecha,
        hora = hora,
        minuto = minuto,
        modalidad = modalidad,
        institucion = institucion,
        enlaceVirtual = if (modalidad == ModalidadCita.VIRTUAL) "https://meet.google.com/ejemplo" else "",
        nombreMedico = medico
    )

    /** Medianoche de hoy en UTC, igual que las fechas del DatePicker. */
    private fun hoyUtcMillis(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }
}
