package com.nawaf.kasirpas.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nawaf.kasirpas.MainActivity
import com.nawaf.kasirpas.R
import com.nawaf.kasirpas.databinding.ActivityOnboardingBinding
import com.nawaf.kasirpas.databinding.ItemOnboardingBinding
import com.nawaf.kasirpas.utils.PreferenceManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewPager()
        setupIndicators()
        setCurrentIndicator(0)

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < onboardingAdapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                navigateToMain()
            }
        }

        binding.btnSkip.setOnClickListener {
            navigateToMain()
        }
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(
            listOf(
                OnboardingItem(
                    R.string.onboarding_title_1,
                    R.string.onboarding_desc_1,
                    R.drawable.ic_bolt
                ),
                OnboardingItem(
                    R.string.onboarding_title_2,
                    R.string.onboarding_desc_2,
                    R.drawable.ic_inventory
                ),
                OnboardingItem(
                    R.string.onboarding_title_3,
                    R.string.onboarding_desc_3,
                    R.drawable.ic_store
                )
            )
        )
        binding.viewPager.adapter = onboardingAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                if (position == onboardingAdapter.itemCount - 1) {
                    binding.btnNext.text = getString(R.string.start_now)
                } else {
                    binding.btnNext.text = getString(R.string.next)
                }
            }
        })
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this.layoutParams = layoutParams
            }
            binding.indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = binding.indicatorContainer.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun navigateToMain() {
        prefManager.setOnboarded(true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    data class OnboardingItem(
        val title: Int,
        val description: Int,
        val image: Int
    )

    inner class OnboardingAdapter(private val items: List<OnboardingItem>) :
        RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
            return OnboardingViewHolder(
                ItemOnboardingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class OnboardingViewHolder(private val bindingItem: ItemOnboardingBinding) :
            RecyclerView.ViewHolder(bindingItem.root) {
            fun bind(item: OnboardingItem) {
                bindingItem.title.text = itemView.context.getString(item.title)
                bindingItem.description.text = itemView.context.getString(item.description)
                bindingItem.ivIllustration.setImageResource(item.image)
            }
        }
    }
}
