package se.umu.c22jwg.thirty.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import se.umu.c22jwg.thirty.R
import se.umu.c22jwg.thirty.databinding.FragmentGameBinding
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.viewmodel.GameViewModel
import androidx.navigation.fragment.findNavController

class GameFragment : Fragment() {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by viewModels()
    var dieSelectionEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe dice changes
        viewModel.dice.observe(viewLifecycleOwner) { diceList ->
            updateDiceImages(diceList)
        }
        // Observe round changes
        viewModel.round.observe(viewLifecycleOwner) { round ->
            binding.RoundNum?.text = getString(R.string.round_text, round);
        }
        // Observe roll changes
        viewModel.roll.observe(viewLifecycleOwner) { roll ->
            binding.RollNum?.text = getString(R.string.roll_text, roll);
        }
        viewModel.rollButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.rollButton?.isEnabled = enabled
        }


        viewModel.isDieSelectionEnabled.observe(viewLifecycleOwner) {
            dieSelectionEnabled = it
        }

        viewModel.showFinish.observe(viewLifecycleOwner) { show ->
            if (show) {
                binding.nextButton?.text = getString(R.string.finish_text)
            } else {
                binding.nextButton?.text = getString(R.string.next_text)
            }
        }

        viewModel.remainingChoices.observe(viewLifecycleOwner) { choices ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, choices)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.scoreSpinner.adapter = adapter
        }

        // Observe navigation event
        viewModel.navigateToResult.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                    findNavController().navigate(R.id.action_gameFragment_to_resultFragment)
                viewModel.onNavigatedToResult()
            }
        }

        // Set up submit button click listener
        binding.rollButton?.setOnClickListener {
            viewModel.handleRoll()
            animateRolledDice(viewModel.dice.value ?: emptyList())
        }

        // Set up the next button click listener
        binding.nextButton?.setOnClickListener {
            if (viewModel.showFinish.value == true) {
                viewModel.handleFinish()
            } else {
                viewModel.handleNext(binding.scoreSpinner.selectedItem.toString())
            }
        }

    }

    private fun updateDiceImages(dice: List<Die>) {
        val diceImages = listOf(
            binding.die1Image,
            binding.die2Image,
            binding.die3Image,
            binding.die4Image,
            binding.die5Image,
            binding.die6Image
        )

        dice.forEachIndexed { index, die ->
            val dieImageView = diceImages[index]
            val imageResId = getImageResId(die.value)

            dieImageView.setImageResource(imageResId)
            dieImageView.alpha = if (die.selected) 0.5f else 1.0f

            dieImageView.setOnClickListener {
                if (dieSelectionEnabled) {
                    viewModel.toggleSelected(index)
                }else {
                    Toast.makeText(requireContext(),
                        "You must roll the dice before selecting!",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun animateRolledDice(dice: List<Die>) {
        val diceImages = listOf(
            binding.die1Image,
            binding.die2Image,
            binding.die3Image,
            binding.die4Image,
            binding.die5Image,
            binding.die6Image
        )

        dice.forEachIndexed { index, die ->
            if (die.selected) return@forEachIndexed

            val dieImageView = diceImages[index]
            val imageResId = getImageResId(die.value)

            val rotation = ObjectAnimator.ofFloat(dieImageView, "rotation", 0f, 360f)
            rotation.duration = 300
            rotation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    dieImageView.setImageResource(imageResId)
                }
            })
            rotation.start()
        }
    }

    private fun getImageResId(value: Int): Int {
        return when (value) {
            1 -> R.drawable.die_1
            2 -> R.drawable.die_2
            3 -> R.drawable.die_3
            4 -> R.drawable.die_4
            5 -> R.drawable.die_5
            6 -> R.drawable.die_6
            else -> R.drawable.die_1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}