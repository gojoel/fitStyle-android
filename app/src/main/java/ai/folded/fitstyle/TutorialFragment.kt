package ai.folded.fitstyle

import ai.folded.fitstyle.data.Tutorial
import ai.folded.fitstyle.databinding.FragmentTutorialBinding
import ai.folded.fitstyle.databinding.ItemTutorialBinding
import ai.folded.fitstyle.utils.PREF_COMPLETED_TUTORIAL
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator

class TutorialFragment: Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: TutorialAdapter
    private lateinit var callback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTutorialBinding.inflate(inflater, container, false)
            .apply {
                adapter = TutorialAdapter(this@TutorialFragment)
                viewPager = pager
                viewPager.adapter = adapter

                TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

                callback = object: ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        continueButton.text = when (position) {
                            0 -> getString(R.string.start)
                            adapter.itemCount -1 -> getString(R.string.complete)
                            else -> getString(R.string.continue_title)
                        }
                    }
                }

                viewPager.registerOnPageChangeCallback(callback)

                continueButton.setOnClickListener {
                    val lastPage = viewPager.currentItem == adapter.tutorialPages.size - 1
                    if (lastPage) {
                        completeTutorial()
                    } else {
                        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    }
                }

                skipButton.setOnClickListener { completeTutorial() }

                backButton.setOnClickListener { onBackPressed() }
            }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.unregisterOnPageChangeCallback(callback)
    }

    private fun completeTutorial() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.edit()?.putBoolean(PREF_COMPLETED_TUTORIAL, true)?.apply()

        this.findNavController().navigate(
            TutorialFragmentDirections.actionTutorialToWelcomeFragment())
    }

    private fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}

private const val ARG_TUTORIAL = "tutorial"

class TutorialAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    lateinit var tutorialPages: ArrayList<Tutorial>

    init {
        addTutorialPages()
    }

    override fun getItemCount(): Int = tutorialPages.size

    override fun createFragment(position: Int): Fragment {
        val fragment = TutorialItemFragment()
        fragment.arguments = Bundle().apply {
            putParcelable(ARG_TUTORIAL, tutorialPages[position])
        }

        return fragment
    }

    private fun addTutorialPages() {
        val page1 = Tutorial(R.string.page_one_title, R.string.page_one_details)
        val page2 = Tutorial(R.string.page_two_title, R.string.page_two_details)
        val page3 = Tutorial(R.string.page_three_title, R.string.page_three_details, R.drawable.tutorial_select_style)
        val page4 = Tutorial(R.string.page_four_title, R.string.page_four_details, R.drawable.tutorial_select_photo)
        val page5 = Tutorial(R.string.page_five_title, R.string.page_five_details, R.drawable.tutorial_styled_image)

        tutorialPages = arrayListOf(page1, page2, page3, page4, page5)
    }
}

class TutorialItemFragment : Fragment() {

    private var _binding: ItemTutorialBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_TUTORIAL) }?.apply {
            (getParcelable(ARG_TUTORIAL) as? Tutorial)?.let {
                binding.title.text = getString(it.title)
                binding.details.text = getString(it.details)
                if (it.image != null) {
                    binding.imageContainer.visibility = View.VISIBLE
                    binding.image.setImageResource(it.image)
                } else {
                    binding.imageContainer.visibility = View.GONE
                }
            }
        }
    }
}
