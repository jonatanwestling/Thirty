package se.umu.c22jwg.thirty

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import se.umu.c22jwg.thirty.databinding.ActivityMainBinding
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {
    // Setup the view binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Init the view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        viewModel.dice.observe(this) { diceList ->
            updateDiceImages(diceList)
        }

        binding.submitScoreButton.setOnClickListener {
            viewModel.rollDice();
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
            val imageResId = when (die.value) {
                1 -> R.drawable.die_1
                2 -> R.drawable.die_2
                3 -> R.drawable.die_3
                4 -> R.drawable.die_4
                5 -> R.drawable.die_5
                6 -> R.drawable.die_6
                else -> R.drawable.die_1
            }
            // Add a rotation animation to the die images
            val rotation = ObjectAnimator.ofFloat(dieImageView, "rotation", 0f, 360f)
            rotation.duration = 300
            //Set the image and start animation
            rotation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    dieImageView.setImageResource(imageResId)
                }
            })

            rotation.start()
        }
    }
}