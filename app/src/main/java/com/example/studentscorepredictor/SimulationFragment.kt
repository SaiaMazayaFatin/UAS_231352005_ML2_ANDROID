package com.example.studentscorepredictor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.studentscorepredictor.databinding.FragmentSimulationBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class SimulationFragment : Fragment() {

    private lateinit var tflite: Interpreter
    private var _binding: FragmentSimulationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSimulationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            tflite = Interpreter(loadModelFile())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal memuat model", Toast.LENGTH_SHORT).show()
        }

        // Hubungkan UI dari view fragment
        val etHoursStudied = binding.etHoursStudied
        val etAttendance = binding.etAttendance
        val spinnerGender = binding.spinnerGender
        val spinnerTutoring = binding.spinnerTutoring
        val spinnerRegion = binding.spinnerRegion
        val spinnerParentEducation = binding.spinnerParentEducation
        val btnPredict = binding.btnPredict
        val tvResult = binding.tvResult

        setupSpinners()

        btnPredict.setOnClickListener {
            try {
                val hoursStudied = etHoursStudied.text.toString().toFloat()
                val attendance = etAttendance.text.toString().toFloat()
                val gender = spinnerGender.selectedItem.toString()
                val tutoring = spinnerTutoring.selectedItem.toString()
                val region = spinnerRegion.selectedItem.toString()
                val parentEducation = spinnerParentEducation.selectedItem.toString()

                val inputBuffer = preprocessInput(hoursStudied, attendance, gender, tutoring, region, parentEducation)
                val outputBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())

                tflite.run(inputBuffer, outputBuffer)

                outputBuffer.rewind()
                val prediction = outputBuffer.asFloatBuffer().get()

                tvResult.text = "Hasil Prediksi: %.1f".format(prediction)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Input tidak valid!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = requireActivity().assets.openFd("model_simulation.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun setupSpinners() {
        val context = requireContext()
        binding.spinnerGender.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("Female", "Male"))
        binding.spinnerTutoring.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("No", "Yes"))
        binding.spinnerRegion.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("Rural", "Urban"))
        binding.spinnerParentEducation.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("None", "Primary", "Secondary", "Tertiary"))
    }

    private fun preprocessInput(
        hoursStudied: Float, attendance: Float, gender: String,
        tutoring: String, region: String, parentEducation: String
    ): ByteBuffer {
        val inputArray = FloatArray(11)
        val hoursMean = 9.8472f
        val hoursStd = 3.6707f
        val attendanceMean = 75.3959f
        val attendanceStd = 14.4408f

        inputArray[0] = (hoursStudied - hoursMean) / hoursStd
        inputArray[1] = (attendance - attendanceMean) / attendanceStd
        inputArray[2] = if (gender == "Female") 1.0f else 0.0f
        inputArray[3] = if (gender == "Male") 1.0f else 0.0f
        inputArray[4] = if (tutoring == "No") 1.0f else 0.0f
        inputArray[5] = if (tutoring == "Yes") 1.0f else 0.0f
        inputArray[6] = if (region == "Rural") 1.0f else 0.0f
        inputArray[7] = if (region == "Urban") 1.0f else 0.0f
        inputArray[8] = if (parentEducation == "Primary") 1.0f else 0.0f
        inputArray[9] = if (parentEducation == "Secondary") 1.0f else 0.0f
        inputArray[10] = if (parentEducation == "Tertiary") 1.0f else 0.0f

        val byteBuffer = ByteBuffer.allocateDirect(11 * 4).order(ByteOrder.nativeOrder())
        byteBuffer.asFloatBuffer().put(inputArray)
        return byteBuffer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}