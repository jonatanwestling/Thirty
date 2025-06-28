package se.umu.c22jwg.thirty.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import se.umu.c22jwg.thirty.R

class StartFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startButton: View = view.findViewById(R.id.start_button)
        startButton.setOnClickListener {
            // Navigate to the game fragment
            findNavController().navigate(R.id.action_startFragment_to_gameFragment)
        }
        
    }
}