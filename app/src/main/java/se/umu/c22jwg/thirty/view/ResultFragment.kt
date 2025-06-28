package se.umu.c22jwg.thirty.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import se.umu.c22jwg.thirty.R
import se.umu.c22jwg.thirty.databinding.FragmentResultBinding
import se.umu.c22jwg.thirty.viewmodel.GameViewModel

class ResultFragment: Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel : GameViewModel by activityViewModels()
        // Initialize UI components
        val startButton: View = view.findViewById(R.id.start_button)
        startButton.setOnClickListener {
            // Navigate to the main menu and clear the back stack
            findNavController().navigate(
                R.id.action_resultFragment_to_startFragment2,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.startFragment, inclusive = true)
                    .build()
            )
            viewModel.gameOver();

        }

        // call the viewmodel to get the data here
        val scoreList = viewModel.getRoundResults();
        val choiceTextViews = listOf(
            binding.choice1, binding.choice2, binding.choice3, binding.choice4, binding.choice5,
            binding.choice6, binding.choice7, binding.choice8, binding.choice9, binding.choice10
        )

        val scoreTextViews = listOf(
            binding.score1, binding.score2, binding.score3, binding.score4, binding.score5,
            binding.score6, binding.score7, binding.score8, binding.score9, binding.score10
        )

        for (i in scoreList.indices) {
            val (choice, score) = scoreList[i]
            choiceTextViews[i].text = choice
            scoreTextViews[i].text = score.toString()
        }

        // Fill in total score
        binding.totalScore.text = viewModel.getTotalScore().toString()


    }
}