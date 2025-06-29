package se.umu.c22jwg.thirty.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import se.umu.c22jwg.thirty.R
import se.umu.c22jwg.thirty.databinding.FragmentGameBinding
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.viewmodel.GameViewModel
import androidx.navigation.fragment.findNavController
import androidx.activity.addCallback

class GameFragment : Fragment() {
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()
    private var isUpdatingSpinner = false // Flag to prevent listener firing during updates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Call gameOver to reset the game state
            viewModel.gameOver()
            // Navigate back to start fragment
            findNavController().navigate(R.id.action_gameFragment_to_startFragment)
        }
        /**
         * Observe the LiveData changes from the ViewModel
         */
        viewModel.dice.observe(viewLifecycleOwner) { diceList ->
            updateDiceImages(diceList)
        }
        viewModel.round.observe(viewLifecycleOwner) { round ->
            binding.RoundNum.text = getString(R.string.round_text, round);
        }
        viewModel.roll.observe(viewLifecycleOwner) { roll ->
            binding.RollNum.text = getString(R.string.roll_text, roll);
        }
        viewModel.score.observe(viewLifecycleOwner) { score ->
            binding.ScoreNum.text = "$score"
        }
        viewModel.rollButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.rollButton.isEnabled = enabled
        }
        viewModel.nextButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.nextButton.isEnabled = enabled
        }
        viewModel.isDieSelectionEnabled.observe(viewLifecycleOwner) {
            updateDiceImages(viewModel.dice.value ?: emptyList())
        }
        viewModel.showFinish.observe(viewLifecycleOwner) { show ->
            if (show) {
                binding.nextButton.text = getString(R.string.finish_text)
            } else {
                binding.nextButton.text = getString(R.string.next_text)
            }
        }
        viewModel.remainingChoices.observe(viewLifecycleOwner) { choices ->
            isUpdatingSpinner = true
            // New adapter with updated choices
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, choices)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.scoreSpinner.adapter = adapter
            isUpdatingSpinner = false
        }

        viewModel.selectedChoice.observe(viewLifecycleOwner) { selectedChoice ->
            // Update the spinner, avoid infinite loop
            if (selectedChoice != null && !isUpdatingSpinner) {
                isUpdatingSpinner = true
                // Get the remaining choices
                val choices = viewModel.remainingChoices.value ?: emptyList()
                // Set the selected choice
                if (choices.contains(selectedChoice)) {
                    val position = choices.indexOf(selectedChoice)
                    binding.scoreSpinner.setSelection(position)
                }
                isUpdatingSpinner = false
            }
        }
        viewModel.navigateToResult.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                    findNavController().navigate(R.id.action_gameFragment_to_resultFragment)
            }
        }
        // Set up submit button click listener
        binding.rollButton.setOnClickListener {
            viewModel.handleRoll()
            animateRolledDice(viewModel.dice.value ?: emptyList())
        }
        // Set up the next button click listener
        binding.nextButton.setOnClickListener {
            if (viewModel.showFinish.value == true) {
                viewModel.handleFinish()
            } else {
                viewModel.handleNext()
            }
        }
        // Update the viewmodel when the spinner item has changed
        binding.scoreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Only update the viewmodel if it was the user who changed the value
                if (!isUpdatingSpinner) {
                    val selectedChoice = parent.getItemAtPosition(position).toString()
                    viewModel.onSpinnerChoiceChanged(selectedChoice)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                //Not possible i think?
            }
        }

    }
    /**
     * Update the images of the dice based on the current state of the dice.
     *
     * @param dice The list of dice to be updated.
     */
    private fun updateDiceImages(dice: List<Die>) {
        val selectionEnabled = viewModel.isDieSelectionEnabled.value ?: false
        val diceImages = listOf(
            binding.die1Image,
            binding.die2Image,
            binding.die3Image,
            binding.die4Image,
            binding.die5Image,
            binding.die6Image
        )
        //  Update the dice images
        for ((index, die) in dice.withIndex()) {
            diceImages[index].apply {
                setImageResource(getImageResId(die.value))
                // Change opacity if selection is enabeled or the die is selected
                alpha = when {
                    !selectionEnabled -> 0.1f
                    die.selected -> 0.5f
                    else -> 1.0f
                }
                // Set click listener for selected dice
                setOnClickListener {

                    if (selectionEnabled) {
                        viewModel.toggleSelected(index)
                    } else {
                        // The user must make the initial roll before selecting
                        Toast.makeText(
                            requireContext(),
                            "You must roll the dice before selecting!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /**
     * Animate the roll of the dice.
     *
     * @param dice The list of dice to be animated.
     */
    private fun animateRolledDice(dice: List<Die>) {
        val diceImages = listOf(
            binding.die1Image,
            binding.die2Image,
            binding.die3Image,
            binding.die4Image,
            binding.die5Image,
            binding.die6Image
        )

        for ((index, die) in dice.withIndex()) {
            // Skip the selected dice
            if (die.selected) continue
            val dieImageView = diceImages[index]
            val imageResId = getImageResId(die.value)
            // Animate a full rotation of the die
            ObjectAnimator.ofFloat(dieImageView, "rotation", 0f, 360f).apply {
                duration = 300
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        dieImageView.setImageResource(imageResId)
                    }
                })
                start()
            }
        }
    }
    /**
     * Get the resource ID for the image of a die based on its value.
     *
     * @param value The value of the die.
     * @return The resource ID of the image.
     */
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
        _binding = null
    }
}